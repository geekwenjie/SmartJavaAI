package cn.smartjavaai.ocr.model.plate;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.PlateInfo;
import cn.smartjavaai.common.entity.R;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 车牌检测模型
 * @author dwj
 */
public interface PlateDetModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(PlateDetModelConfig config); // 加载模型

    /**
     * 车牌检测
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌检测
     * @param inputStream
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> detect(InputStream inputStream) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌检测
     * @param base64Image
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> detectBase64(String base64Image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌检测
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> detect(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌检测
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default R<List<PlateInfo>> detect(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 车牌检测
     * @param image DJL Image
     * @return
     */
    default DetectedObjects detectCore(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 车牌检测
     * @param image DJL Image
     * @return
     */
    default R<List<PlateInfo>> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 检测并绘制结果
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    default R<Void> detectAndDraw(String imagePath, String outputPath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param sourceImage
     * @return
     */
    @Deprecated
    default R<BufferedImage> detectAndDraw(BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param image
     * @return
     */
    default Image detectAndDraw(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default GenericObjectPool<Predictor<Image, DetectedObjects>> getPool(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
