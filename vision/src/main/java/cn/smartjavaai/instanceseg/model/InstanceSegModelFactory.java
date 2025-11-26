package cn.smartjavaai.instanceseg.model;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.instanceseg.config.InstanceSegModelConfig;
import cn.smartjavaai.instanceseg.enums.InstanceSegModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实例分割 模型工厂
 * @author dwj
 */
@Slf4j
public class InstanceSegModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile InstanceSegModelFactory instance;

    private static final ConcurrentHashMap<InstanceSegModelEnum, InstanceSegModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<InstanceSegModelEnum, Class<? extends InstanceSegModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private InstanceSegModelFactory() {}

    // 双重检查锁定的单例方法
    public static InstanceSegModelFactory getInstance() {
        if (instance == null) {
            synchronized (InstanceSegModelFactory.class) {
                if (instance == null) {
                    instance = new InstanceSegModelFactory();
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
    public InstanceSegModel getModel(InstanceSegModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createFaceDetModel(config);
        });
    }

    /**
     * 使用ModelConfig创建模型
     * @param config
     * @return
     */
    private InstanceSegModel createFaceDetModel(InstanceSegModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        InstanceSegModel model = null;
        try {
            model = (InstanceSegModel) clazz.newInstance();
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
    private static void registerAlgorithm(InstanceSegModelEnum modelEnum, Class<? extends InstanceSegModel> clazz) {
        registry.put(modelEnum, clazz);
    }


    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(InstanceSegModelEnum modelEnum) {
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
        registerAlgorithm(InstanceSegModelEnum.SEG_YOLOV8N_ONNX, CommonInstanceSegModel.class);
        registerAlgorithm(InstanceSegModelEnum.SEG_YOLOV8N_PYTORCH, CommonInstanceSegModel.class);
        registerAlgorithm(InstanceSegModelEnum.SEG_YOLO11N_PYTORCH, CommonInstanceSegModel.class);
        registerAlgorithm(InstanceSegModelEnum.SEG_YOLO11N_ONNX, CommonInstanceSegModel.class);
        registerAlgorithm(InstanceSegModelEnum.SEG_MASK_RCNN, CommonInstanceSegModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }
}

