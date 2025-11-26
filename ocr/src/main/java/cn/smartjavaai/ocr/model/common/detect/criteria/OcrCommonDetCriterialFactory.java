package cn.smartjavaai.ocr.model.common.detect.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.model.common.detect.translator.PPOCRDetTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/7/8
 */
public class OcrCommonDetCriterialFactory {


    public static Criteria<Image, NDList> createCriteria(OcrDetModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, NDList> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
        if(StringUtils.isNotBlank(config.getBatchifier())){
            params.put("batchifier", config.getBatchifier());
        }
        if(config.getModelEnum() == CommonDetModelEnum.PP_OCR_V5_SERVER_DET_MODEL ||
                config.getModelEnum() == CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL ||
                config.getModelEnum() == CommonDetModelEnum.PP_OCR_V4_SERVER_DET_MODEL ||
                config.getModelEnum() == CommonDetModelEnum.PP_OCR_V4_MOBILE_DET_MODEL
        ){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, NDList.class)
                            .optModelPath(Paths.get(config.getDetModelPath()))
                            .optTranslator(new PPOCRDetTranslator(params))
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }
        return criteria;
    }
}
