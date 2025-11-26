package cn.smartjavaai.semseg.model;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.CategoryMask;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.semseg.config.SemSegModelConfig;

/**
 * 语义分割模型
 * @author dwj
 */
public interface SemSegModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(SemSegModelConfig config);

    /**
     * 语义分割
     * @param image
     * @return
     */
    default R<CategoryMask> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<CategoryMask> detectAndDraw(String imagePath, String outputPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default Image detectAndDraw(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
