package cn.smartjavaai.pose.model;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Joints;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;
import cn.smartjavaai.pose.config.PoseModelConfig;

/**
 * 姿态估计模型
 * @author dwj
 */
public interface PoseModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(PoseModelConfig config);

    /**
     * 姿态估计
     * @param image
     * @return
     */
    default R<Joints[]> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 姿态估计并绘制
     * @param image
     * @return
     */
    default Image detectAndDraw(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 姿态估计并绘制
     * @param imagePath
     * @param outputPath
     * @return
     */
    default R<Joints[]> detectAndDraw(String imagePath, String outputPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
