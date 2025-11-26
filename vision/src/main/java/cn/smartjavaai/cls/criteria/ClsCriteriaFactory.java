package cn.smartjavaai.cls.criteria;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.action.exception.ActionException;
import cn.smartjavaai.action.model.CommonActionTranslator;
import cn.smartjavaai.cls.config.ClsModelConfig;
import cn.smartjavaai.cls.enums.ClsModelEnum;
import cn.smartjavaai.cls.translator.YoloClsTranslator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.DJLCommonUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * 分类模型Criteria工厂
 * @author dwj
 */
public class ClsCriteriaFactory {


    /**
     * 创建动作识别Criteria
     * @param config
     * @return
     */
    public static Criteria<Image, Classifications> createCriteria(ClsModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Translator<Image, Classifications> translator = getTranslator(config);
        //检查模型路径
        if (StringUtils.isBlank(config.getModelPath())){
            throw new ActionException("请指定模型路径");
        }
        boolean isUrl = DJLCommonUtils.hasSupportedProtocol(config.getModelPath());
        Criteria<Image, Classifications> criteria =
                Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelUrls(isUrl ? config.getModelPath() : null)
                        .optModelPath(isUrl ? null : Paths.get(config.getModelPath()))
                        .optTranslator(translator)
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .optEngine(config.getModelEnum().getEngine())
                        .build();
        return criteria;
    }

    /**
     * 获取分类模型Translator
     * @param config
     * @return
     */
    public static Translator<Image, Classifications> getTranslator(ClsModelConfig config) {
        Translator<Image, Classifications> translator = null;
        if(config.getModelEnum() == ClsModelEnum.YOLOV11
                || config.getModelEnum() == ClsModelEnum.YOLOV8){
        YoloClsTranslator.Builder builder = YoloClsTranslator.builder().optSynsetArtifactName("synset.txt");
            translator = builder.build();
        }
        return translator;
    }
}
