package cn.smartjavaai.common.utils;

import ai.djl.ndarray.NDArray;
import ai.djl.util.RandomUtils;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.common.entity.face.HeadPose;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author dwj
 */
public class BufferedImageUtils {

    /**
     * 拷贝图片
     * @param src
     * @return
     */
    public static BufferedImage copyBufferedImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * 对图像解码返回BGR格式矩阵数据
     *
     * @param image
     * @return
     */
    public static byte[] getMatrixBGR(BufferedImage image) {
        byte[] matrixBGR;
        if (isBGR3Byte(image)) {
            matrixBGR = (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        } else {
            // ARGB格式图像数据
            int intrgb[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            matrixBGR = new byte[image.getWidth() * image.getHeight() * 3];
            // ARGB转BGR格式
            for (int i = 0, j = 0; i < intrgb.length; ++i, j += 3) {
                matrixBGR[j] = (byte) (intrgb[i] & 0xff);
                matrixBGR[j + 1] = (byte) ((intrgb[i] >> 8) & 0xff);
                matrixBGR[j + 2] = (byte) ((intrgb[i] >> 16) & 0xff);
            }
        }
        return matrixBGR;
    }

    /**
     * 推断图像是否为BGR格式
     *
     * @return
     */
    public static boolean isBGR3Byte(BufferedImage image) {
        return equalBandOffsetWith3Byte(image, new int[]{0, 1, 2});
    }

    /**
     * @param image
     * @param bandOffset 用于推断通道顺序
     * @return
     */
    private static boolean equalBandOffsetWith3Byte(BufferedImage image, int[] bandOffset) {
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            if (image.getData().getSampleModel() instanceof ComponentSampleModel) {
                ComponentSampleModel sampleModel = (ComponentSampleModel) image.getData().getSampleModel();
                if (Arrays.equals(sampleModel.getBandOffsets(), bandOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static BufferedImage bgrToBufferedImage(byte[] data, int width, int height) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        // bgr to rgb
        byte b;
        for (int i = 0; i < data.length; i = i + 3) {
            b = data[i];
            data[i] = data[i + 2];
            data[i + 2] = b;
        }
        BufferedImage image = new BufferedImage(width, height, type);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    /**
     * 检查图像是否有效
     * @param image
     * @return
     */
    public static boolean isImageValid(BufferedImage image) {
        // 检查是否为 null 或尺寸异常（如宽高为0）
        return image != null && image.getWidth() > 0 && image.getHeight() > 0;
    }

    /**
     * 画检测框
     *
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void drawRect(BufferedImage image, int x, int y, int width, int height) {
        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            g.setColor(new Color(0, 255, 0));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawRect(x, y, width, height);
        } finally {
            g.dispose();
        }
    }

    /**
     * 保存BufferedImage图片
     * @param image
     * @param outputPath
     * @param formatName
     * @throws IOException
     */
    public static void saveBufferedImage(BufferedImage image, String outputPath, String formatName) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("BufferedImage 不能为空");
        }
        if (outputPath == null || outputPath.isEmpty()) {
            throw new IllegalArgumentException("输出路径不能为空");
        }
        if (formatName == null || formatName.isEmpty()) {
            throw new IllegalArgumentException("格式不能为空");
        }

        Path path = Paths.get(outputPath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent); // 自动创建父目录
        }
        File outFile = path.toFile();
        boolean result = ImageIO.write(image, formatName, outFile);
        if (!result) {
            throw new IOException("保存图片失败，不支持的格式: " + formatName);
        }
    }


    /**
     * 默认保存图片格式为png
     * @param image
     * @param outputPath
     * @throws IOException
     */
    public static void saveImage(BufferedImage image, String outputPath) throws IOException {
        saveBufferedImage(image, outputPath, "png");
    }



    /**
     * 画检测框(有倾斜角)
     *
     * @param image
     * @param box
     */
    public static void drawRect(BufferedImage image, NDArray box) {
        float[] points = box.toFloatArray();
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];

        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) points[2 * i];
            yPoints[i] = (int) points[2 * i + 1];
        }
        xPoints[4] = xPoints[0];
        yPoints[4] = yPoints[0];

        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            g.setColor(new Color(0, 255, 0));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawPolyline(xPoints, yPoints, 5); // xPoints, yPoints, nPoints
        } finally {
            g.dispose();
        }
    }

    /**
     * 画检测框(有倾斜角)和文本
     *
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(BufferedImage image, NDArray box, String text) {
        float[] points = box.toFloatArray();
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];

        for (int i = 0; i < 4; i++) {
            xPoints[i] = (int) points[2 * i];
            yPoints[i] = (int) points[2 * i + 1];
        }
        xPoints[4] = xPoints[0];
        yPoints[4] = yPoints[0];

        // 将绘制图像转换为Graphics2D
        Graphics2D g = (Graphics2D) image.getGraphics();
        try {
            int fontSize = 32;
            Font font = new Font("楷体", Font.PLAIN, fontSize);
            g.setFont(font);
            g.setColor(new Color(0, 0, 255));
            // 声明画笔属性 ：粗 细（单位像素）末端无修饰 折线处呈尖角
            BasicStroke bStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            g.setStroke(bStroke);
            g.drawPolyline(xPoints, yPoints, 5); // xPoints, yPoints, nPoints
            g.drawString(text, xPoints[0], yPoints[0]);
        } finally {
            g.dispose();
        }
    }


    /**
     * 显示文字
     *
     * @param image
     * @param text
     * @param x
     * @param y
     */
    public static void drawImageText(BufferedImage image, String text, int x, int y) {
        Graphics graphics = image.getGraphics();
        int fontSize = 32;
        Font font = new Font("楷体", Font.PLAIN, fontSize);
        try {
            graphics.setFont(font);
            graphics.setColor(new Color(0, 0, 255));
            int strWidth = graphics.getFontMetrics().stringWidth(text);
            graphics.drawString(text, x, y);
        } finally {
            graphics.dispose();
        }
    }


    /**
     * 画检测框(有倾斜角)和文本
     *
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(BufferedImage image, DetectionRectangle box, String text, Color color) {
        // 将绘制图像转换为Graphics2D
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        try {
            drawRectAndText(graphics, box, text, color);
        } finally {
            graphics.dispose();
        }
    }


    /**
     * 画检测框(有倾斜角)和文本
     *
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(BufferedImage image, DetectionRectangle box, String text, int fontSize) {
        Color color = new Color(255, 0, 0);
        // 将绘制图像转换为Graphics2D
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setFont(new Font("楷体", Font.PLAIN, fontSize));
        try {
            drawRectAndText(graphics, box, text, color);
        } finally {
            graphics.dispose();
        }
    }

    /**
     * 画检测框(有倾斜角)和文本
     *
     * @param graphics
     * @param box
     * @param text
     */
    public static void drawRectAndText(Graphics2D graphics, DetectionRectangle box, String text, Color color) {
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        graphics.setColor(color);// 边框颜色
        graphics.drawRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
        Graphics2DUtils.drawText(graphics, text, box.getX(), box.getY(), stroke, 4);
    }

    /**
     * 绘制检测框
     * @param sourceImage
     * @param detectionResponse
     * @throws IOException
     */
    public static void drawFaceSearchResult(BufferedImage sourceImage, DetectionResponse detectionResponse, String displayField) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            throw new IllegalArgumentException("图像无效");
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new IllegalArgumentException("无目标数据");
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
            //绘制人脸关键点
            if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                    !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
                Graphics2DUtils.drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
                //人脸查询结果
                if(detectionInfo.getFaceInfo().getFaceSearchResults() != null){
                     for (FaceSearchResult faceSearchResult : detectionInfo.getFaceInfo().getFaceSearchResults()){
                         if(StringUtils.isNotBlank(faceSearchResult.getMetadata())){
                             JsonObject metadata = GsonUtils.parseToJsonObject(faceSearchResult.getMetadata());
                             JsonElement nameElement = metadata.get("name");
                             if(metadata.has("name")){
                                 Graphics2DUtils.drawText(graphics, nameElement.getAsString(), rectangle.getX(), rectangle.getY(), stroke, 4);
                             }
                         }
                     }
                }
            }
        }
        graphics.dispose();
    }





    /**
     * 绘制检测框
     * @param sourceImage
     * @param detectionResponse
     * @throws IOException
     */
    public static void drawBoundingBoxes(BufferedImage sourceImage, DetectionResponse detectionResponse) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            throw new IllegalArgumentException("图像无效");
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            throw new IllegalArgumentException("无目标数据");
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
            //绘制人脸关键点
            if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                    !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
                Graphics2DUtils.drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
