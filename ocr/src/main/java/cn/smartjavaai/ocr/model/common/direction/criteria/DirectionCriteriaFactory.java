package cn.smartjavaai.ocr.model.common.direction.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.DirectionInfo;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.model.common.detect.translator.PPOCRDetTranslator;
import cn.smartjavaai.ocr.model.common.direction.translator.PpWordRotateTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 行方向分类
 * @author dwj
 */
public class DirectionCriteriaFactory {

    public static Criteria<Image, DirectionInfo> createCriteria(DirectionModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, DirectionInfo> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
        if(StringUtils.isNotBlank(config.getBatchifier())){
            params.put("batchifier", config.getBatchifier());
        }
        if(config.getModelEnum() == DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS){
            params.put("resizeWidth", 192);
            params.put("resizeHeight", 48);
        }else if (config.getModelEnum() == DirectionModelEnum.PP_LCNET_X0_25){
            params.put("resizeWidth", 160);
            params.put("resizeHeight", 80);
        }else if (config.getModelEnum() == DirectionModelEnum.PP_LCNET_X1_0){
            params.put("resizeWidth", 160);
            params.put("resizeHeight", 80);
        }
        criteria =
                Criteria.builder()
                        .optEngine("OnnxRuntime")
                        .setTypes(Image.class, DirectionInfo.class)
                        .optModelPath(Paths.get(config.getModelPath()))
                        .optDevice(device)
                        .optTranslator(new PpWordRotateTranslator(params))
                        .optProgress(new ProgressBar())
                        .build();
        return criteria;
    }

}
