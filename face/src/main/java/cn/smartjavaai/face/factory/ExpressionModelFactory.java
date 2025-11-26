package cn.smartjavaai.face.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.expression.CommonEmotionModel;
import cn.smartjavaai.face.model.expression.ExpressionModel;
import cn.smartjavaai.face.model.liveness.MiniVisionLivenessModel;
import cn.smartjavaai.face.model.liveness.Seetaface6LivenessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表情识别模型工厂
 * @author dwj
 */
@Slf4j
public class ExpressionModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile ExpressionModelFactory instance;

    /**
     * 模型缓存
     */
    private static final ConcurrentHashMap<ExpressionModelEnum, ExpressionModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<ExpressionModelEnum, Class<? extends ExpressionModel>> registry =
            new ConcurrentHashMap<>();


    public static ExpressionModelFactory getInstance() {
        if (instance == null) {
            synchronized (ExpressionModelFactory.class) {
                if (instance == null) {
                    instance = new ExpressionModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param expressionModelEnum
     * @param clazz
     */
    private static void registerModel(ExpressionModelEnum expressionModelEnum, Class<? extends ExpressionModel> clazz) {
        registry.put(expressionModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public ExpressionModel getModel(FaceExpressionConfig config) {
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
    private ExpressionModel createFaceModel(FaceExpressionConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new FaceException("Unsupported algorithm");
        }
        ExpressionModel model = null;
        try {
            model = (ExpressionModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FaceException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel(ExpressionModelEnum.DensNet121, CommonEmotionModel.class);
        registerModel(ExpressionModelEnum.FrEmotion, CommonEmotionModel.class);
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
    public static void removeFromCache(ExpressionModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
