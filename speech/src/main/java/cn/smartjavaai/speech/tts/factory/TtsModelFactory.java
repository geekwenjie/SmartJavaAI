package cn.smartjavaai.speech.tts.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.asr.model.VoskRecognizer;
import cn.smartjavaai.speech.asr.model.WhisperRecognizer;
import cn.smartjavaai.speech.tts.config.TtsModelConfig;
import cn.smartjavaai.speech.tts.enums.TtsModelEnum;
import cn.smartjavaai.speech.tts.model.SherpaTtsModel;
import cn.smartjavaai.speech.tts.model.TtsModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音合成模型工厂
 * @author dwj
 */
@Slf4j
public class TtsModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile TtsModelFactory instance;

    /**
     * 模型缓存
     */
    private static final ConcurrentHashMap<TtsModelEnum, TtsModel> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<TtsModelEnum, Class<? extends TtsModel>> registry =
            new ConcurrentHashMap<>();


    public static TtsModelFactory getInstance() {
        if (instance == null) {
            synchronized (TtsModelFactory.class) {
                if (instance == null) {
                    instance = new TtsModelFactory();
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
    private static void registerModel(TtsModelEnum expressionModelEnum, Class<? extends TtsModel> clazz) {
        registry.put(expressionModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public TtsModel getModel(TtsModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new AsrException("未配置语音识别模型枚举");
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
    private TtsModel createModel(TtsModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new AsrException("Unsupported model");
        }
        TtsModel model = null;
        try {
            model = (TtsModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AsrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel(TtsModelEnum.SHERPA_KOKORO, SherpaTtsModel.class);
        registerModel(TtsModelEnum.SHERPA_KITTEN, SherpaTtsModel.class);
        registerModel(TtsModelEnum.SHERPA_MATCHA, SherpaTtsModel.class);
        registerModel(TtsModelEnum.SHERPA_VITS, SherpaTtsModel.class);
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
    public static void removeFromCache(TtsModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }



}
