package cn.smartjavaai.cls.model;

import cn.smartjavaai.action.model.CommonActionRecModel;
import cn.smartjavaai.cls.config.ClsModelConfig;
import cn.smartjavaai.cls.enums.ClsModelEnum;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图像分类 模型工厂
 * @author dwj
 */
@Slf4j
public class ClsModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile ClsModelFactory instance;

    private static final ConcurrentHashMap<ClsModelEnum, ClsModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<ClsModelEnum, Class<? extends ClsModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private ClsModelFactory() {}

    // 双重检查锁定的单例方法
    public static ClsModelFactory getInstance() {
        if (instance == null) {
            synchronized (ClsModelFactory.class) {
                if (instance == null) {
                    instance = new ClsModelFactory();
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
    public ClsModel getModel(ClsModelConfig config) {
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
    private ClsModel createFaceDetModel(ClsModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        ClsModel model = null;
        try {
            model = (ClsModel) clazz.newInstance();
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
    private static void registerAlgorithm(ClsModelEnum modelEnum, Class<? extends ClsModel> clazz) {
        registry.put(modelEnum, clazz);
    }



    // 初始化默认算法
    static {
        registerAlgorithm(ClsModelEnum.YOLOV8, CommonClsModel.class);
        registerAlgorithm(ClsModelEnum.YOLOV11, CommonClsModel.class);
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
    public static void removeFromCache(ClsModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }
}

