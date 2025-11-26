package cn.smartjavaai.face.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Landmark;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.HeadPose;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.face.EyeStatus;
import cn.smartjavaai.common.enums.face.GenderType;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.Graphics2DUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.facedect.mtcnn.MtcnnBatchResult;
import com.seeta.sdk.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人脸检测相关工具类
 * @author dwj
 * @date 2025/4/9
 */
public class FaceUtils {


    /**
     * 转换为FaceDetectedResult
     * @param detection
     * @param img
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(DetectedObjects detection, Image img){
        if(Objects.isNull(detection) || Objects.isNull(detection.getProbabilities())
                || detection.getProbabilities().isEmpty() || Objects.isNull(detection.items()) || detection.items().isEmpty()){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectedObjects.DetectedObject> detectedObjectList = detection.items();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        Iterator iterator = detectedObjectList.iterator();
        int index = 0;
        while(iterator.hasNext()) {
            DetectedObjects.DetectedObject result = (DetectedObjects.DetectedObject)iterator.next();
            BoundingBox box = result.getBoundingBox();
            List<Point> keyPoints = new ArrayList<Point>();
            box.getBounds().getPath().forEach(point -> {
                keyPoints.add(new Point(point.getX(), point.getY()));
            });
            int x = (int)(box.getBounds().getX() * img.getWidth());
            int y = (int)(box.getBounds().getY() * img.getHeight());
            int width = (int)(box.getBounds().getWidth() * img.getWidth());
            int height = (int)(box.getBounds().getHeight() * img.getHeight());
            // 修正边界，防止越界
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + width > img.getWidth()) width = img.getWidth() - x;
            if (y + height > img.getHeight()) height = img.getHeight() - y;
            DetectionRectangle rectangle = new DetectionRectangle(x, y, width, height);
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, detection.getProbabilities().get(index).floatValue(),faceInfo);
            detectionInfoList.add(detectionInfo);
            index++;
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }

    /**
     * 转换为FaceDetectedResult
     * @param seetaResult
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
            //过滤置信度
            /*if(config.getConfidenceThreshold() > 0){
                continue;
            }*/
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            List<Point> keyPoints = Arrays.stream(seetaPointFS)
                    .map(p -> new Point(p.x, p.y))
                    .collect(Collectors.toList());
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, 0, faceInfo);
            detectionInfoList.add(detectionInfo);
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }


    /**
     * 转换为FaceDetectedResult(人脸特征提取)
     * @param seetaResult
     * @return
     */
    public static DetectionResponse featuresConvertToResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList, List<float[]> featureList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            FaceInfo faceInfo = new FaceInfo();
            if(seetaPointFSList != null && seetaPointFSList.size() > 0){
                SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
                List<Point> keyPoints = Arrays.stream(seetaPointFS)
                        .map(p -> new Point(p.x, p.y))
                        .collect(Collectors.toList());
                faceInfo.setKeyPoints(keyPoints);
            }
            if(featureList != null && featureList.size() > 0){
                faceInfo.setFeature(featureList.get(i));
            }
            detectionInfoList.add(new DetectionInfo(rectangle, 0, faceInfo));
        }
        return new DetectionResponse(detectionInfoList);
    }


    /**
     * 转换为FaceDetectedResult(人脸特征提取)
     * @param rect
     * @param seetaPointFS
     * @param feature
     * @return
     */
    public static DetectionResponse featuresConvertToResponse(SeetaRect rect, SeetaPointF[] seetaPointFS, float[] feature){
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
        FaceInfo faceInfo = new FaceInfo();
        List<Point> keyPoints = Arrays.stream(seetaPointFS)
                .map(p -> new Point(p.x, p.y))
                .collect(Collectors.toList());
        faceInfo.setKeyPoints(keyPoints);
        faceInfo.setFeature(feature);
        detectionInfoList.add(new DetectionInfo(rectangle, 0, faceInfo));
        return new DetectionResponse(detectionInfoList);
    }



    /**
     * 修正检测框
     * @param rectangle
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static DetectionRectangle correctRect(DetectionRectangle rectangle, int imageWidth, int imageHeight) {
        int x = rectangle.getX();
        int y = rectangle.getY();
        int width = rectangle.getWidth();
        int height = rectangle.getHeight();
        // 修正x, y防止越界
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        // 宽高不能超出图片范围
        if (x + width > imageWidth) {
            width = imageWidth - x;
        }
        if (y + height > imageHeight) {
            height = imageHeight - y;
        }
        // 防止最终 width 或 height 为负或为 0
        if (width <= 0 || height <= 0) {
            return null; // 无效区域
        }
        return new DetectionRectangle(x, y, width, height);
    }

    /**
     * 子图中人脸关键点坐标 - Coordinates of key points in the image
     *
     * @param points
     * @return
     */
    public static double[][] facePoints(List<Point> points) {
        //      图中关键点坐标 - Coordinates of key points in the image
        //      1.  left_eye_x , left_eye_y
        //      2.  right_eye_x , right_eye_y
        //      3.  nose_x , nose_y
        //      4.  left_mouth_x , left_mouth_y
        //      5.  right_mouth_x , right_mouth_y
        double[][] pointsArray = new double[5][2]; // 保存人脸关键点 - Save facial key points
        int i = 0;
        for (Point point : points) {
            pointsArray[i][0] = point.getX();
            pointsArray[i][1] = point.getY();
            i++;
        }
        return pointsArray;
    }



    /**
     * 512x512的目标点 - Target point of 512x512
     * standard 5 landmarks for FFHQ faces with 512 x 512
     *
     * @param manager
     * @return
     */
    public static NDArray faceTemplate512x512(NDManager manager) {
        double[][] coord5point = {
                {192.98138, 239.94708}, // 512x512的目标点 - Target point of 512x512
                {318.90277, 240.1936},
                {256.63416, 314.01935},
                {201.26117, 371.41043},
                {313.08905, 371.15118}
        };
        NDArray points = manager.create(coord5point);
        return points;
    }

    /**
     * 112x112的目标点 - Target point of 112x112
     * standard 5 landmarks for FFHQ faces with 112x112
     *
     * @param manager
     * @return
     */
    public static NDArray faceTemplate112x112(NDManager manager) {
        double[][] coord5point = {
                {30.29459953, 51.69630051}, // 112x112的目标点 - Target point of 512x512
                {65.53179932, 51.50139999},
                {48.02519989, 71.73660278},
                {33.54930115, 87},
                {62.72990036, 87}
        };
        NDArray points = manager.create(coord5point);
        return points;
    }

    /**
     * 96x112的目标点 - Target point of 96x112
     * standard 5 landmarks for FFHQ faces with 96x112
     *
     * @param manager
     * @return
     */
    public static NDArray faceTemplate96x112(NDManager manager) {
        double[][] coord5point = {
                {30.29459953,  51.69630051},
                {65.53179932,  51.50139999},
                {48.02519989,  71.73660278},
                {33.54930115,  92.3655014},
                {62.72990036,  92.20410156}
        };
        NDArray points = manager.create(coord5point);
        return points;
    }


    /**
     * 绘制人脸属性
     * @param sourceImage
     * @param detectionResponse
     * @param savePath
     * @throws IOException
     */
    public static void drawBoxesWithFaceAttribute(BufferedImage sourceImage, DetectionResponse detectionResponse, String savePath) throws IOException {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        for(DetectionInfo detectionInfo : detectionResponse.getDetectionInfoList()){
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            graphics.setColor(Color.RED);// 边框颜色
            graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
            //drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //绘制人脸关键点
            if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                    !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
                Graphics2DUtils.drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
            }
            // 判断人脸框是否足够大
            if (rectangle.getHeight() > 60 && detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getFaceAttribute() != null) {
                StringBuilder attrText = new StringBuilder();
                FaceAttribute faceAttribute = detectionInfo.getFaceInfo().getFaceAttribute();
                if (faceAttribute.getGenderType() != null) {
                    attrText.append(faceAttribute.getGenderType().name()).append(" ");
                }

                if (faceAttribute.getAge() != null) {
                    attrText.append(faceAttribute.getAge()).append("岁").append(" ");
                }

                if (faceAttribute.getWearingMask() != null) {
                    attrText.append(faceAttribute.getWearingMask() ? "戴口罩" : "未戴口罩").append(" ");
                }

                if (faceAttribute.getLeftEyeStatus() != null && faceAttribute.getRightEyeStatus() != null) {
                    attrText.append("眼睛:")
                            .append(faceAttribute.getLeftEyeStatus().name())
                            .append("/")
                            .append(faceAttribute.getRightEyeStatus().name())
                            .append(" ");
                }

                List<String> lines = new ArrayList<>();

                if (faceAttribute.getGenderType() != null) {
                    lines.add("性别: " + faceAttribute.getGenderType().name());
                }
                if (faceAttribute.getAge() != null) {
                    lines.add("年龄: " + faceAttribute.getAge());
                }
                if (faceAttribute.getWearingMask() != null) {
                    lines.add("口罩: " + (faceAttribute.getWearingMask() ? "是" : "否"));
                }
                if (faceAttribute.getLeftEyeStatus() != null && faceAttribute.getRightEyeStatus() != null) {
                    lines.add("眼睛: " + faceAttribute.getLeftEyeStatus().name() + "/" + faceAttribute.getRightEyeStatus().name());
                }
                if (faceAttribute.getHeadPose() != null) {
                    //attrText.append("姿态:").append(faceAttribute.getHeadPose().toString());
                    HeadPose pose = faceAttribute.getHeadPose();
                    String pitch = pose.getPitch() != null ? String.valueOf(pose.getPitch().intValue()) : "-";
                    String yaw = pose.getYaw() != null ? String.valueOf(pose.getYaw().intValue()) : "-";
                    String roll = pose.getRoll() != null ? String.valueOf(pose.getRoll().intValue()) : "-";
                    lines.add("姿态: P=" + pitch + " Y=" + yaw + " R=" + roll);
                }
                if (!lines.isEmpty()) {
                    Graphics2DUtils.drawMultilineTextWithBackground(graphics, lines, rectangle.getX(), rectangle.getY());  // 适当偏移
                }

            }
        }
        graphics.dispose();
        ImageIO.write(sourceImage, "png", new File(savePath));
    }



    /**
     * 将 Milvus 查询返回的得分转换为 0~1 范围的相似度
     * @param metricType 向量度量方式：IP 或 L2
     * @param score 原始得分（L2 为距离，IP 为相似度）
     * @return 映射后的相似度（0~1）
     */
    public static float convertScoreToSimilarity(String metricType, float score) {
        switch (metricType.toUpperCase()) {
            case "IP":
                // 内积 IP 的范围为 [-1, 1]，归一化为 [0, 1]
                return (score + 1.0f) / 2.0f;
            case "L2":
                // 欧氏距离 L2：距离越小越相似，1 / (1 + 距离) 映射到 (0, 1]
                return 1.0f / (1.0f + score);
            case "COSINE":
                // 余弦相似度 COSINE：本身范围为 [-1, 1]，也需要归一化到 [0, 1]
                return (score + 1.0f) / 2.0f;
            default:
                throw new IllegalArgumentException("Unsupported metricType: " + metricType);
        }
    }

    /**
     * 裁剪人脸
     * @param image
     * @param rectangle
     * @return
     */
    public static Image cropFace(Image image, DetectionRectangle rectangle){
        return image.getSubImage(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    /**
     * 绘制人脸
     * @param image
     * @param rectangle
     * @return
     */
//    public static Image drawFaceName(Image image, DetectionResponse detectionResponse){
//
//    }



    /**
     * 将 Mtcnn 批量结果转换为 DJL 的 DetectedObjects
     * @param mtcnnBatchResult
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static DetectedObjects toDetectedObjects(MtcnnBatchResult mtcnnBatchResult, int imageWidth, int imageHeight) {
        List<String> classNames = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        List<BoundingBox> boxes = new ArrayList<>();

        NDArray boxesND = mtcnnBatchResult.boxes.get(0);
        NDArray probsND = mtcnnBatchResult.probs.get(0);
        NDArray pointsND = mtcnnBatchResult.points.get(0);
        if(pointsND != null){
            pointsND = pointsND.toType(DataType.FLOAT64, false);
        }
        if(boxesND == null || probsND == null || pointsND == null){
            return new DetectedObjects(classNames, probs, boxes);
        }
        long numBoxes = boxesND.getShape().get(0);
        for (int i = 0; i < numBoxes; i++) {
            NDArray box = boxesND.get(i);  // [x1, y1, x2, y2]
            NDArray prob = probsND.get(i);
            NDArray pointND = pointsND.get(i); // shape [5,2]

            float x1 = box.getFloat(0);
            float y1 = box.getFloat(1);
            float x2 = box.getFloat(2);
            float y2 = box.getFloat(3);

            // 转换为 DJL 的 Rectangle，需要归一化到 [0,1]
            double x = x1 / imageWidth;
            double y = y1 / imageHeight;
            double w = (x2 - x1) / imageWidth;
            double h = (y2 - y1) / imageHeight;

            List<ai.djl.modality.cv.output.Point> keyPoints = new ArrayList<>();
            double[] flatPoints = pointND.toDoubleArray(); // 一维长度 10
            for (int p = 0; p < 5; p++) {
                keyPoints.add(new ai.djl.modality.cv.output.Point(flatPoints[p * 2], flatPoints[p * 2 + 1]));
            }
            Landmark landmark =
                    new Landmark(x, y, w, h, keyPoints);
//            BoundingBox rect = new ai.djl.modality.cv.output.Rectangle(x, y, w, h);
            classNames.add("Face");  // 默认类别是人脸
            probs.add((double) prob.getFloat());
            boxes.add(landmark);
        }
        return new DetectedObjects(classNames, probs, boxes);
    }



}
