package cn.smartjavaai.translation.factory;

import cn.smartjavaai.common.config.Config;


import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.enums.TranslationModeEnum;
import cn.smartjavaai.translation.exception.TranslationException;
import cn.smartjavaai.translation.model.NllbModel;
import cn.smartjavaai.translation.model.OpusMtModel;
import cn.smartjavaai.translation.model.TranslationModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 机器翻译模型工厂
 * @author dwj
 */
@Slf4j
public class TranslationModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile TranslationModelFactory instance;

    private static final ConcurrentHashMap<TranslationModeEnum, TranslationModel> modelMap = new ConcurrentHashMap<>();



    /**
     * 检测模型注册表
     */
    private static final Map<TranslationModeEnum, Class<? extends TranslationModel>> modelRegistry =
            new ConcurrentHashMap<>();


    public static TranslationModelFactory getInstance() {
        if (instance == null) {
            synchronized (TranslationModelFactory.class) {
                if (instance == null) {
                    instance = new TranslationModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册翻译模型
     * @param translationModeEnum
     * @param clazz
     */
    private static void registerCommonDetModel(TranslationModeEnum translationModeEnum, Class<? extends TranslationModel> clazz) {
        modelRegistry.put(translationModeEnum, clazz);
    }

    /**
     * 获取翻译模型（通过配置）
     * @param config
     * @return
     */
    public TranslationModel getModel(TranslationModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new TranslationException("未配置OCR模型");
        }
        return modelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createModel(config);
        });
    }


    /**
     * 创建翻译模型
     * @param config
     * @return
     */
    private TranslationModel createModel(TranslationModelConfig config) {
        Class<?> clazz = modelRegistry.get(config.getModelEnum());
        if(clazz == null){
            throw new TranslationException("Unsupported model");
        }
        TranslationModel model = null;
        try {
            model = (TranslationModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TranslationException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerCommonDetModel(TranslationModeEnum.NLLB_MODEL, NllbModel.class);
        registerCommonDetModel(TranslationModeEnum.OPUS_MT_EN_ZH, OpusMtModel.class);
        registerCommonDetModel(TranslationModeEnum.OPUS_MT_ZH_EN, OpusMtModel.class);
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
    public static void removeFromCache(TranslationModeEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
