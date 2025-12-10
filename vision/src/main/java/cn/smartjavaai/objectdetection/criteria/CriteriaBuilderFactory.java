package cn.smartjavaai.objectdetection.criteria;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import org.apache.commons.lang3.StringUtils;

/**
 * Criteria构建工厂
 * @author dwj
 * @date 2025/5/14
 */
public class CriteriaBuilderFactory {

    public static Criteria<Image, DetectedObjects> createCriteria(DetectorModelConfig config) {
        //以下模型modelPath不允许为空
        if(config.getModelEnum() == DetectorModelEnum.YOLOV8_OFFICIAL_ONNX ||
                config.getModelEnum() == DetectorModelEnum.YOLOV12_OFFICIAL_ONNX ||
                config.getModelEnum() == DetectorModelEnum.YOLOV8_CUSTOM_ONNX ||
                config.getModelEnum() == DetectorModelEnum.YOLOV12_CUSTOM_ONNX ||
                config.getModelEnum() == DetectorModelEnum.TENSORFLOW2_OFFICIAL){
            if(StringUtils.isBlank(config.getModelPath())){
                throw new DetectionException("modelPath is null");
            }
        }
        switch (config.getModelEnum()) {
            case YOLOV8_OFFICIAL_ONNX:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV11_OFFICIAL_ONNX:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV12_OFFICIAL_ONNX:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV8_CUSTOM_ONNX:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV11_CUSTOM_ONNX:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case YOLOV12_CUSTOM_ONNX:
                return new YoloCriteriaBuilder().buildCriteria(config);
            case TENSORFLOW2_OFFICIAL:
                return new Tensorflow2CriteriaBuilder().buildCriteria(config);
            // 其他类型
            default:
                return new DJLModelCriteriaBuilder().buildCriteria(config);
        }
    }

}