//                Graphics2DUtils.drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
                //人脸属性
                if(detectionInfo.getFaceInfo().getFaceAttribute() != null){
                    FaceAttribute faceAttribute = detectionInfo.getFaceInfo().getFaceAttribute();
                    drawFaceAttribute(faceAttribute, rectangle, graphics);
                }
            }
            //绘制目标检测信息
            if(detectionInfo.getObjectDetInfo() != null){
                String className = detectionInfo.getObjectDetInfo().getClassName();
                Graphics2DUtils.drawText(graphics, className, rectangle.getX(), rectangle.getY(), stroke, 4);
            }
        }
        graphics.dispose();
    }

    /**
     * 绘制矩形框和文字
     *
     * @param sourceImage
     * @param detectionInfo
     */
    public static void drawRectAndText(BufferedImage sourceImage, DetectionInfo detectionInfo) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            throw new IllegalArgumentException("图像无效");
        }
        if(Objects.isNull(detectionInfo)){
            throw new IllegalArgumentException("无目标数据");
        }
        Graphics2D graphics = sourceImage.createGraphics();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
        //绘制人脸关键点
        if(detectionInfo.getFaceInfo() != null && detectionInfo.getFaceInfo().getKeyPoints() != null &&
                !detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
            Graphics2DUtils.drawLandmarks(graphics, detectionInfo.getFaceInfo().getKeyPoints());
//                Graphics2DUtils.drawText(graphics, "face", rectangle.getX(), rectangle.getY(), stroke, 4);
            //人脸属性
            if(detectionInfo.getFaceInfo().getFaceAttribute() != null){
                FaceAttribute faceAttribute = detectionInfo.getFaceInfo().getFaceAttribute();
                drawFaceAttribute(faceAttribute, rectangle, graphics);
            }
        }
        //绘制目标检测信息
        if(detectionInfo.getObjectDetInfo() != null){
            String className = detectionInfo.getObjectDetInfo().getClassName();
            Graphics2DUtils.drawText(graphics, className, rectangle.getX(), rectangle.getY(), stroke, 4);
        }
        graphics.dispose();
    }


    /**
     * 绘制人脸属性
     * @param faceAttribute
     * @param rectangle
     * @param graphics
     */
    public static void drawFaceAttribute(FaceAttribute faceAttribute, DetectionRectangle rectangle, Graphics2D graphics){
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



    public static void drawPolygonWithText(BufferedImage image,List<PolygonLabel> polygonLabelList, int fontSize) {
        Font font = new Font("楷体", Font.PLAIN, fontSize);
        Stroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Color color = new Color(0, 0, 255);
        Graphics2D g = (Graphics2D) image.getGraphics();
        for (PolygonLabel polygonLabel : polygonLabelList){
            drawPolygonWithText(g, polygonLabel.getPoints(), polygonLabel.getText(), font, color, stroke);
        }
    }

    /**
     * 绘制多边形及文字
     * @param g         Graphics2D
     * @param points    多边形顶点
     * @param text      绘制的文字（可为空）
     * @param font      字体
     * @param color     颜色
     * @param stroke    画笔样式
     */
    public static void drawPolygonWithText(Graphics2D g, List<Point> points,
                                           String text, int fontSize) {
        Font font = new Font("楷体", Font.PLAIN, fontSize);
        Stroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Color color = new Color(0, 0, 255);
        drawPolygonWithText(g, points, text, font, color, stroke);
    }


    /**
     * 绘制多边形及文字
     * @param g         Graphics2D
     * @param points    多边形顶点
     * @param text      绘制的文字（可为空）
     * @param font      字体
     * @param color     颜色
     * @param stroke    画笔样式
     */
    public static void drawPolygonWithText(Graphics2D g, List<Point> points,
                                           String text, Font font,
                                           Color color, Stroke stroke) {
        if (points == null || points.size() < 3) {
            return;
        }

        int[] xPoints = points.stream().mapToInt(p -> (int) p.getX()).toArray();
        int[] yPoints = points.stream().mapToInt(p -> (int) p.getY()).toArray();

        g.setFont(font);
        g.setColor(color);
        g.setStroke(stroke);
        // 绘制多边形
        g.drawPolygon(xPoints, yPoints, points.size());
        // 绘制文字（默认放在第一个点）
        if (text != null && !text.isEmpty()) {
            g.drawString(text, xPoints[0], yPoints[0]);
        }
    }

    /**
     * 绘制关键点
     * @param graphics
     * @param points
     * @param color
     */
    public static void drawKeyPoints(Graphics2D graphics, List<Point> points, Color color){
        if(points == null || points.isEmpty()){
            return;
        }
        for (Point point : points){
            //绘制关键点
            graphics.setColor(color);
            graphics.drawRect((int)point.getX(), (int)point.getY(), 2, 2);
        }
    }


    /**
     * 绘制检测框
     * @param sourceImage
     * @param detectionResponse
     * @throws IOException
     */
    public static void drawFaceSearchResult(Graphics2D graphics, DetectionInfo detectionInfo, String text) {
        graphics.setColor(Color.RED);// 边框颜色
        graphics.setStroke(new BasicStroke(2));   // 线宽2像素
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        int stroke = 2;
        DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
        graphics.setColor(Color.RED);// 边框颜色
        graphics.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),  rectangle.getHeight());
        //绘制人脸关键点
        //人脸查询结果
        if(detectionInfo.getFaceInfo().getFaceSearchResults() != null){
            for (FaceSearchResult faceSearchResult : detectionInfo.getFaceInfo().getFaceSearchResults()){
                if(StringUtils.isNotBlank(faceSearchResult.getMetadata())){
                    JsonObject metadata = GsonUtils.parseToJsonObject(faceSearchResult.getMetadata());
                    JsonElement nameElement = metadata.get("name");
                    if(metadata.has("name")){
                        Graphics2DUtils.drawText(graphics, nameElement.getAsString(), rectangle.getX(), rectangle.getY(), stroke, 4);
                    }
                }
            }
        }
        graphics.dispose();
    }

}
