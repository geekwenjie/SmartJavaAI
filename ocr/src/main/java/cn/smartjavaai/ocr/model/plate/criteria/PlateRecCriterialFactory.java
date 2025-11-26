package cn.smartjavaai.ocr.model.plate.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.PlateRecModelConfig;
import cn.smartjavaai.ocr.entity.PlateResult;
import cn.smartjavaai.ocr.enums.PlateRecModelEnum;
import cn.smartjavaai.ocr.model.plate.translator.CRNNPlateRecTranslator;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author dwj
 * @date 2025/7/8
 */
public class PlateRecCriterialFactory {


    public static Criteria<Image, PlateResult> createCriteria(PlateRecModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, PlateResult> criteria = null;
        if(config.getModelEnum() == PlateRecModelEnum.PLATE_REC_CRNN){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, PlateResult.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optTranslator(new CRNNPlateRecTranslator())
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }
        return criteria;
    }
}
