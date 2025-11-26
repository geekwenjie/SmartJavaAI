package cn.smartjavaai.objectdetection.model.person;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;
import cn.smartjavaai.objectdetection.enums.PersonDetectorModelEnum;
import cn.smartjavaai.objectdetection.enums.PersonDetectorModelEnum;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 行人检测 模型工厂
 * @author dwj
 */
@Slf4j
public class PersonDetModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile PersonDetModelFactory instance;

    private static final ConcurrentHashMap<PersonDetectorModelEnum, PersonDetModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<PersonDetectorModelEnum, Class<? extends PersonDetModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private PersonDetModelFactory() {}

    // 双重检查锁定的单例方法
    public static PersonDetModelFactory getInstance() {
        if (instance == null) {
            synchronized (PersonDetModelFactory.class) {
                if (instance == null) {
                    instance = new PersonDetModelFactory();
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
    public PersonDetModel getModel(PersonDetModelConfig config) {
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
    private PersonDetModel createModel(PersonDetModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        PersonDetModel model = null;
        try {
            model = (PersonDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DetectionException(e);
        }
        model.loadModel(config);
        return model;
    }


    /**
     * 注册模型
     * @param modelEnum
     * @param clazz
     */
    private static void registerAlgorithm(PersonDetectorModelEnum modelEnum, Class<? extends PersonDetModel> clazz) {
        registry.put(modelEnum, clazz);
    }


    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(PersonDetectorModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }


    // 初始化默认算法
    static {
        registerAlgorithm(PersonDetectorModelEnum.YOLOV8_PERSON, CommonPersonDetModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }
}

