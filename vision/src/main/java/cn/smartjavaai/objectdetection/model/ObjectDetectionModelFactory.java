package cn.smartjavaai.objectdetection.model;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 目标检测 模型工厂
 * @author dwj
 */
@Slf4j
public class ObjectDetectionModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile ObjectDetectionModelFactory instance;

    private static final ConcurrentHashMap<DetectorModelEnum, DetectorModel> modelMap = new ConcurrentHashMap<>();

    static{
        log.debug("缓存目录：{}", Config.getCachePath());
    }

    // 私有构造函数，防止外部创建实例
    private ObjectDetectionModelFactory() {}

    // 双重检查锁定的单例方法
    public static ObjectDetectionModelFactory getInstance() {
        if (instance == null) {
            synchronized (ObjectDetectionModelFactory.class) {
                if (instance == null) {
                    instance = new ObjectDetectionModelFactory();
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
    public DetectorModel getModel(DetectorModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            DetectorModel model = new DetectorModel();
            model.loadModel(config);
            model.setFromFactory(true);
            return model;
        });
    }

    /**
     * 获取默认模型
     * @return
     */
//    public DetectorModel getModel() {
//        // 初始化默认配置
//        DetectorModelConfig config = new DetectorModelConfig();
//        config.setModelEnum(DetectorModelEnum.YOLO11N);
//        return getModel(config);
//    }

    /**
     * 关闭所有已加载的模型
     */
    public void closeAll() {
        modelMap.values().forEach(DetectorModel::close);
        modelMap.clear();
    }

    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(DetectorModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }
}

