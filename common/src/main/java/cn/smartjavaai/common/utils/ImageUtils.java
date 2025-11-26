package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.CategoryMask;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.util.RandomUtils;
import cn.hutool.core.codec.Base64;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.PolygonLabel;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
//import java.awt.image.ColorConvertOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片处理工具类
 */
public class ImageUtils {


    /**
     * 保存DJL图片
     *
     * @param img
     * @param name
     * @param path
     */
    public static void save(Image img, String name, String path) {
        Path outputDir = Paths.get(path);
        Path imagePath = outputDir.resolve(name);
        // OpenJDK 不能保存 jpg 图片的 alpha channel
        try {
            img.save(Files.newOutputStream(imagePath), "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取图片矩阵BGR
     *
     * @param img
     * @return
     */
    public static byte[] getMatrixBGR(Image img) {
        if (img.getWrappedImage() instanceof BufferedImage){
            return BufferedImageUtils.getMatrixBGR((BufferedImage)img.getWrappedImage());
        }else if (img.getWrappedImage() instanceof Mat){
            return OpenCVUtils.getMatrixBGR((Mat)img.getWrappedImage());
        }else {
            throw new RuntimeException("不支持的图片类型");
        }
    }


    /**
     * 保存图片,含检测框
     *
     * @param img
     * @param detection
     * @param name
     * @param path
     * @throws IOException
     */
    public static void saveBoundingBoxImage(
            Image img, DetectedObjects detection, String name, String path) throws IOException {
        // Make image copy with alpha channel because original image was jpg
        img.drawBoundingBoxes(detection);
        Path outputDir = Paths.get(path);
        Files.createDirectories(outputDir);
        Path imagePath = outputDir.resolve(name);
        // OpenJDK can't save jpg with alpha channel
        img.save(Files.newOutputStream(imagePath), "png");
    }



    /**
     * 计算左上角，右下角坐标 （x0,y0,x1,y1）
     * Get absolute coordinations
     *
     * @param rect
     * @param width
     * @param height
     * @return
     */
    public static int[] rectXYXY(ai.djl.modality.cv.output.Rectangle rect, int width, int height) {
        int left = Math.max((int) (width * rect.getX()), 0);
        int top = Math.max((int) (height * rect.getY()), 0);
        int right = Math.min((int) (width * (rect.getX() + rect.getWidth())), width - 1);
        int bottom = Math.min((int) (height * (rect.getY() + rect.getHeight())), height - 1);
        return new int[] {left, top, right, bottom};
    }

    /**
     * 列出文件夹下的所有图片文件
     * List all image files under the folder
     *
     * @param folderPath
     * @return
     */
    public static List<File> listImageFiles(String folderPath) {
        File folder = new File(folderPath);
        List<File> imageFiles = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return imageFiles;
            }
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                            name.endsWith(".png") || name.endsWith(".bmp") ||
                            name.endsWith(".gif") || name.endsWith(".tiff") ||
                            name.endsWith(".webp")) {
                        imageFiles.add(file);
                    }
                }
            }
        }
        return imageFiles;
    }

    /**
     * 读取指定目录下所有图片，返回 List<Image>（DJL 格式）
     *
     * @param folderPath 图片文件夹路径
     * @return List<Image>
     * @throws IOException
     */
    public static List<Image> readImagesFromFolder(String folderPath) throws IOException {
        File folder = new File(folderPath);
        List<Image> imageList = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return imageList;
            }
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                            name.endsWith(".png") || name.endsWith(".bmp") ||
                            name.endsWith(".gif") || name.endsWith(".tiff") ||
                            name.endsWith(".webp")) {

                        Image img = SmartImageFactory.getInstance().fromInputStream(Files.newInputStream(file.toPath()));
                        imageList.add(img);
                    }
                }
            }
        }
        return imageList;
    }

    /**
     * 判断所有图片尺寸是否一致
     *
     * @param images 图片列表
     */
    public static boolean isAllImageSizeEqual(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return true; // 空集合视为一致
        }
        int width = images.get(0).getWidth();
        int height = images.get(0).getHeight();
        for (Image img : images) {
            if (img.getWidth() != width || img.getHeight() != height) {
                return false;
            }
        }
        return true;
    }



    /**
     * 拷贝图片
     * @param src
     * @return
     */
    public static Image copy(Image src) {
       Object srcData = src.getWrappedImage();
       //当图片是BufferedImage，DJL的duplicate会有问题
       if (srcData instanceof BufferedImage) {
           return SmartImageFactory.getInstance().fromBufferedImage(BufferedImageUtils.copyBufferedImage((BufferedImage) srcData));
       }else{
           return src.duplicate();
       }
    }

    /**
     * 为不同分类生成不同颜色
     * @param background
     * @param opacity
     * @param classes
     * @return
     */
    public static int[] generateColors(int background, int opacity, List<String> classes) {
        int[] colors = new int[classes.size()];
        colors[0] = background;
        for (int i = 1; i < classes.size(); i++) {
            int red = RandomUtils.nextInt(256);
            int green = RandomUtils.nextInt(256);
            int blue = RandomUtils.nextInt(256);
            colors[i] = opacity << 24 | red << 16 | green << 8 | blue;
        }
        return colors;
    }

    /**
     * 生成不同颜色遮罩
     * @param colors
     * @param mask
     * @return
     */
    public static Image getColorOverlay(int[] colors,int[][] mask) {
        int height = mask.length;
        int width = mask[0].length;
        int[] pixels = new int[width * height];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int index = mask[h][w];
                pixels[h * width + w] = colors[index];
            }
        }
        return SmartImageFactory.getInstance().fromPixels(pixels, width, height);
    }

    /**
     * 绘制遮罩
     * @param categoryMask
     * @param image
     * @param opacity
     * @param background
     */
    public static void drawMask(CategoryMask categoryMask, Image image, int opacity, int background) {
        int[] colors = generateColors(background, opacity, categoryMask.getClasses());
        Image maskImage = getColorOverlay(colors, categoryMask.getMask());
        image.drawImage(maskImage, true);
    }

    /**
     * 保存 Image 到指定路径，格式根据后缀自动推断
     */
    public static void save(Image image, Path path) throws IOException {
        String fileName = path.getFileName().toString().toLowerCase();
        String format = "png"; // 默认 png
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            format = "jpg";
        } else if (fileName.endsWith(".bmp")) {
            format = "bmp";
        } else if (fileName.endsWith(".webp")) {
            format = "webp";
        }
        Files.createDirectories(path.getParent());
        try (OutputStream os = Files.newOutputStream(path)) {
            image.save(os, format);
        }
    }

    /**
     * 保存 Image 到指定路径，格式根据后缀自动推断
     */
    public static void save(Image image, Path path, String format) throws IOException {
        Files.createDirectories(path.getParent());
        try (OutputStream os = Files.newOutputStream(path)) {
            image.save(os, format);
        }
    }

    /**
     * 保存 Image 到指定路径
     */
    public static void save(Image image, String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        Files.createDirectories(path.getParent());
        try (OutputStream os = Files.newOutputStream(path)) {
            image.save(os, "png");
        }
    }

    /**
     * 转换为 BufferedImage
     */
    public static BufferedImage toBufferedImage(Image image) {
        Object wrapped = image.getWrappedImage();
        if (wrapped instanceof BufferedImage) {
            return (BufferedImage) wrapped;
        } else if (wrapped instanceof Mat) {
            Mat mat = (Mat) wrapped;
            return OpenCVUtils.mat2Image(mat);
        } else {
            throw new IllegalArgumentException("Unsupported wrapped image type: " + wrapped.getClass());
        }
    }

    /**
     * 转换为 Mat
     */
    public static Mat toMat(Image image) {
        Object wrapped = image.getWrappedImage();
        if (wrapped instanceof BufferedImage) {
            return OpenCVUtils.image2Mat((BufferedImage) wrapped);
        } else if (wrapped instanceof Mat) {
            return (Mat) wrapped;
        } else {
            throw new IllegalArgumentException("Unsupported wrapped image type: " + wrapped.getClass());
        }
    }

    /**
     * Image 转 byte[] （默认 png 格式）
     */
    public static byte[] toBytes(Image image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            image.save(baos, format);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert Image to byte[]", e);
        }
    }

    /**
     * 保存 Image 到 OutputStream
     *
     * @param image   图像对象
     * @param os      输出流（需要调用方负责关闭）
     * @param format  保存格式（png/jpg/webp）
     */
    public static void toOutputStream(Image image, OutputStream os, String format) {
        try {
            image.save(os, format);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write image to OutputStream", e);
        }
    }

    /**
     * 转换 Image 为 Base64 字符串
     *
     * @param image  图像对象
     * @param format 输出格式（png/jpg/webp）
     * @return Base64 编码的字符串
     */
    public static String toBase64(Image image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            image.save(baos, format);
            return Base64.encode(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to Base64", e);
        }
    }

    /**
     * 释放 OpenCV Mat
     * @param image
     */
    public static void releaseOpenCVMat(Image image){
        if (image != null && image.getWrappedImage() instanceof Mat){
            ((Mat)image.getWrappedImage()).release();
        }
    }

    /**
     * 绘制检测结果
     * @param sourceImage
     * @param detectionResponse
     * @return
     */
    public static Image drawBoundingBoxes(Image sourceImage, DetectionResponse detectionResponse){
        Object srcData = sourceImage.getWrappedImage();
        if (srcData instanceof BufferedImage) {
            BufferedImage copyBufferedImage = BufferedImageUtils.copyBufferedImage((BufferedImage) srcData);
            BufferedImageUtils.drawBoundingBoxes(copyBufferedImage, detectionResponse);
            return SmartImageFactory.getInstance().fromBufferedImage(copyBufferedImage);
        }else if (srcData instanceof Mat) {
            Mat srcMat = ((Mat) srcData).clone();
            OpenCVUtils.drawBoundingBoxes(srcMat, detectionResponse);
            return SmartImageFactory.getInstance().fromMat(srcMat);
        }else {
            throw new IllegalArgumentException("Unsupported wrapped image type: " + srcData.getClass());
        }
    }

    /**
     * 逆时针旋转图片
     *
     * @param image
     * @param times
     * @return
     */
    public static Image rotateImg(Image image, int times) {
        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), times);
            return OpenCVImageFactory.getInstance().fromNDArray(rotated);
        }
    }

    /**
     * 图片旋转
     *
     * @param manager
     * @param image
     * @return
     */
    public static Image rotateImg(NDManager manager, Image image) {
        NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
        return ImageFactory.getInstance().fromNDArray(rotated);
    }


    public static void drawPolygonWithText(Image image, List<PolygonLabel> polygonLabelList, int fontSize) {
        Object srcData = image.getWrappedImage();
        if (srcData instanceof BufferedImage) {
            BufferedImageUtils.drawPolygonWithText((BufferedImage) srcData, polygonLabelList, fontSize);
        }else if (srcData instanceof Mat) {
            Mat srcMat = (Mat) srcData;
            OpenCVUtils.drawPolygonWithText(srcMat, polygonLabelList, fontSize);
        }else {
            throw new IllegalArgumentException("Unsupported wrapped image type: " + srcData.getClass());
        }
    }



    /**
     * 绘制矩形框
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(Image image, DetectionRectangle box, String text) {
        Object srcData = image.getWrappedImage();
        if (srcData instanceof BufferedImage) {
            BufferedImageUtils.drawRectAndText((BufferedImage) srcData, box, text,12);
        }else if (srcData instanceof Mat) {
            Mat srcMat = (Mat) srcData;
            OpenCVUtils.drawRectAndText(srcMat, box, text, 0.5);
        }else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 绘制矩形框
     * @param image
     * @param box
     * @param text
     */
    public static void drawRectAndText(Image image, DetectionRectangle box, String text, double fontSize) {
        Object srcData = image.getWrappedImage();
        if (srcData instanceof BufferedImage) {
            BufferedImageUtils.drawRectAndText((BufferedImage) srcData, box, text, (int)fontSize);
        }else if (srcData instanceof Mat) {
            Mat srcMat = (Mat) srcData;
            OpenCVUtils.drawRectAndText(srcMat, box, text, fontSize);
        }else {
            throw new IllegalArgumentException();
        }
    }

    public static void drawRectAndText(Image image, DetectionInfo detectionInfo){
        Object srcData = image.getWrappedImage();
        if (srcData instanceof BufferedImage) {
            BufferedImageUtils.drawRectAndText((BufferedImage) srcData, detectionInfo);
        }else if (srcData instanceof Mat) {
            Mat srcMat = (Mat) srcData;
            OpenCVUtils.drawRectAndText(srcMat, detectionInfo);
        }else {
            throw new IllegalArgumentException();
        }
    }

    public static void drawRectAndText(Image image, List<DetectionInfo> detectionInfoList){
        for(DetectionInfo detectionInfo : detectionInfoList){
            drawRectAndText(image, detectionInfo);
        }
    }
}
