package cn.smartjavaai.common.cv;

import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.util.Utils;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 图片处理工厂类
 * @author dwj
 */
public class SmartImageFactory {

    public enum Engine {
        BUFFEREDIMAGE,
        OPENCV
    }

    private static volatile Engine currentEngine = Engine.BUFFEREDIMAGE;
    private static volatile SmartImageFactory instance;

    public static synchronized void setEngine(Engine engine) {
        if (engine == null || engine == currentEngine) {
            return;
        }
        currentEngine = engine;
        // 只在切换时注册全局
        switch (currentEngine) {
            case OPENCV:
                ImageFactory.setImageFactory(new OpenCVImageFactory());
                break;
            case BUFFEREDIMAGE:
            default:
                ImageFactory.setImageFactory(new BufferedImageFactory());
        }
    }

    public static synchronized SmartImageFactory getInstance() {
        if (instance == null) {
            instance = new SmartImageFactory();
            // 初始化全局 Engine
            switch (currentEngine) {
                case OPENCV:
                    ImageFactory.setImageFactory(new OpenCVImageFactory());
                    break;
                case BUFFEREDIMAGE:
                default:
                    ImageFactory.setImageFactory(new BufferedImageFactory());
            }
        }
        return instance;
    }


    public Image fromBufferedImage(BufferedImage sourceImage){
        if (sourceImage == null) {
            throw new IllegalArgumentException("BufferedImage 不能为空");
        }
        Image image = null;
        switch (currentEngine) {
            case BUFFEREDIMAGE:
                image = ImageFactory.getInstance().fromImage(sourceImage);
                break;
            case OPENCV:
                // 先转 Mat
                Mat mat = OpenCVUtils.image2Mat(sourceImage);
                image = ImageFactory.getInstance().fromImage(mat);
                break;
            default:
                throw new IllegalStateException("未知 Engine: " + currentEngine);
        }
        return image;
    }

    public Image fromMat(Mat mat){
        if (mat == null) {
            throw new IllegalArgumentException("mat 不能为空");
        }
        Image image = null;
        switch (currentEngine) {
            case OPENCV:
                image = ImageFactory.getInstance().fromImage(mat);
                break;
            case BUFFEREDIMAGE:
                // 先转 Mat
                BufferedImage sourceImage = OpenCVUtils.mat2Image(mat);
                image = ImageFactory.getInstance().fromImage(sourceImage);
                break;
            default:
                throw new IllegalStateException("未知 Engine: " + currentEngine);
        }
        return image;
    }

    public Image fromBase64(String base64Image) throws IOException {
        return ImageFactory.getInstance().fromUrl(base64Image);
    }

    public Image fromBytes(byte[] imageData) throws IOException {
        return ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(imageData));
    }

    public Image fromFile(File file) throws IOException {
        return ImageFactory.getInstance().fromFile(file.toPath());
    }

    public Image fromFile(Path path) throws IOException {
        return ImageFactory.getInstance().fromFile(path);
    }

    public Image fromFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath 不能为空");
        }
        return fromFile(Paths.get(filePath));
    }

    public Image fromPixels(int[] pixels, int width, int height){
        return ImageFactory.getInstance().fromPixels(pixels, width, height);
    }

    public Image fromInputStream(InputStream inputStream) throws IOException {
        return ImageFactory.getInstance().fromInputStream(inputStream);
    }

    public Image fromUrl(URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL 不能为空");
        }
        try (InputStream inputStream = url.openStream()) {
            return ImageFactory.getInstance().fromInputStream(inputStream);
        }
    }

    public Image fromUrl(String urlString) throws IOException {
        return this.fromUrl(new URL(urlString));
    }



}
