package cn.smartjavaai.objectdetection.criteria;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;

/**
 * 模型加载策略接口，用于根据不同模型类型构建对应的 DJL Criteria 实例
 * @author dwj
 * @date 2025/5/14
 */
public interface CriteriaBuilderStrategy {

    /**
     * 根据模型类型构建对应的 DJL Criteria 实例
     * @param config
     * @return
     */
    Criteria<Image, DetectedObjects> buildCriteria(DetectorModelConfig config);

}
