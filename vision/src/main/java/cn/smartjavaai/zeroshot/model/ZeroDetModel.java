package cn.smartjavaai.zeroshot.model;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.VisionLanguageInput;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.zeroshot.config.ZeroDetConfig;

/**
 * 零样本目标检测模型
 * @author dwj
 */

public interface ZeroDetModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(ZeroDetConfig config);

    /**
     * 零样本目标检测
     * @param image
     * @return
     */
    default R<DetectionResponse> detect(Image image, String[] candidates){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default DetectedObjects detectCore(VisionLanguageInput input){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<DetectionResponse> detectAndDraw(Image image, String[] candidates){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<DetectionResponse> detectAndDraw(String[] candidates, String imagePath, String outputPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
