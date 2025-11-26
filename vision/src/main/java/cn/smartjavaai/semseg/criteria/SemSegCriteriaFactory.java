package cn.smartjavaai.semseg.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.CategoryMask;
import ai.djl.modality.cv.translator.SemanticSegmentationTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.semseg.config.SemSegModelConfig;
import cn.smartjavaai.semseg.enums.SemSegModelEnum;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语义分割Criteria工厂
 * @author dwj
 */
public class SemSegCriteriaFactory {


    public static Criteria<Image, CategoryMask> createCriteria(SemSegModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, CategoryMask> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
        if(config.getModelEnum() == SemSegModelEnum.DEEPLABV3){
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, CategoryMask.class)
                            .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                    config.getModelEnum().getModelUri())
                            .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                            .optTranslatorFactory(new SemanticSegmentationTranslatorFactory())
                            .optEngine("PyTorch")
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }
        return criteria;
    }
}
