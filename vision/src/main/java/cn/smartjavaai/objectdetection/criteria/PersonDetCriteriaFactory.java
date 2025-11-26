package cn.smartjavaai.objectdetection.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.obb.config.ObbDetModelConfig;
import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.obb.enums.ObbDetModelEnum;
import cn.smartjavaai.obb.exception.ObbDetException;
import cn.smartjavaai.obb.translator.YoloV11OddTranslator;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;
import cn.smartjavaai.objectdetection.enums.PersonDetectorModelEnum;
import cn.smartjavaai.objectdetection.translator.YoloV8PersonDetTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 行人检测Criteria创建工厂
 * @author dwj
 */
public class PersonDetCriteriaFactory {


    /**
     * 创建行人检测Criteria
     * @param config
     * @return
     */
    public static Criteria<Image, DetectedObjects> createCriteria(PersonDetModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Translator<Image, DetectedObjects> translator = getTranslator(config);
        //检查模型路径
        if (StringUtils.isBlank(config.getModelPath())){
            throw new ObbDetException("请指定模型路径");
        }
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelPath(Paths.get(config.getModelPath()))
                        .optTranslator(translator)
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .optEngine(config.getModelEnum().getEngine())
                        .build();
        return criteria;
    }


    /**
     * 获取行人检测Translator
     * @param config
     * @return
     */
    public static Translator<Image, DetectedObjects> getTranslator(PersonDetModelConfig config) {
        Translator<Image, DetectedObjects> translator = null;
        if (config.getModelEnum() == PersonDetectorModelEnum.YOLOV8_PERSON){
            translator = YoloV8PersonDetTranslator.builder()
                    .setImageSize(config.getModelEnum().getInputSize(), config.getModelEnum().getInputSize())
                    .optThreshold(config.getThreshold() > 0 ? config.getThreshold() : 0.5f)
                    .optNmsThreshold(0.45f).build();
        }
        return translator;
    }

}
