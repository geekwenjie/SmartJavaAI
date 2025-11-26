package cn.smartjavaai.face.model.expression.criterial;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.model.expression.translator.DenseNetEmotionTranslator;
import cn.smartjavaai.face.model.expression.translator.FrEmotionTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * 人脸表情识别 Criteria构建工厂
 * @author dwj
 * @date 2025/5/14
 */
public class EmotionCriteriaFactory {

    public static Criteria<Image, Classifications> createCriteria(FaceExpressionConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, Classifications> criteria = null;
        if(config.getModelEnum() == ExpressionModelEnum.DensNet121){
            //开源项目地址：https://github.com/sajjjadayobi/FaceLib
            //初始化 检测Criteria
            criteria =
                    Criteria.builder()
                            .optEngine("PyTorch")
                            .setTypes(Image.class, Classifications.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optTranslator(new DenseNetEmotionTranslator(224))
                            .optProgress(new ProgressBar())
                            .optDevice(device)
                            .build();
        }else if (config.getModelEnum() == ExpressionModelEnum.FrEmotion){
            //初始化 检测Criteria
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(ai.djl.modality.cv.Image.class, Classifications.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optTranslator(new FrEmotionTranslator(224))
                            .optProgress(new ProgressBar())
                            .optDevice(device)
                            .build();
        }
        return criteria;
    }

}
