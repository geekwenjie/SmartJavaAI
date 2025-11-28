package cn.smartjavaai.zeroshot.model;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.zeroshot.config.ZeroDetConfig;
import cn.smartjavaai.zeroshot.enums.ZeroDetModelEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 零样本目标检测 模型工厂
 * @author dwj
 */
@Slf4j
public class ZeroDetModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile ZeroDetModelFactory instance;

    private static final ConcurrentHashMap<ZeroDetModelEnum, ZeroDetModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<ZeroDetModelEnum, Class<? extends ZeroDetModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private ZeroDetModelFactory() {}

    // 双重检查锁定的单例方法
    public static ZeroDetModelFactory getInstance() {
        if (instance == null) {
            synchronized (ZeroDetModelFactory.class) {
                if (instance == null) {
                    instance = new ZeroDetModelFactory();
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
    public ZeroDetModel getModel(ZeroDetConfig config) {
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
    private ZeroDetModel createModel(ZeroDetConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        ZeroDetModel model = null;
        try {
            model = (ZeroDetModel) clazz.newInstance();
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
    private static void registerAlgorithm(ZeroDetModelEnum modelEnum, Class<? extends ZeroDetModel> clazz) {
        registry.put(modelEnum, clazz);
    }


    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(ZeroDetModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }


    // 初始化默认算法
    static {
        registerAlgorithm(ZeroDetModelEnum.YOLOV8S_WORLDV2, CommonZeroDetModel.class);
        registerAlgorithm(ZeroDetModelEnum.OWLV2_BASE_PATCH16, CommonZeroDetModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }
}

