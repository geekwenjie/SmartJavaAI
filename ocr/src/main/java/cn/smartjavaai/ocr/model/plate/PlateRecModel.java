package cn.smartjavaai.ocr.model.plate;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.config.PlateRecModelConfig;
import cn.smartjavaai.ocr.entity.PlateInfo;
import cn.smartjavaai.ocr.entity.PlateResult;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 车牌识别模型
 * @author dwj
 */
public interface PlateRecModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(PlateRecModelConfig config); // 加载模型

    /**
     * 车牌识别
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> recognize(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌识别
     * @param inputStream
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> recognize(InputStream inputStream) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌识别
     * @param base64Image
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> recognizeBase64(String base64Image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌识别
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> recognize(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌识别
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> recognize(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 车牌识别
     * @param image DJL Image
     * @return
     */
    default R<List<PlateInfo>> recognize(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 识别裁剪后的图片
     * @return
     */
    default PlateResult recognizeCropped(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 检测并绘制结果
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    default R<Void> recognizeAndDraw(String imagePath, String outputPath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param sourceImage
     * @return
     */
    @Deprecated
    default R<BufferedImage> recognizeAndDraw(BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<Image> recognizeAndDraw(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default GenericObjectPool<Predictor<Image, PlateResult>> getPool() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
