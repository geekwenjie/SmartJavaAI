package cn.smartjavaai.instanceseg.model;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.CategoryMask;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.instanceseg.config.InstanceSegModelConfig;

import java.awt.image.BufferedImage;

/**
 * 实例分割模型
 * @author dwj
 */
public interface InstanceSegModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(InstanceSegModelConfig config);

    /**
     * 实例分割
     * @param image
     * @return
     */
    default R<DetectionResponse> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default DetectedObjects detectCore(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<DetectionResponse> detectAndDraw(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<DetectionResponse> detectAndDraw(String imagePath, String outputPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
