package cn.smartjavaai.face.model.liveness.criterial;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.ImageFeatureExtractorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.constant.FaceNetConstant;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.model.liveness.translator.IicFrTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 活体检测 Criteria构建工厂
 * @author dwj
 */
public class LivenessCriteriaFactory {

    public static Criteria<Image, Float> createCriteria(LivenessConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, Float> criteria = null;
        if(config.getModelEnum() == LivenessModelEnum.IIC_FL_MODEL){
            criteria = Criteria.builder()
                                    .optEngine("OnnxRuntime")
                                    .setTypes(ai.djl.modality.cv.Image.class, Float.class)
                                    .optModelPath(Paths.get(config.getModelPath()))
                                    .optTranslator(new IicFrTranslator())
                                    .optProgress(new ProgressBar())
                                    .optDevice(device)
                                    .build();
        }
        return criteria;
    }

}
