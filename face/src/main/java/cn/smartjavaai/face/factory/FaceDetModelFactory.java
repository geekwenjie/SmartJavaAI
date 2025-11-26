package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceAttributeModelEnum;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.facedect.CommonFaceDetModel;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facedect.MtcnnFaceDetModel;
import cn.smartjavaai.face.model.facedect.SeetaFace6FaceDetModel;
import cn.smartjavaai.face.model.facerec.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸检测模型工厂
 * @author dwj
 */
@Slf4j
public class FaceDetModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile FaceDetModelFactory instance;

    private static final ConcurrentHashMap<FaceDetModelEnum, FaceDetModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<FaceDetModelEnum, Class<? extends FaceDetModel>> registry =
            new ConcurrentHashMap<>();


    public static FaceDetModelFactory getInstance() {
        if (instance == null) {
            synchronized (FaceDetModelFactory.class) {
                if (instance == null) {
                    instance = new FaceDetModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param faceDetModelEnum
     * @param clazz
     */
    private static void registerAlgorithm(FaceDetModelEnum faceDetModelEnum, Class<? extends FaceDetModel> clazz) {
        registry.put(faceDetModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public FaceDetModel getModel(FaceDetConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置人脸模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createFaceDetModel(config);
        });
    }

    /**
     * 获取默认模型
     * @return
     */
    public FaceDetModel getModel() {
        // 初始化默认配置
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return getModel(config);
    }

    /**
     * 使用ModelConfig创建模型
     * @param config
     * @return
     */
    private FaceDetModel createFaceDetModel(FaceDetConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new FaceException("Unsupported model");
        }
        FaceDetModel model = null;
        try {
            model = (FaceDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    /**
     * 获取轻量级人脸模型
     * @return
     */
    public FaceDetModel getLightFaceDetModel() {
        // 初始化默认配置
        FaceDetConfig config = new FaceDetConfig();
        config.setModelEnum(FaceDetModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE);
        config.setConfidenceThreshold(FaceDetectConstant.DEFAULT_CONFIDENCE_THRESHOLD);
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return getModel(config);
    }


    // 初始化默认算法
    static {
        registerAlgorithm(FaceDetModelEnum.RETINA_FACE, CommonFaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.RETINA_FACE_640_ONNX, CommonFaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.RETINA_FACE_1080_720_ONNX, CommonFaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.ULTRA_LIGHT_FAST_GENERIC_FACE, CommonFaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.SEETA_FACE6_MODEL, SeetaFace6FaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.YOLOV5_FACE_640, CommonFaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.YOLOV5_FACE_320, CommonFaceDetModel.class);
        registerAlgorithm(FaceDetModelEnum.MTCNN, MtcnnFaceDetModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }

    /**
     * 关闭所有已加载的模型
     */
    public void closeAll() {
        modelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        modelMap.clear();
    }

    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(FaceDetModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
