package cn.smartjavaai.ocr.model.plate.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.enums.PlateDetModelEnum;
import cn.smartjavaai.ocr.model.plate.translator.Yolo5PlateDetectTranslator;
import cn.smartjavaai.ocr.model.plate.translator.Yolov7PlateDetectTranslator;
import cn.smartjavaai.ocr.model.plate.translator.Yolov8PlateDetectTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/7/8
 */
public class PlateDetCriterialFactory {


    public static Criteria<Image, DetectedObjects> createCriteria(PlateDetModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, DetectedObjects> criteria = null;
        ConcurrentHashMap params = new ConcurrentHashMap<String, String>();
        params.putAll(config.getCustomParams());
        if(StringUtils.isNotBlank(config.getBatchifier())){
            params.put("batchifier", config.getBatchifier());
        }
        if(config.getModelEnum() == PlateDetModelEnum.YOLOV5){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, DetectedObjects.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optTranslator(new Yolo5PlateDetectTranslator(params))
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }else if (config.getModelEnum() == PlateDetModelEnum.YOLOV7){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, DetectedObjects.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optTranslator(new Yolov7PlateDetectTranslator(params))
                            .optDevice(device)
                            .optProgress(new ProgressBar())
                            .build();
        }
//        else if (config.getModelEnum() == PlateDetModelEnum.YOLOV8){
//            criteria =
//                    Criteria.builder()
//                            .optEngine("OnnxRuntime")
//                            .setTypes(Image.class, DetectedObjects.class)
//                            .optModelPath(Paths.get(config.getModelPath()))
//                            .optTranslator(new Yolov8PlateDetectTranslator(params))
//                            .optDevice(device)
//                            .optProgress(new ProgressBar())
//                            .build();
//        }
        return criteria;
    }
}
