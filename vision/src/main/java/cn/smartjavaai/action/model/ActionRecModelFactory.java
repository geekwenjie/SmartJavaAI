package cn.smartjavaai.action.model;

import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.model.person.CommonPersonDetModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动作识别 模型工厂
 * @author dwj
 */
@Slf4j
public class ActionRecModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile ActionRecModelFactory instance;

    private static final ConcurrentHashMap<ActionRecModelEnum, ActionRecModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<ActionRecModelEnum, Class<? extends ActionRecModel>> registry =
            new ConcurrentHashMap<>();


    // 私有构造函数，防止外部创建实例
    private ActionRecModelFactory() {}

    // 双重检查锁定的单例方法
    public static ActionRecModelFactory getInstance() {
        if (instance == null) {
            synchronized (ActionRecModelFactory.class) {
                if (instance == null) {
                    instance = new ActionRecModelFactory();
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
    public ActionRecModel getModel(ActionRecModelConfig config) {
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
    private ActionRecModel createModel(ActionRecModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new DetectionException("Unsupported model");
        }
        ActionRecModel model = null;
        try {
            model = (ActionRecModel) clazz.newInstance();
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
    private static void registerAlgorithm(ActionRecModelEnum modelEnum, Class<? extends ActionRecModel> clazz) {
        registry.put(modelEnum, clazz);
    }



    // 初始化默认算法
    static {
        registerAlgorithm(ActionRecModelEnum.INCEPTIONV1_KINETICS400_ONNX, CommonActionRecModel.class);
        registerAlgorithm(ActionRecModelEnum.INCEPTIONV3_KINETICS400_ONNX, CommonActionRecModel.class);
        registerAlgorithm(ActionRecModelEnum.RESNET_V1B_KINETICS400_ONNX, CommonActionRecModel.class);
        registerAlgorithm(ActionRecModelEnum.VIT_BASE_PATCH16_224_DJL, CommonActionRecModel.class);
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
    public static void removeFromCache(ActionRecModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }
}

