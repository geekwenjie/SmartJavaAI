package cn.smartjavaai.action.model;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * 人类动作识别模型
 * @author dwj
 * @date 2025/8/12
 */
public interface ActionRecModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(ActionRecModelConfig config);

    /**
     * 动作检测
     * @param image
     * @return
     */
    default R<Classifications> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
