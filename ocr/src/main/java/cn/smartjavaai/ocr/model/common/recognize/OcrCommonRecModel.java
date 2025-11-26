package cn.smartjavaai.ocr.model.common.recognize;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * OCR 通用识别模型
 * @author dwj
 */
public interface OcrCommonRecModel extends AutoCloseable{

    default void setTextDetModel(OcrCommonDetModel detModel){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default OcrCommonDetModel getTextDetModel(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setDirectionModel(OcrDirectionModel directionModel){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default OcrDirectionModel getDirectionModel(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 加载模型
     * @param config
     */
    void loadModel(OcrRecModelConfig config); // 加载模型

    /**
     * 文本识别
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default OcrInfo recognize(String imagePath, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本识别
     * @param image
     * @return
     */
    default OcrInfo recognize(Image image, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本检测
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default OcrInfo recognize(BufferedImage image, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本检测
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default OcrInfo recognize(byte[] imageData, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 识别并绘制结果
     * @param imagePath
     * @param outputPath
     */
    default void recognizeAndDraw(String imagePath, String outputPath, int fontSize, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 识别并绘制结果
     * @param image
     * @return
     */
    @Deprecated
    default OcrInfo recognizeAndDraw(Image image, int fontSize, OcrRecOptions options){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 识别并绘制结果
     * @param sourceImage
     * @return
     */
    @Deprecated
    default BufferedImage recognizeAndDraw(BufferedImage sourceImage, int fontSize, OcrRecOptions options){
        throw new UnsupportedOperationException("默认不支持该功能");
    }
    /**
     * 识别并绘制Base64结果
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default String recognizeAndDrawToBase64(byte[] imageData, int fontSize, OcrRecOptions options){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 识别并绘制结果
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default OcrInfo recognizeAndDraw(byte[] imageData, int fontSize, OcrRecOptions options){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    @Deprecated
    default List<OcrInfo> batchRecognize(List<BufferedImage> imageList, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default List<OcrInfo> batchRecognizeDJLImage(List<Image> imageList, OcrRecOptions options) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default GenericObjectPool<Predictor<Image, String>> getPool() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
