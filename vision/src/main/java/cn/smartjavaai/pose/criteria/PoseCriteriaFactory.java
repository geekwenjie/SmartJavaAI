package cn.smartjavaai.pose.criteria;

import ai.djl.Device;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.Joints;
import ai.djl.modality.cv.translator.YoloPoseTranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.action.model.CommonActionTranslator;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.action.exception.ActionException;
import cn.smartjavaai.common.utils.DJLCommonUtils;
import cn.smartjavaai.obb.config.ObbDetModelConfig;
import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.obb.enums.ObbDetModelEnum;
import cn.smartjavaai.obb.exception.ObbDetException;
import cn.smartjavaai.obb.translator.YoloV11OddTranslator;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.pose.config.PoseModelConfig;
import cn.smartjavaai.pose.enums.PoseModelEnum;
import cn.smartjavaai.pose.exception.PoseException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 姿势估计模型工厂
 * @author dwj
 */
public class PoseCriteriaFactory {

    /**
     * 创建姿势估计Criteria
     * @param config
     * @return
     */
    public static Criteria<Image, Joints[]> createCriteria(PoseModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, Joints[]> criteria = null;
        //DJL官方提供模型
        if(StringUtils.isNotBlank(config.getModelEnum().getModelUri())){
            criteria = createDJLCriteria(config, device);
        }
        return criteria;
    }

    /**
     * 创建DJL官方模型Criteria
     * 需要模型同目录下存在：serving.properties
     * @param config
     * @return
     */
    public static Criteria<Image, Joints[]> createDJLCriteria(PoseModelConfig config, Device device) {
//        if(StringUtils.isNotBlank(config.getModelPath())
//                && !DJLCommonUtils.isServingPropertiesExists(Paths.get(config.getModelPath()))){
//            throw new PoseException("模型所在目录未找到 serving.properties 文件");
//        }

        Criteria<Image, Joints[]> criteria = null;
        if(config.getModelEnum() == PoseModelEnum.YOLO11N_POSE_PT || config.getModelEnum() == PoseModelEnum.YOLO11N_POSE_ONNX
                || config.getModelEnum() == PoseModelEnum.YOLOV8N_POSE_PT || config.getModelEnum() == PoseModelEnum.YOLOV8N_POSE_ONNX){
            criteria =
                    Criteria.builder()
                            .setTypes(Image.class, Joints[].class)
                            .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null : config.getModelEnum().getModelUri())
                            .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                            .optDevice(device)
                            .optTranslatorFactory(new YoloPoseTranslatorFactory())
                            .optArgument("threshold", config.getThreshold() > 0 ? config.getThreshold() : null)
                            .optProgress(new ProgressBar())
                            .build();
        }

        return criteria;
    }


}
