package cn.smartjavaai.objectdetection.criteria;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;

import java.util.Objects;

/**
 * DJL提供的Criteria 构建器
 * @author dwj
 * @date 2025/5/14
 */
public class DJLModelCriteriaBuilder implements CriteriaBuilderStrategy {

    private static final String DJL_MODEL_PREFIX = "djl://";

    @Override
    public Criteria<Image, DetectedObjects> buildCriteria(DetectorModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optArgument("threshold", config.getThreshold() > 0 ? config.getThreshold() : DetectorConstant.DEFAULT_THRESHOLD)
                .optModelUrls(DJL_MODEL_PREFIX + config.getModelEnum().getModelUri())
                .optDevice(device)
                //.optOption("ortDevice", "TensorRT")
                .optProgress(new ProgressBar())
                .build();
        return criteria;
    }
}
