package cn.smartjavaai.ocr.model.common.direction;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDManager;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.entity.DirectionInfo;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * OCR 文本方向分类模型
 * @author dwj
 */
public interface OcrDirectionModel extends AutoCloseable{

    default void setTextDetModel(OcrCommonDetModel detModel){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default OcrCommonDetModel getTextDetModel(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 加载模型
     * @param config
     */
    void loadModel(DirectionModelConfig config); // 加载模型

    /**
     * 文本方向检测
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default List<OcrItem> detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本方向检测
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default List<OcrItem> detect(BufferedImage image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本方向检测
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default List<OcrItem> detect(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本方向检测
     * @param image
     * @return
     */
    default List<OcrItem> detect(Image image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本方向检测（基于检测结果）
     * @param boxList
     * @param srcMat
     * @param manager
     * @return
     */
    default List<OcrItem> detect(List<OcrBox> boxList, Mat srcMat) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default List<List<OcrItem>> batchDetect(List<List<OcrBox>> boxList, List<Mat> srcMatList) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    default void detectAndDraw(String imagePath, String outputPath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param sourceImage
     * @return
     */
    @Deprecated
    default BufferedImage detectAndDraw(BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制结果
     * @param sourceImage
     * @return
     */
    default Image detectAndDraw(Image sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default GenericObjectPool<Predictor<Image, DirectionInfo>> getPool() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
