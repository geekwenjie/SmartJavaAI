package cn.smartjavaai.action.criteria;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.action.model.CommonActionTranslator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.action.exception.ActionException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动作识别模型工厂
 * @author dwj
 */
public class ActionRecCriteriaFactory {


    /**
     * 创建动作识别Criteria
     * @param config
     * @return
     */
    public static Criteria<Image, Classifications> createCriteria(ActionRecModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Translator<Image, Classifications> translator = getTranslator(config);
        if(StringUtils.isBlank(config.getModelEnum().getModelUrl())){
            //检查模型路径
            if (StringUtils.isBlank(config.getModelPath())){
                throw new ActionException("请指定模型路径");
            }
        }
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : config.getModelEnum().getModelUrl())
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optTranslator(translator)
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .optEngine(config.getModelEnum().getEngine())
                        .build();
        return criteria;
    }

    /**
     * 获取动作识别Translator
     * @param config
     * @return
     */
    public static Translator<Image, Classifications> getTranslator(ActionRecModelConfig config) {
        Translator<Image, Classifications> translator = null;
        if(config.getModelEnum() == ActionRecModelEnum.INCEPTIONV1_KINETICS400_ONNX
                || config.getModelEnum() == ActionRecModelEnum.INCEPTIONV3_KINETICS400_ONNX
                || config.getModelEnum() == ActionRecModelEnum.INCEPTIONV3_KINETICS400_ONNX){
            translator =new CommonActionTranslator(config.getModelEnum().getInputWidth(), config.getModelEnum().getInputHeight());
        }
        return translator;
    }
}
