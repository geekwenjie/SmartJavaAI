package cn.smartjavaai.clip.model;

import cn.smartjavaai.action.model.ActionRecModelFactory;
import cn.smartjavaai.action.model.CommonActionRecModel;
import cn.smartjavaai.clip.config.ClipModelConfig;
import cn.smartjavaai.clip.enums.ClipModelEnum;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/10/20
 */
@Slf4j
public class ClipModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile ClipModelFactory instance;

    private static final ConcurrentHashMap<ClipModelEnum, ClipModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<ClipModelEnum, Class<? extends ClipModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private ClipModelFactory() {}

    // 双重检查锁定的单例方法
    public static ClipModelFactory getInstance() {
        if (instance == null) {
            synchronized (ClipModelFactory.class) {
                if (instance == null) {
                    instance = new ClipModelFactory();
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
    public ClipModel getModel(ClipModelConfig config) {
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
    private ClipModel createFaceDetModel(ClipModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        ClipModel model = null;
        try {
            model = (ClipModel) clazz.newInstance();
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
    private static void registerAlgorithm(ClipModelEnum modelEnum, Class<? extends ClipModel> clazz) {
        registry.put(modelEnum, clazz);
    }



    // 初始化默认算法
    static {
        registerAlgorithm(ClipModelEnum.OPENAI, OpenAIClipModel.class);
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
    public static void removeFromCache(ClipModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
