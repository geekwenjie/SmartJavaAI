package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.liveness.CommonLivenessModel;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
import cn.smartjavaai.face.model.liveness.MiniVisionLivenessModel;
import cn.smartjavaai.face.model.liveness.Seetaface6LivenessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 活体检测模型工厂
 * @author dwj
 */
@Slf4j
public class LivenessModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile LivenessModelFactory instance;

    private static final ConcurrentHashMap<LivenessModelEnum, LivenessDetModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<LivenessModelEnum, Class<? extends LivenessDetModel>> registry =
            new ConcurrentHashMap<>();


    public static LivenessModelFactory getInstance() {
        if (instance == null) {
            synchronized (LivenessModelFactory.class) {
                if (instance == null) {
                    instance = new LivenessModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param livenessModelEnum
     * @param clazz
     */
    private static void registerModel(LivenessModelEnum livenessModelEnum, Class<? extends LivenessDetModel> clazz) {
        registry.put(livenessModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public LivenessDetModel getModel(LivenessConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置活体检测模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createFaceModel(config);
        });
    }

    /**
     * 使用ModelConfig创建模型
     * @param config
     * @return
     */
    private LivenessDetModel createFaceModel(LivenessConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new FaceException("Unsupported algorithm");
        }
        LivenessDetModel model = null;
        try {
            model = (LivenessDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel(LivenessModelEnum.SEETA_FACE6_MODEL, Seetaface6LivenessModel.class);
        registerModel(LivenessModelEnum.MINI_VISION_MODEL, MiniVisionLivenessModel.class);
        registerModel(LivenessModelEnum.IIC_FL_MODEL, CommonLivenessModel.class);
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
    public static void removeFromCache(LivenessModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
