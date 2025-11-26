package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.util.RandomUtils;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.PolygonLabel;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.HeadPose;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * OpenCV 工具类
 */
public class OpenCVUtils {


    /**
     * canny算法，边缘检测
     *
     * @param src
     * @return
     */
    public static Mat canny(Mat src) {
        Mat mat = src.clone();
        Imgproc.Canny(src, mat, 100, 200);
        return mat;
    }

    /**
     * 画线
     *
     * @param mat
     * @param point1
     * @param point2
     */
    public static void line(Mat mat, Point point1, Point point2) {
        Imgproc.line(mat, point1, point2, new Scalar(255, 255, 255), 1);
    }

    /**
     * NDArray to opencv_core.Mat
     *
     * @param manager
     * @param srcPoints
     * @param dstPoints
     * @return
     */
    public static Mat toOpenCVMat(NDManager manager, NDArray srcPoints, NDArray dstPoints) {
        NDArray svdMat = SVDUtils.transformationFromPoints(manager, srcPoints, dstPoints);
        double[] doubleArray = svdMat.toDoubleArray();
        Mat newSvdMat = new Mat(2, 3, CvType.CV_64F);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                newSvdMat.put(i, j, doubleArray[i * 3 + j]);
            }
        }
        return newSvdMat;
    }

    /**
     * double[][] points array to Mat
     * @param points
     * @return
     */
    public static Mat toOpenCVMat(double[][] points) {
        Mat mat = new Mat(5, 2, CvType.CV_64F);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                mat.put(i, j, points[i * 5 + j]);
            }
        }
        return mat;
    }

    /**
     * 变换矩阵的逆矩阵
     *
     * @param src
     * @return
     */
    public static Mat invertAffineTransform(Mat src) {
        Mat dst = src.clone();
        Imgproc.invertAffineTransform(src, dst);
        return dst;
    }

    /**
     * Mat to BufferedImage
     *
     * @param mat
     * @return
     */
    public static BufferedImage mat2Image(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        byte[] data = new byte[width * height * (int) mat.elemSize()];
        Imgproc.cvtColor(mat, mat, 4);
        mat.get(0, 0, data);
        BufferedImage ret = new BufferedImage(width, height, 5);
        ret.getRaster().setDataElements(0, 0, width, height, data);
        return ret;
    }

    /**
     * BufferedImage to Mat
     *
     * @param img
     * @return
     */
    public static Mat image2Mat(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // 强制转换为 TYPE_3BYTE_BGR，自动去除透明通道
        BufferedImage convertedImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = convertedImg.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        byte[] data = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    /**
     * 透视变换
     *
     * @param src
     * @param srcPoints
     * @param dstPoints
     * @return
     */
    public static Mat perspectiveTransform(Mat src, Mat srcPoints, Mat dstPoints) {
        Mat dst = src.clone();
        Mat warp_mat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Imgproc.warpPerspective(src, dst, warp_mat, dst.size());
        warp_mat.release();
        return dst;
    }

    /**
     * 绘制矩形框和文字
     *
     * @param image
     * @param detectionInfoList
     */
    public static void drawRectAndText(Mat image, List<DetectionInfo> detectionInfoList) {
        if(CollectionUtils.isEmpty(detectionInfoList))
            return;
        for(DetectionInfo detectionInfo : detectionInfoList){
            drawRectAndText(image, detectionInfo);
        }
    }


    /**
     * 绘制矩形框和文字
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(Mat image, DetectionRectangle box, String text) {
        if(image.empty())
            return;
        int x = box.getX();
        int y = box.getY();
        int width = box.getWidth();
        int height = box.getHeight();
        Scalar rectangleColor = new Scalar((double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178));
        // 绘制矩形框
        Point pt1 = new Point(x, y);
        Point pt2 = new Point(x + width, y + height);
        Imgproc.rectangle(image, pt1, pt2, rectangleColor, 2);
        Scalar textColor = new Scalar(255.0, 255.0, 255.0);
        putTextWithBackground(image, text, pt1, textColor, rectangleColor, 1);
    }

    /**
     * 绘制矩形框和文字
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(Mat image, DetectionRectangle box, String text, double fontSize) {
        if(image.empty())
            return;
        int x = box.getX();
        int y = box.getY();
        int width = box.getWidth();
        int height = box.getHeight();
        Scalar rectangleColor = new Scalar((double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178));
        // 绘制矩形框
        Point pt1 = new Point(x, y);
        Point pt2 = new Point(x + width, y + height);
        Imgproc.rectangle(image, pt1, pt2, rectangleColor, 2);
        Scalar textColor = new Scalar(255.0, 255.0, 255.0);
        putTextWithBackground(image, text, pt1, textColor, rectangleColor, 1, fontSize);
    }


    /**
     * 绘制矩形框和文字
     *
     * @param image
     * @param detectionInfo
     */
    public static void drawRectAndText(Mat image, DetectionInfo detectionInfo) {
        if (image == null) return;
        int x = detectionInfo.getDetectionRectangle().getX();
        int y = detectionInfo.getDetectionRectangle().getY();
        int width = detectionInfo.getDetectionRectangle().getWidth();
        int height = detectionInfo.getDetectionRectangle().getHeight();
        Scalar rectangleColor = new Scalar((double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178), (double)RandomUtils.nextInt(178));
        // 绘制矩形框
        Point pt1 = new Point(x, y);
        Point pt2 = new Point(x + width, y + height);
        Imgproc.rectangle(image, pt1, pt2, rectangleColor, 2);
        // 绘制文字
        String className = null;
        Scalar textColor = new Scalar(255.0, 255.0, 255.0);
        //目标检测信息
        if(detectionInfo.getObjectDetInfo() != null){
            className = detectionInfo.getObjectDetInfo().getClassName();
            putTextWithBackground(image, className, pt1, textColor, rectangleColor, 1);
        }
        //人脸
        if(detectionInfo.getFaceInfo() != null){
            className = "face";
            putTextWithBackground(image, className, pt1, textColor, rectangleColor, 1);
            //绘制关键点
            drawLandmarks(image, detectionInfo.getFaceInfo().getKeyPoints());
            //绘制人脸属性
            if(detectionInfo.getFaceInfo().getFaceAttribute() != null){
                drawFaceAttribute(detectionInfo.getFaceInfo().getFaceAttribute(), detectionInfo.getDetectionRectangle(), image);
            }
        }
    }


    /**
     * 在Mat上绘制矩形框和文字
     *
     * @param mat       待绘制的Mat
     * @param x         矩形左上角X
     * @param y         矩形左上角Y
     * @param width     矩形宽度
     * @param height    矩形高度
     * @param color     框的颜色，例如 new Scalar(0, 255, 0) 绿色
     * @param thickness 框线宽度
     * @param text      需要绘制的文字，可以为null或空
     * @param fontScale 文字缩放比例
     * @param textColor 文字颜色
     */
    public static void drawRectAndText(Mat mat,
                                       int x, int y, int width, int height,
                                       Scalar color, int thickness,
                                       String text, double fontScale, Scalar textColor) {

        if (mat == null || mat.empty()) return;

        // 绘制矩形框
        Point pt1 = new Point(x, y);
        Point pt2 = new Point(x + width, y + height);
        Imgproc.rectangle(mat, pt1, pt2, color, thickness);

        // 绘制文字
        if (text != null && !text.isEmpty()) {
            int baseline[] = new int[1];
            Size textSize = Imgproc.getTextSize(text, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, thickness, baseline);
            // 保证文字不超出矩形
            Point textOrg = new Point(x, y - 5 < 0 ? y + textSize.height + 5 : y - 5);
            Imgproc.putText(mat, text, textOrg, Imgproc.FONT_HERSHEY_SIMPLEX, fontScale, textColor, thickness);
        }
    }

    /**
     * 将 Bytedeco 的 Mat 转换为 OpenCV 官方的 Mat
     * @param src Bytedeco Mat (BGR 或 BGRA)
     * @return OpenCV Mat (BGR 或 BGRA)
     */
//    public static org.opencv.core.Mat convertToOpenCVMat(org.bytedeco.opencv.opencv_core.Mat bMat) {
//
//
//        try {
//            int width = bMat.cols();
//            int height = bMat.rows();
//            int channels = bMat.channels();
//
//            // 创建 OpenCV Mat
//            org.opencv.core.Mat cvMat = new org.opencv.core.Mat(height, width, channels == 3 ? CvType.CV_8UC3 : CvType.CV_8UC1);
//
//            // 从 bytedeco Mat 获取像素数据
//            byte[] data = new byte[width * height * channels];
//            bMat.data().get(data);
//
//            // 填充到 OpenCV Mat
//            cvMat.put(0, 0, data);
//            return cvMat;
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    /**
     * 从 OpenCV Mat 中获取 BGR 格式矩阵数据
     *
     * @param mat OpenCV Mat，需为 CV_8UC3 或可转换为 BGR 格式
     * @return BGR 格式字节数组，按行连续存储
     */
    public static byte[] getMatrixBGR(Mat mat) {
        if (mat == null || mat.empty()) {
            throw new IllegalArgumentException("Mat 不能为空");
        }

        // 确保是三通道 BGR 格式
        Mat bgrMat = new Mat();
        if (mat.channels() == 3) {
            mat.copyTo(bgrMat);
        } else if (mat.channels() == 4) {
            // RGBA 转 BGR
            Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_RGBA2BGR);
        } else if (mat.channels() == 1) {
            // 灰度转 BGR
            Imgproc.cvtColor(mat, bgrMat, Imgproc.COLOR_GRAY2BGR);
        } else {
            throw new IllegalArgumentException("不支持的通道数: " + mat.channels());
        }

        int size = (int) (bgrMat.total() * bgrMat.channels());
        byte[] data = new byte[size];
        bgrMat.get(0, 0, data);
        bgrMat.release(); // 释放临时 Mat
        return data;
    }

    /**
     * 在图像上绘制带白色背景、黑色文字的文本
     */
    public static void putTextWithBackground(Mat image, String text, org.opencv.core.Point origin, Scalar textColor, Scalar backgroundColor, int padding) {
        // 默认字体
        int font = Imgproc.FONT_HERSHEY_SIMPLEX;
        // 默认字体缩放大小
        double fontScale = 0.6;
        //线条粗细
        int thickness = 2;
        //获取文字大小
        int[] baseLine = new int[1];
        Size textSize = Imgproc.getTextSize(text, font, fontScale, thickness, baseLine);
        int textWidth = (int) textSize.width;
        int textHeight = (int) textSize.height;

        //计算带padding的背景框
        org.opencv.core.Point bgTopLeft = new org.opencv.core.Point(origin.x - padding, origin.y - textHeight - padding);
        org.opencv.core.Point bgBottomRight = new org.opencv.core.Point(origin.x + textWidth + padding, origin.y + baseLine[0] + padding);

        //绘制背景矩形
        Imgproc.rectangle(image, bgTopLeft, bgBottomRight, backgroundColor, Imgproc.FILLED);

        //绘制文字（黑色）
        Imgproc.putText(image, text, origin, font, fontScale, textColor, thickness);
    }

    /**
     * 在图像上绘制带白色背景、黑色文字的文本
     */
    public static void putTextWithBackground(Mat image, String text, org.opencv.core.Point origin, Scalar textColor, Scalar backgroundColor, int padding, double fontScale) {
        // 默认字体
        int font = Imgproc.FONT_HERSHEY_SIMPLEX;
        //线条粗细
        int thickness = 1;
        //获取文字大小
        int[] baseLine = new int[1];
        Size textSize = Imgproc.getTextSize(text, font, fontScale, thickness, baseLine);
        int textWidth = (int) textSize.width;
        int textHeight = (int) textSize.height;

        //计算带padding的背景框
        org.opencv.core.Point bgTopLeft = new org.opencv.core.Point(origin.x - padding, origin.y - textHeight - padding);
        org.opencv.core.Point bgBottomRight = new org.opencv.core.Point(origin.x + textWidth + padding, origin.y + baseLine[0] + padding);

        //绘制背景矩形
        Imgproc.rectangle(image, bgTopLeft, bgBottomRight, backgroundColor, Imgproc.FILLED);

        //绘制文字（黑色）
        Imgproc.putText(image, text, origin, font, fontScale, textColor, thickness);
    }

    /**
     * 绘制检测结果
     * @param image 待绘制的图片
     * @param detectionResponse 检测结果
     * @return 绘制后的图片
     */
    public static void drawBoundingBoxes(Mat image, DetectionResponse detectionResponse){
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new IllegalArgumentException("无目标数据");
        }
        drawRectAndText(image, detectionResponse.getDetectionInfoList());
    }

    /**
     * 在 Mat 上绘制关键点
     * @param mat OpenCV 图像
     * @param keyPoints 人脸关键点列表
     */
    public static void drawLandmarks(Mat mat, List<cn.smartjavaai.common.entity.Point> keyPoints) {
        // 设置颜色 (BGR 格式)，这里是橙色 (0,96,246)
        Scalar color = new Scalar(0, 96, 246);
        // 遍历关键点，用圆来表示 (比矩形更自然)
        for (cn.smartjavaai.common.entity.Point p : keyPoints) {
            Point cvPoint = new Point(p.getX(), p.getY());
            Imgproc.circle(mat, cvPoint, 2, color, 2, Imgproc.LINE_AA, 0);
        }
    }

    /**
     * 在 Mat 上绘制多行文字，并带背景
     * @param mat OpenCV Mat
     * @param lines 文字行
     * @param x 起始 x
     * @param y 起始 y
     */
    public static void drawMultilineTextWithBackground(Mat mat, List<String> lines, int x, int y) {
        int fontFace = Imgproc.FONT_HERSHEY_SIMPLEX;
        double fontScale = 0.5; // 字体大小
        int thickness = 1;
        int baseline[] = {0};

        // 逐行计算最大宽度 & 总高度
        int maxWidth = 0;
        int lineHeight = 0;
        for (String line : lines) {
            Size textSize = Imgproc.getTextSize(line, fontFace, fontScale, thickness, baseline);
            maxWidth = Math.max(maxWidth, (int) textSize.width);
            lineHeight = Math.max(lineHeight, (int) (textSize.height + baseline[0]));
        }

        int padding = 4;
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = lineHeight * lines.size() + padding * 2;

        // 绘制背景矩形 (半透明黑色在 OpenCV 里不好直接实现，只能画实色或用 addWeighted 合成)
        Scalar bgColor = new Scalar(0, 0, 0); // BGR = 黑色
        Point topLeft = new Point(x, y);
        Point bottomRight = new Point(x + boxWidth, y + boxHeight);
        Imgproc.rectangle(mat, topLeft, bottomRight, bgColor, -1); // -1 表示填充

        // 逐行绘制文字 (白色)
        Scalar textColor = new Scalar(255, 255, 255);
        for (int i = 0; i < lines.size(); i++) {
            int textY = y + padding + (i + 1) * lineHeight;
            Imgproc.putText(mat, lines.get(i),
                    new Point(x + padding, textY),
                    fontFace, fontScale, textColor, thickness, Imgproc.LINE_AA, false);
        }
    }

    /**
     * 绘制人脸属性
     * @param faceAttribute
     * @param rectangle
     * @param mat
     */
    public static void drawFaceAttribute(FaceAttribute faceAttribute, DetectionRectangle rectangle, Mat mat){
        List<String> lines = new ArrayList<>();
        if (faceAttribute.getGenderType() != null) {
            lines.add("gender: " + faceAttribute.getGenderType());
        }
        if (faceAttribute.getAge() != null) {
            lines.add("age: " + faceAttribute.getAge());
        }
        if (faceAttribute.getWearingMask() != null) {
            lines.add("mask: " + (faceAttribute.getWearingMask() ? "yes" : "no"));
        }
        if (faceAttribute.getLeftEyeStatus() != null && faceAttribute.getRightEyeStatus() != null) {
            lines.add("eyes: " + faceAttribute.getLeftEyeStatus() + "/" + faceAttribute.getRightEyeStatus());
        }
        if (faceAttribute.getHeadPose() != null) {
            HeadPose pose = faceAttribute.getHeadPose();
            String pitch = pose.getPitch() != null ? String.valueOf(pose.getPitch().intValue()) : "-";
            String yaw = pose.getYaw() != null ? String.valueOf(pose.getYaw().intValue()) : "-";
            String roll = pose.getRoll() != null ? String.valueOf(pose.getRoll().intValue()) : "-";
            lines.add("head pose: P=" + pitch + " Y=" + yaw + " R=" + roll);
        }
        if (!lines.isEmpty()) {
            drawMultilineTextWithBackground(mat, lines, rectangle.getX(), rectangle.getY());  // 适当偏移
        }
    }


    /**
     * 透视变换 + 裁剪
     * @param srcMat
     * @param landMarks
     * @return
     */
    public static Image transformAndCrop(Mat srcMat, List<ai.djl.modality.cv.output.Point> landMarks){
        if (landMarks == null || landMarks.size() != 4) {
            throw new IllegalArgumentException("必须提供4个关键点");
        }

        // 步骤 1：排序为 左上、右上、右下、左下
        List<ai.djl.modality.cv.output.Point> ordered = PointUtils.orderPoints(landMarks);

        ai.djl.modality.cv.output.Point lt = ordered.get(0);
        ai.djl.modality.cv.output.Point rt = ordered.get(1);
        ai.djl.modality.cv.output.Point rb = ordered.get(2);
        ai.djl.modality.cv.output.Point lb = ordered.get(3);

        // 步骤 2：计算目标图像尺寸（宽、高）
        int img_crop_width = (int) Math.max(
                PointUtils.distance(lt, rt),
                PointUtils.distance(rb, lb)
        );
        int img_crop_height = (int) Math.max(
                PointUtils.distance(lt, lb),
                PointUtils.distance(rt, rb)
        );

        // 步骤 3：构造目标坐标点
        List<ai.djl.modality.cv.output.Point> dstPoints = Arrays.asList(
                new ai.djl.modality.cv.output.Point(0, 0),
                new ai.djl.modality.cv.output.Point(img_crop_width, 0),
                new ai.djl.modality.cv.output.Point(img_crop_width, img_crop_height),
                new ai.djl.modality.cv.output.Point(0, img_crop_height)
        );

        // 步骤 4：透视变换
        Mat srcPoint2f = DJLCommonUtils.toMat(ordered);
        Mat dstPoint2f = DJLCommonUtils.toMat(dstPoints);
        Mat cvMat = OpenCVUtils.perspectiveTransform(srcMat, srcPoint2f, dstPoint2f);

        // 步骤 5：转为 DJL Image + 裁剪
        Image subImg = OpenCVImageFactory.getInstance().fromImage(cvMat);
        subImg = subImg.getSubImage(0, 0, img_crop_width, img_crop_height);

        // 释放资源
        cvMat.release();
        srcPoint2f.release();
        dstPoint2f.release();
        return subImg;
    }

    /**
     * Mat To MatOfPoint
     * @param mat
     * @return
     */
    public static MatOfPoint matToMatOfPoint(Mat mat) {
        int rows = mat.rows();
        MatOfPoint matOfPoint = new MatOfPoint();

        List<Point> list = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            Point point = new Point((float) mat.get(i, 0)[0], (float) mat.get(i, 1)[0]);
            list.add(point);
        }
        matOfPoint.fromList(list);

        return matOfPoint;
    }

    /**
     * Mat To double[][] Array
     * @param mat
     * @return
     */
    public static double[][] matToDoubleArray(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();
        double[][] doubles = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                doubles[i][j] = mat.get(i, j)[0];
            }
        }
        return doubles;
    }

    /**
     * Mat To float[][] Array
     * @param mat
     * @return
     */
    public static float[][] matToFloatArray(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();

        float[][] floats = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                floats[i][j] = (float) mat.get(i, j)[0];
            }
        }

        return floats;
    }

    /**
     * Mat To byte[][] Array
     * @param mat
     * @return
     */
    public static byte[][] matToUint8Array(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();

        byte[][] bytes = new byte[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bytes[i][j] = (byte) mat.get(i, j)[0];
            }
        }

        return bytes;
    }

    /**
     * float[][] Array To Mat
     * @param arr
     * @return
     */
    public static Mat floatArrayToMat(float[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        Mat mat = new Mat(rows, cols, CvType.CV_32F);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arr[i][j]);
            }
        }

        return mat;
    }

    /**
     * byte[][] Array To Mat
     * @param arr
     * @return
     */
    public static Mat uint8ArrayToMat(byte[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        Mat mat = new Mat(rows, cols, CvType.CV_8U);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arr[i][j]);
            }
        }

        return mat;
    }


    /**
     * 将自定义 Point 列表转换为 OpenCV Point 列表
     * @param pointList 自定义 Point 列表
     * @return OpenCV Point 列表
     */
    public static List<Point> toCvPointList(List<cn.smartjavaai.common.entity.Point> pointList) {
        if (pointList == null) {
            return null;
        }
        return pointList.stream()
                .map(p -> new Point(p.getX(), p.getY()))
                .collect(Collectors.toList());
    }

    public static void drawPolygonWithText(Mat mat, List<PolygonLabel> polygonLabelList, int fontSize) {
        for (PolygonLabel polygonLabel : polygonLabelList){
            List<Point> cvPointList = toCvPointList(polygonLabel.getPoints());
            drawPolygonWithText(mat, cvPointList, polygonLabel.getText(), new Scalar(0, 255, 0), 2);
        }
    }


    /**
     * 在图像上绘制多边形（任意边数）
     *
     * @param mat    图像
     * @param points 点的列表（至少2个点）
     * @param color  颜色
     * @param thickness 线宽
     */
    public static void drawPolygonWithText(Mat mat, List<Point> points, String text, Scalar color, int thickness) {
        if (points == null || points.size() < 2) {
            return;
        }
        // 连线
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size()); // 最后一个点连回第一个
            Imgproc.line(mat, p1, p2, color, thickness);
        }
        if(StringUtils.isNotBlank(text)){
            Scalar textScalar = new Scalar(0,0,0);
            // 保证文字不超出矩形
            Imgproc.putText(mat, text, points.get(0), Imgproc.FONT_HERSHEY_SIMPLEX, 1, textScalar, thickness);
        }
    }

    public static Mat getSubImage(Mat image, int x, int y, int w, int h) {
        return image.submat(new Rect(x, y, w, h));
    }

    /**
     * 从本地路径读取图片并转为 Mat
     *
     * @param path 图片路径
     * @return Mat 对象
     */
    public static Mat loadImage(String path) {
        // 使用 imread 读取
        Mat mat = Imgcodecs.imread(path);
        // 判空，避免后续处理时报错
        if (mat.empty()) {
            throw new IllegalArgumentException("无法加载图片: " + path);
        }
        return mat;
    }



}
