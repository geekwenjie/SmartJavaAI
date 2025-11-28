package cn.smartjavaai.zeroshot.criteria;

import ai.djl.Device;
import ai.djl.huggingface.translator.ZeroShotObjectDetectionTranslatorFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.VisionLanguageInput;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloWorldTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslatorFactory;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.zeroshot.config.ZeroDetConfig;
import cn.smartjavaai.zeroshot.enums.ZeroDetModelEnum;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 零样本目标检测Criteria工厂
 * @author dwj
 */
public class ZeroDetCriteriaFactory {


    public static Criteria<VisionLanguageInput, DetectedObjects> createCriteria(ZeroDetConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        TranslatorFactory translatorFactory = null;
        if(config.getModelEnum() == ZeroDetModelEnum.OWLV2_BASE_PATCH16){
            translatorFactory = new ZeroShotObjectDetectionTranslatorFactory();
        }else if(config.getModelEnum() == ZeroDetModelEnum.YOLOV8S_WORLDV2){
            translatorFactory = new YoloWorldTranslatorFactory();
        }
        Criteria<VisionLanguageInput, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(VisionLanguageInput.class, DetectedObjects.class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                config.getModelEnum().getModelUri())
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optDevice(device)
                        .optEngine(config.getModelEnum().getEngine())
                        .optTranslatorFactory(translatorFactory)
                        .optProgress(new ProgressBar())
                        .build();

        return criteria;
    }
}
