package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.FaceAttributeConfig;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.enums.FaceAttributeModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.attribute.Seetaface6FaceAttributeModel;
import lombok.extern.slf4j.Slf4j;
import cn.smartjavaai.face.model.attribute.FaceAttributeModel;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人脸属性检测模型工厂
 * @author dwj
 */
@Slf4j
public class FaceAttributeModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile FaceAttributeModelFactory instance;

    private static final ConcurrentHashMap<FaceAttributeModelEnum, FaceAttributeModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<FaceAttributeModelEnum, Class<? extends FaceAttributeModel>> registry =
            new ConcurrentHashMap<>();


    public static FaceAttributeModelFactory getInstance() {
        if (instance == null) {
            synchronized (FaceAttributeModelFactory.class) {
                if (instance == null) {
                    instance = new FaceAttributeModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param name
     * @param clazz
     */
    private static void registerModel(FaceAttributeModelEnum faceAttributeModelEnum, Class<? extends FaceAttributeModel> clazz) {
        registry.put(faceAttributeModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public FaceAttributeModel getModel(FaceAttributeConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new FaceException("未配置活体检测模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createFaceModel(config);
        });
    }

    /**
     * 使用ModelConfig创建算法
     * @param config
     * @return
     */
    private FaceAttributeModel createFaceModel(FaceAttributeConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new FaceException("Unsupported model");
        }
        FaceAttributeModel model = null;
        try {
            model = (FaceAttributeModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel(FaceAttributeModelEnum.SEETA_FACE6_MODEL, Seetaface6FaceAttributeModel.class);
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
    public static void removeFromCache(FaceAttributeModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
