package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.QualityConfig;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.QualityModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.quality.FaceQualityModel;
import cn.smartjavaai.face.model.quality.Seetaface6QualityModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 质量评估模型工厂
 * @author dwj
 */
@Slf4j
public class FaceQualityModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile FaceQualityModelFactory instance;

    private static final ConcurrentHashMap<QualityModelEnum, FaceQualityModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<QualityModelEnum, Class<? extends FaceQualityModel>> registry =
            new ConcurrentHashMap<>();


    public static FaceQualityModelFactory getInstance() {
        if (instance == null) {
            synchronized (FaceQualityModelFactory.class) {
                if (instance == null) {
                    instance = new FaceQualityModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param qualityModelEnum
     * @param clazz
     */
    private static void registerModel(QualityModelEnum qualityModelEnum, Class<? extends FaceQualityModel> clazz) {
        registry.put(qualityModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public FaceQualityModel getModel(QualityConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置质量评估模型");
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
    private FaceQualityModel createFaceModel(QualityConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new FaceException("Unsupported algorithm");
        }
        FaceQualityModel model = null;
        try {
            model = (FaceQualityModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel(QualityModelEnum.SEETA_FACE6_MODEL, Seetaface6QualityModel.class);
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
    public static void removeFromCache(QualityModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
