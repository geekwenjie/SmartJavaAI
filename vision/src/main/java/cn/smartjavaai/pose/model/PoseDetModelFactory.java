package cn.smartjavaai.pose.model;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.obb.model.CommonObbDetModel;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.pose.config.PoseModelConfig;
import cn.smartjavaai.pose.enums.PoseModelEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 姿态估计 模型工厂
 * @author dwj
 */
@Slf4j
public class PoseDetModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile PoseDetModelFactory instance;

    private static final ConcurrentHashMap<PoseModelEnum, PoseModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<PoseModelEnum, Class<? extends PoseModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private PoseDetModelFactory() {}

    // 双重检查锁定的单例方法
    public static PoseDetModelFactory getInstance() {
        if (instance == null) {
            synchronized (PoseDetModelFactory.class) {
                if (instance == null) {
                    instance = new PoseDetModelFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public PoseModel getModel(PoseModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createModel(config);
        });
    }

    /**
     * 使用ModelConfig创建模型
     * @param config
     * @return
     */
    private PoseModel createModel(PoseModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        PoseModel model = null;
        try {
            model = (PoseModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DetectionException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    /**
     * 注册模型
     * @param modelEnum
     * @param clazz
     */
    private static void registerAlgorithm(PoseModelEnum modelEnum, Class<? extends PoseModel> clazz) {
        registry.put(modelEnum, clazz);
    }


    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(PoseModelEnum modelEnum) {
        modelMap.remove(modelEnum);
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

    // 初始化默认算法
    static {
        registerAlgorithm(PoseModelEnum.YOLOV8N_POSE_ONNX, CommonPoseModel.class);
        registerAlgorithm(PoseModelEnum.YOLO11N_POSE_ONNX, CommonPoseModel.class);
        registerAlgorithm(PoseModelEnum.YOLOV8N_POSE_PT, CommonPoseModel.class);
        registerAlgorithm(PoseModelEnum.YOLO11N_POSE_PT, CommonPoseModel.class);
//        registerAlgorithm(PoseModelEnum.SIMPLE_POSE_MXNET, CommonPoseModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }
}

