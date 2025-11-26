package cn.smartjavaai.objectdetection.model.person;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.obb.config.ObbDetModelConfig;
import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;

/**
 * 行人检测模型
 * @author dwj
 */
public interface PersonDetModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(PersonDetModelConfig config);

    /**
     * 旋转框
     * @param image
     * @return
     */
    default R<DetectionResponse> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 行人检测 核心方法
     * @param image
     * @return
     */
    default DetectedObjects detectCore(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 行人检测并绘制
     * @param image
     * @return
     */
    default R<DetectionResponse> detectAndDraw(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 行人检测并绘制
     * @param imagePath
     * @param outputPath
     * @return
     */
    default R<DetectionResponse> detectAndDraw(String imagePath, String outputPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
