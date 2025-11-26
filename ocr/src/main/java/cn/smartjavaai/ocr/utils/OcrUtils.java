package cn.smartjavaai.ocr.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Landmark;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.ocr.entity.*;
import cn.smartjavaai.ocr.entity.RotatedBox;
import cn.smartjavaai.ocr.enums.AngleEnum;
import cn.smartjavaai.ocr.enums.PlateType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * @author dwj
 * @date 2025/4/22
 */
@Slf4j
public class OcrUtils {


    /**
     * 转换为OcrBox
     * @param dt_boxes
     * @return
     */
    public static List<OcrBox> convertToOcrBox(NDList dt_boxes) {
        List<OcrBox> boxList = new ArrayList<>();
        for (NDArray box : dt_boxes) {
            float[] pointsArr = box.toFloatArray();
            OcrBox ocrBox = new OcrBox(
                    new Point(pointsArr[0], pointsArr[1]),
                    new Point(pointsArr[2], pointsArr[3]),
                    new Point(pointsArr[4], pointsArr[5]),
                    new Point(pointsArr[6], pointsArr[7])
            );
            boxList.add(ocrBox);
        }
        return boxList;
    }

    /**
     * 转换为OcrBox
     * @param ndLists
     * @return
     */
    public static List<List<OcrBox>> convertToOcrBox(List<NDList> ndLists) {
        if (ndLists == null || ndLists.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<OcrBox>> boxLists = new ArrayList<>();
        for (NDList dt_boxes : ndLists) {
            boxLists.add(convertToOcrBox(dt_boxes));
        }
        return boxLists;
    }




    /**
     * 逆时针旋转图片
     *
     * @param image
     * @param angleEnum
     * @return
     */
    public static Image rotateImg(Image image, AngleEnum angleEnum) {
        try (NDManager manager = NDManager.newBaseManager()) {
            int times = 0;
            switch (angleEnum) {
                case ANGLE_90:
                    times = 1;
                    break;
                case ANGLE_180:
                    times = 2;
                    break;
                case ANGLE_270:
                    times = 3;
                    break;
            }
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), times);
            return OpenCVImageFactory.getInstance().fromNDArray(rotated);
        }
    }


    /**
     * 转换为OcrInfo
     * @param lines
     * @return
     */
    public static OcrInfo convertToOcrInfo(List<ArrayList<RotatedBoxCompX>> lines){
        if(Objects.isNull(lines) || lines.size() == 0){
            return null;
        }
        List<List<OcrItem>> lineList = new ArrayList<List<OcrItem>>();
        String fullText = "";
        for(ArrayList<RotatedBoxCompX> boxList : lines){
            List<OcrItem> line = new ArrayList<OcrItem>();
            for(RotatedBoxCompX box : boxList){
                float[] pointsArr = box.getBox().toFloatArray();
                float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
                float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
                float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
                float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
                OcrBox ocrBox = new OcrBox(new Point(lt[0], lt[1]), new Point(rt[0], rt[1]), new Point(rb[0], rb[1]), new Point(lb[0], lb[1]));
                OcrItem ocrItem = new OcrItem(ocrBox, box.getText());
                line.add(ocrItem);
                String text = box.getText();
                if(text.trim().equals(""))
                    continue;
                fullText += text + " ";
            }
            lineList.add(line);
            fullText += '\n';
        }
        return new OcrInfo(lineList, fullText);
    }

    public static OcrInfo convertRotatedBoxesToOcrItems(List<RotatedBox> rotatedBoxes) {
        OcrInfo ocrInfo = new OcrInfo();
        List<OcrItem> ocrItems = new ArrayList<>();
        StringBuilder fullText = new StringBuilder();
        for (RotatedBox rotatedBox : rotatedBoxes) {
            NDArray box = rotatedBox.getBox();
            float[] points = box.toFloatArray();
            Point topLeft = new Point(points[0], points[1]);
            Point topRight = new Point(points[2], points[3]);
            Point bottomRight = new Point(points[4], points[5]);
            Point bottomLeft = new Point(points[6], points[7]);

            OcrBox ocrBox = new OcrBox(topLeft, topRight, bottomRight, bottomLeft);
            String text = rotatedBox.getText();

            OcrItem item = new OcrItem();
            item.setOcrBox(ocrBox);
            item.setText(text);
            ocrItems.add(item);
            fullText.append(text + " ");
        }
        if (fullText.length() > 0) {
            fullText.deleteCharAt(fullText.length() - 1);
        }
        ocrInfo.setOcrItemList(ocrItems);
        ocrInfo.setFullText(fullText.toString());
        return ocrInfo;
    }


    /**
     * 透视变换 + 裁剪
     * @param srcMat
     * @param box
     * @return
     */
    public static Image transformAndCrop(Mat srcMat, OcrBox box){
        Mat subImg = transformAndCropToMat(srcMat, box);
        return SmartImageFactory.getInstance().fromMat(subImg);
    }

    /**
     * 透视变换 + 裁剪
     * @param srcMat
     * @param box
     * @return
     */
    public static Mat transformAndCropToMat(Mat srcMat, OcrBox box){
        float[] pointsArr = box.toFloatArray();
        float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
        float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
        float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
        float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
        int img_crop_width = (int) Math.max(PointUtils.distance(lt, rt), PointUtils.distance(rb, lb));
        int img_crop_height = (int) Math.max(PointUtils.distance(lt, lb), PointUtils.distance(rt, rb));
        List<ai.djl.modality.cv.output.Point> srcPoints = new ArrayList<>();
        srcPoints.add(new ai.djl.modality.cv.output.Point(lt[0], lt[1]));
        srcPoints.add(new ai.djl.modality.cv.output.Point(rt[0], rt[1]));
        srcPoints.add(new ai.djl.modality.cv.output.Point(rb[0], rb[1]));
        srcPoints.add(new ai.djl.modality.cv.output.Point(lb[0], lb[1]));
        List<ai.djl.modality.cv.output.Point> dstPoints = new ArrayList<>();
        dstPoints.add(new ai.djl.modality.cv.output.Point(0, 0));
        dstPoints.add(new ai.djl.modality.cv.output.Point(img_crop_width, 0));
        dstPoints.add(new ai.djl.modality.cv.output.Point(img_crop_width, img_crop_height));
        dstPoints.add(new ai.djl.modality.cv.output.Point(0, img_crop_height));
        Mat srcPoint2f = DJLCommonUtils.toMat(srcPoints);
        Mat dstPoint2f = DJLCommonUtils.toMat(dstPoints);
        //透视变换
        Mat cvMat = OpenCVUtils.perspectiveTransform(srcMat, srcPoint2f, dstPoint2f);
        srcPoint2f.release();
        dstPoint2f.release();
        Mat result = OpenCVUtils.getSubImage(cvMat, 0, 0, img_crop_width, img_crop_height);
        cvMat.release();
        return result;
    }

    /**
     * 绘制文本框
     *
     * @param mat
     * @param boxList
     */
    public static void drawRect(Mat mat, List<OcrBox> boxList) {
        for(OcrBox ocrBox : boxList){
            Imgproc.line(mat, ocrBox.getTopLeft().toCvPoint(), ocrBox.getTopRight().toCvPoint(), new Scalar(0, 255, 0), 1);
            Imgproc.line(mat, ocrBox.getTopRight().toCvPoint(), ocrBox.getBottomRight().toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(mat, ocrBox.getBottomRight().toCvPoint(), ocrBox.getBottomLeft().toCvPoint(), new Scalar(0, 255, 0),1);
            Imgproc.line(mat, ocrBox.getBottomLeft().toCvPoint(), ocrBox.getTopLeft().toCvPoint(), new Scalar(0, 255, 0), 1);
        }
    }


    /**
     * 将 OCR 结果转换为多边形标签列表
     * @param ocrItemList OCR 识别结果
     * @return PolygonLabel 列表
     */
    public static List<PolygonLabel> toPolygonLabelList(List<OcrItem> ocrItemList) {
        List<PolygonLabel> polygonLabelList = new ArrayList<>();
        if (ocrItemList == null || ocrItemList.isEmpty()) {
            return polygonLabelList;
        }
        for (OcrItem item : ocrItemList) {
            if (item.getOcrBox() == null) continue;
            List<cn.smartjavaai.common.entity.Point> points = Arrays.asList(
                    item.getOcrBox().getTopLeft(),
                    item.getOcrBox().getTopRight(),
                    item.getOcrBox().getBottomRight(),
                    item.getOcrBox().getBottomLeft()
            );
            String text = null;
            //角度
            if(item.getAngle() != null){
                text = item.getAngle().getValue();
            }else{
                text = item.getText();
            }
            PolygonLabel label = new PolygonLabel(points, text);
            polygonLabelList.add(label);
        }
        return polygonLabelList;
    }


    /**
     * 将 OCR 结果转换为多边形标签列表
     * @param boxList
     * @return PolygonLabel 列表
     */
    public static List<PolygonLabel> ocrBoxtoPolygonLabelList(List<OcrBox> boxList) {
        List<PolygonLabel> polygonLabelList = new ArrayList<>();
        if (boxList == null || boxList.isEmpty()) {
            return polygonLabelList;
        }
        for (OcrBox item : boxList) {
            List<cn.smartjavaai.common.entity.Point> points = Arrays.asList(
                    item.getTopLeft(),
                    item.getTopRight(),
                    item.getBottomRight(),
                    item.getBottomLeft()
            );
            PolygonLabel label = new PolygonLabel(points);
            polygonLabelList.add(label);
        }
        return polygonLabelList;
    }


    /**
     * OCR 结果绘制
     */
    public static void drawOcrResult(BufferedImage image, OcrInfo ocrInfo, int fontSize) {
        List<OcrItem> ocrItemList = ocrInfo.getOcrItemList();
        if (CollectionUtils.isNotEmpty(ocrInfo.getLineList())) {
            ocrItemList = ocrInfo.flattenLines();
        }
        List<PolygonLabel> polygonLabelList = toPolygonLabelList(ocrItemList);
        BufferedImageUtils.drawPolygonWithText(image, polygonLabelList, fontSize);
    }

    /**
     * OCR 结果绘制
     */
    public static void drawOcrResult(BufferedImage image, List<OcrItem> ocrItemList, int fontSize) {
        List<PolygonLabel> polygonLabelList = toPolygonLabelList(ocrItemList);
        BufferedImageUtils.drawPolygonWithText(image, polygonLabelList, fontSize);
    }

    /**
     * OCR 结果绘制
     */
    public static void drawOcrDetResult(Image image, List<OcrBox> ocrBoxList, int fontSize) {
        List<PolygonLabel> polygonLabelList = ocrBoxtoPolygonLabelList(ocrBoxList);
        ImageUtils.drawPolygonWithText(image, polygonLabelList, fontSize);
    }

    /**
     * OCR 结果绘制
     */
    public static void drawOcrResult(Image image, List<OcrItem> ocrItemList, int fontSize) {
        List<PolygonLabel> polygonLabelList = toPolygonLabelList(ocrItemList);
        ImageUtils.drawPolygonWithText(image, polygonLabelList, fontSize);
    }



    /**
     * 绘制文本框及文本
     * @param srcMat
     * @param itemList
     */
//    public static void drawRectWithText(Mat srcMat, List<OcrItem> itemList) {
//        for(OcrItem item : itemList){
//            OcrBox ocrBox = item.getOcrBox();
//            Imgproc.line(srcMat, ocrBox.getTopLeft().toCvPoint(), ocrBox.getTopRight().toCvPoint(), new Scalar(0, 255, 0), 1);
//            Imgproc.line(srcMat, ocrBox.getTopRight().toCvPoint(), ocrBox.getBottomRight().toCvPoint(), new Scalar(0, 255, 0),1);
//            Imgproc.line(srcMat, ocrBox.getBottomRight().toCvPoint(), ocrBox.getBottomLeft().toCvPoint(), new Scalar(0, 255, 0),1);
//            Imgproc.line(srcMat, ocrBox.getBottomLeft().toCvPoint(), ocrBox.getTopLeft().toCvPoint(), new Scalar(0, 255, 0), 1);
//            // 中文乱码
//            Imgproc.putText(srcMat, item.getAngle().getValue(), ocrBox.getTopLeft().toCvPoint(), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0, 255, 0), 1);
//        }
//    }


    public static List<PlateInfo> convertToPlateInfo(DetectedObjects detectedObjects, Image image) {
        List<PlateInfo> plateInfoList = new ArrayList<>();
        Iterator iterator = detectedObjects.items().iterator();
        int index = 0;
        while(iterator.hasNext()) {
            DetectedObjects.DetectedObject result = (DetectedObjects.DetectedObject)iterator.next();
            BoundingBox box = result.getBoundingBox();
            List<Point> keyPoints = new ArrayList<Point>();
            if(box instanceof Landmark){
                box.getBounds().getPath().forEach(point -> {
                    keyPoints.add(new Point(point.getX(), point.getY()));
                });
            }
            int x = (int)(box.getBounds().getX() * image.getWidth());
            int y = (int)(box.getBounds().getY() * image.getHeight());
            int width = (int)(box.getBounds().getWidth() * image.getWidth());
            int height = (int)(box.getBounds().getHeight() * image.getHeight());
            // 修正边界，防止越界
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x + width > image.getWidth()) width = image.getWidth() - x;
            if (y + height > image.getHeight()) height = image.getHeight() - y;
            PlateInfo plateInfo = new PlateInfo();
            plateInfo.setPlateType(PlateType.fromClassName(detectedObjects.getClassNames().get(index)));
            plateInfo.setScore(detectedObjects.getProbabilities().get(index).floatValue());
            plateInfo.setDetectionRectangle(new DetectionRectangle(x, y, width, height));
            OcrBox ocrBox = new OcrBox(keyPoints.get(0), keyPoints.get(1), keyPoints.get(2), keyPoints.get(3));
            plateInfo.setBox(ocrBox);
            plateInfoList.add(plateInfo);
            index++;
        }
        return plateInfoList;
    }

    /**
     * 绘制车牌信息
     * @param srcMat
     * @param plateInfoList
     */
    public static void drawPlateInfo(Mat srcMat, List<PlateInfo> plateInfoList) {
        for(PlateInfo plateInfo : plateInfoList){
            OcrBox ocrBox = plateInfo.getBox();
            Imgproc.line(srcMat, ocrBox.getTopLeft().toCvPoint(), ocrBox.getTopRight().toCvPoint(), new Scalar(0, 0, 255), 1);
            Imgproc.line(srcMat, ocrBox.getTopRight().toCvPoint(), ocrBox.getBottomRight().toCvPoint(), new Scalar(0, 0, 255),1);
            Imgproc.line(srcMat, ocrBox.getBottomRight().toCvPoint(), ocrBox.getBottomLeft().toCvPoint(), new Scalar(0, 0, 255),1);
            Imgproc.line(srcMat, ocrBox.getBottomLeft().toCvPoint(), ocrBox.getTopLeft().toCvPoint(), new Scalar(0, 0, 255), 1);
            // 中文乱码
            OpenCVUtils.putTextWithBackground(srcMat, plateInfo.getPlateNumber() + " " + plateInfo.getPlateColor(), ocrBox.getTopLeft().toCvPoint(), new Scalar(255, 255, 255), new Scalar(0, 0, 0), 1);
        }
    }

    /**
     * 在图像上绘制带白色背景、黑色文字的文本
     */
    public static void drawPlateInfo(BufferedImage image, List<PlateInfo> plateInfoList) {
        // 将绘制图像转换为Graphics2D
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        for(PlateInfo plateInfo : plateInfoList){
            DetectionRectangle rectangle = plateInfo.getDetectionRectangle();
            OcrBox ocrBox = plateInfo.getBox();
            String text = plateInfo.getPlateNumber() + " " + plateInfo.getPlateColor();
            BufferedImageUtils.drawRectAndText(graphics, rectangle, text, Color.BLACK);
            List<Point> keyPoints = new ArrayList<Point>();
            keyPoints.add(ocrBox.getTopLeft());
            keyPoints.add(ocrBox.getTopRight());
            keyPoints.add(ocrBox.getBottomRight());
            keyPoints.add(ocrBox.getBottomLeft());
            BufferedImageUtils.drawKeyPoints(graphics, keyPoints, Color.GREEN);
        }
        graphics.dispose();
    }


}
