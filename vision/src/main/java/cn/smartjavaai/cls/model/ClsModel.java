package cn.smartjavaai.cls.model;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.cls.config.ClsModelConfig;
import cn.smartjavaai.common.entity.R;

/**
 * 图像分类模型
 * @author dwj
 */
public interface ClsModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(ClsModelConfig config);

    /**
     * 分类
     * @param image
     * @return
     */
    default R<Classifications> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 分类
     * @param imagePath
     * @return
     */
    default R<Classifications> detect(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
