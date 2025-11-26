package cn.smartjavaai.ocr.model.table;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.TableStructureConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.entity.TableStructureResult;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 表格结构识别模型
 * @author dwj
 */
public interface TableStructureModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(TableStructureConfig config);


    /**
     * 表格结构检测
     * @param image
     * @return
     */
    @Deprecated
    default R<TableStructureResult> detect(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表格结构检测
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<TableStructureResult> detect(String imagePath) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表格结构检测
     * @param imageData 图片字节数组
     * @return
     */
    @Deprecated
    default R<TableStructureResult> detect(byte[] imageData) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表格结构检测
     * @param image DJL Image
     * @return
     */
    default R<TableStructureResult> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default GenericObjectPool<Predictor<Image, TableStructureResult>> getPool() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
