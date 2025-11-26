package cn.smartjavaai.ocr.model.common.recognize.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.model.common.recognize.translator.PPOCRRecTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/7/8
 */
public class OcrCommonRecCriterialFactory {


    public static Criteria<Image, String> createCriteria(OcrRecModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, String> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
        if(StringUtils.isNotBlank(config.getBatchifier())){
            params.put("batchifier", config.getBatchifier());
        }
        if(config.getRecModelEnum() == CommonRecModelEnum.PP_OCR_V5_SERVER_REC_MODEL ||
                config.getRecModelEnum() == CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL ||
                config.getRecModelEnum() == CommonRecModelEnum.PP_OCR_V4_SERVER_REC_MODEL ||
                config.getRecModelEnum() == CommonRecModelEnum.PP_OCR_V4_MOBILE_REC_MODEL ){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, String.class)
                            .optModelPath(Paths.get(config.getRecModelPath()))
                            .optTranslator(new PPOCRRecTranslator(params))
                            .optProgress(new ProgressBar())
                            .optDevice(device)
                            .build();
        }
        return criteria;
    }
}
