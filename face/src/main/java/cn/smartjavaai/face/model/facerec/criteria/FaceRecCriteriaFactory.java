package cn.smartjavaai.face.model.facerec.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceNetConstant;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.model.facerec.FaceRecPreprocessConfig;
import cn.smartjavaai.face.model.facerec.translator.*;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 人脸识别 Criteria构建工厂
 * @author dwj
 */
public class FaceRecCriteriaFactory {

    public static Criteria<Image, float[]> createCriteria(FaceRecConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Translator<Image, float[]> translator = getFaceRecTranslator(config);
        Criteria<Image, float[]> criteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                FaceNetConstant.MODEL_URL)
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optTranslator(translator)
                        .optDevice(device)
                        .optEngine(config.getModelEnum().getEngine())
                        .optProgress(new ProgressBar())
                        .build();
        return criteria;
    }

    /**
     * 获取人脸识别模型Translator
     * @param config
     * @return
     */
    public static Translator<Image, float[]> getFaceRecTranslator(FaceRecConfig config) {
        FaceRecPreprocessConfig preprocessConfig =  new FaceRecPreprocessConfig.Builder()
                .inputSize(config.getModelEnum().getInputWidth(), config.getModelEnum().getInputHeight())
                .build();
        switch (config.getModelEnum()) {
            case VGG_FACE:
                preprocessConfig = new FaceRecPreprocessConfig.Builder()
                        .inputSize(config.getModelEnum().getInputWidth(), config.getModelEnum().getInputHeight())
                        .usePipeline(false)
                        .normalize(false)
                        .build();
                break;
        }
        return new CommonFaceRecTranslator(preprocessConfig);
    }

}
