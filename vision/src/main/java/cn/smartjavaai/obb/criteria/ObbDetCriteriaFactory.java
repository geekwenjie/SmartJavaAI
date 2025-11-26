package cn.smartjavaai.obb.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.obb.config.ObbDetModelConfig;
import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.obb.enums.ObbDetModelEnum;
import cn.smartjavaai.obb.exception.ObbDetException;
import cn.smartjavaai.obb.translator.YoloV11OddTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * 旋转框Criteria工厂
 * @author dwj
 */
public class ObbDetCriteriaFactory {


    /**
     * 创建旋转框检测Criteria
     * @param config
     * @return
     */
    public static Criteria<Image, ObbResult> createCriteria(ObbDetModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Translator<Image, ObbResult> translator = getTranslator(config);
        //检查模型路径
        if (StringUtils.isBlank(config.getModelPath())){
            throw new ObbDetException("请指定模型路径");
        }
        Criteria<Image, ObbResult> criteria =
                Criteria.builder()
                        .setTypes(Image.class, ObbResult.class)
                        .optModelPath(Paths.get(config.getModelPath()))
                        .optTranslator(translator)
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .optEngine(config.getModelEnum().getEngine())
                        .build();
        return criteria;
    }


    /**
     * 获取旋转框检测Translator
     * @param config
     * @return
     */
    public static Translator<Image, ObbResult> getTranslator(ObbDetModelConfig config) {
        Translator<Image, ObbResult> translator = null;
         if (config.getModelEnum() == ObbDetModelEnum.YOLOV11){
            translator = YoloV11OddTranslator.builder()
                    .setImageSize(config.getModelEnum().getInputWidth(), config.getModelEnum().getInputHeight())
                    .optThreshold(config.getThreshold() > 0 ? config.getThreshold() : 0.25f)
                    .optNmsThreshold(0.45f)
                    .optSynsetArtifactName("synset.txt").build();
        }
        return translator;
    }
}
