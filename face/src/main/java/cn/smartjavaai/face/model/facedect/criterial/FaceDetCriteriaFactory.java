package cn.smartjavaai.face.model.facedect.criterial;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.LetterBoxUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.constant.RetinaFaceConstant;
import cn.smartjavaai.face.constant.UltraLightFastGenericFaceConstant;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.expression.translator.DenseNetEmotionTranslator;
import cn.smartjavaai.face.model.expression.translator.FrEmotionTranslator;
import cn.smartjavaai.face.translator.FaceDetectionTranslator;
import cn.smartjavaai.face.translator.YoloV5FaceTranslator;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 人脸检测 Criteria构建工厂
 * @author dwj
 */
public class FaceDetCriteriaFactory {

    /**
     * 创建人脸检测Criteria
     * @param config
     * @return
     */
    public static Criteria<Image, DetectedObjects> createCriteria(FaceDetConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Translator<Image, DetectedObjects> translator = getTranslator(config);
        if(StringUtils.isBlank(config.getModelEnum().getModelUrl())){
            //检查模型路径
            if (StringUtils.isBlank(config.getModelPath())){
                throw new FaceException("请指定模型路径");
            }
        }
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : config.getModelEnum().getModelUrl())
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optTranslator(translator)
                        .optDevice(device)
                        .optProgress(new ProgressBar())
                        .optEngine(config.getModelEnum().getEngine())
                        .build();
        return criteria;
    }


    /**
     * 获取人脸检测Translator
     * @param config
     * @return
     */
    public static Translator<Image, DetectedObjects> getTranslator(FaceDetConfig config) {
        Translator<Image, DetectedObjects> translator = null;
        if(config.getModelEnum() == FaceDetModelEnum.RETINA_FACE || config.getModelEnum() == FaceDetModelEnum.RETINA_FACE_640_ONNX
                || config.getModelEnum() == FaceDetModelEnum.RETINA_FACE_1080_720_ONNX){
            translator =
                    new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), RetinaFaceConstant.variance, FaceDetectConstant.MAX_FACE_LIMIT,
                            RetinaFaceConstant.scales, RetinaFaceConstant.steps, config.getModelEnum().getInputWidth(), config.getModelEnum().getInputHeight());
        }else if (config.getModelEnum() == FaceDetModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE){
            translator =
                    new FaceDetectionTranslator(config.getConfidenceThreshold(), config.getNmsThresh(), UltraLightFastGenericFaceConstant.variance,
                            FaceDetectConstant.MAX_FACE_LIMIT, UltraLightFastGenericFaceConstant.scales, UltraLightFastGenericFaceConstant.steps, config.getModelEnum().getInputWidth(), config.getModelEnum().getInputHeight());
        }else if (config.getModelEnum() == FaceDetModelEnum.YOLOV5_FACE_640
                || config.getModelEnum() == FaceDetModelEnum.YOLOV5_FACE_320){
            translator = YoloV5FaceTranslator.builder()
                            .setImageSize(config.getModelEnum().getInputWidth(), config.getModelEnum().getInputWidth()).build();
        }
        return translator;
    }

}
