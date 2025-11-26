package cn.smartjavaai.semseg.model;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.semseg.config.SemSegModelConfig;
import cn.smartjavaai.semseg.enums.SemSegModelEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语义分割 模型工厂
 * @author dwj
 */
@Slf4j
public class SemSegModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile SemSegModelFactory instance;

    private static final ConcurrentHashMap<SemSegModelEnum, SemSegModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<SemSegModelEnum, Class<? extends SemSegModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private SemSegModelFactory() {}

    // 双重检查锁定的单例方法
    public static SemSegModelFactory getInstance() {
        if (instance == null) {
            synchronized (SemSegModelFactory.class) {
                if (instance == null) {
                    instance = new SemSegModelFactory();
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
    public SemSegModel getModel(SemSegModelConfig config) {
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
    private SemSegModel createModel(SemSegModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        SemSegModel model = null;
        try {
            model = (SemSegModel) clazz.newInstance();
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
    private static void registerAlgorithm(SemSegModelEnum modelEnum, Class<? extends SemSegModel> clazz) {
        registry.put(modelEnum, clazz);
    }


    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(SemSegModelEnum modelEnum) {
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
        registerAlgorithm(SemSegModelEnum.DEEPLABV3, CommonSemSegModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }
}

