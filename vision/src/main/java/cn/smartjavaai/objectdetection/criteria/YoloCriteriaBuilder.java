package cn.smartjavaai.objectdetection.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV8TranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * YOLO模型Criteria 构建器
 * @author dwj
 * @date 2025/5/14
 */
public class YoloCriteriaBuilder implements CriteriaBuilderStrategy {
    @Override
    public Criteria<Image, DetectedObjects> buildCriteria(DetectorModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }

        Map<String, Object> customParams = getDefaultConfig();
        // 合并用户自定义参数（如有重复，覆盖默认默认值）
        if (config.getCustomParams() != null) {
            customParams.putAll(config.getCustomParams());
        }

        Criteria.Builder criteriaBuilder = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                //.optModelUrls("/Users/wenjie/Documents/develop/face_model/yolo")
                .optModelPath(Paths.get(config.getModelPath()))
                .optEngine("OnnxRuntime")
                //.optOption("ortDevice", "TensorRT")
                .optArguments(customParams)
                .optDevice(device)
                .optTranslatorFactory(new YoloV8TranslatorFactory())
                .optProgress(new ProgressBar())
                .optArgument("threshold", config.getThreshold() > 0 ? config.getThreshold() : DetectorConstant.DEFAULT_THRESHOLD);
        if(config.getMaxBox() >  0){
            criteriaBuilder.optArgument("maxBox", config.getMaxBox());
        }
        Criteria<Image, DetectedObjects> criteria = criteriaBuilder.build();
        return criteria;
    }

    public Map<String, Object> getDefaultConfig(){
        Map<String, Object> arguments = new HashMap<>();
        // 添加默认参数
        arguments.put("width", 640);
        arguments.put("height", 640);
        arguments.put("resize", true);
        arguments.put("toTensor", true);
        arguments.put("applyRatio", true);
        return arguments;
    }
}
