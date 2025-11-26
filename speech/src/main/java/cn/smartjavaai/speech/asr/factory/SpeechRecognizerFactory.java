package cn.smartjavaai.speech.asr.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.enums.AsrModelEnum;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.asr.model.SherpaRecognizer;
import cn.smartjavaai.speech.asr.model.SpeechRecognizer;
import cn.smartjavaai.speech.asr.model.VoskRecognizer;
import cn.smartjavaai.speech.asr.model.WhisperRecognizer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音识别模型工厂
 * @author dwj
 */
@Slf4j
public class SpeechRecognizerFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile SpeechRecognizerFactory instance;

    /**
     * 模型缓存
     */
    private static final ConcurrentHashMap<AsrModelEnum, SpeechRecognizer> modelMap = new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<AsrModelEnum, Class<? extends SpeechRecognizer>> registry =
            new ConcurrentHashMap<>();


    public static SpeechRecognizerFactory getInstance() {
        if (instance == null) {
            synchronized (SpeechRecognizerFactory.class) {
                if (instance == null) {
                    instance = new SpeechRecognizerFactory();
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
    private static void registerModel(AsrModelEnum expressionModelEnum, Class<? extends SpeechRecognizer> clazz) {
        registry.put(expressionModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public SpeechRecognizer getModel(AsrModelConfig config) {
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
    private SpeechRecognizer createModel(AsrModelConfig config) {
        Class<?> clazz = registry.get(config.getModelEnum());
        if(clazz == null){
            throw new AsrException("Unsupported model");
        }
        SpeechRecognizer model = null;
        try {
            model = (SpeechRecognizer) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AsrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerModel(AsrModelEnum.WHISPER, WhisperRecognizer.class);
        registerModel(AsrModelEnum.VOSK, VoskRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_PARAFORMER, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_TRANSDUCER, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_WHISPER, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_FIREREDASR, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_MOONSHINE, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_NEMO, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_SENSEVOICE, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_DOLPHIN, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_ZIPFORMERCTC, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_WENETCTC, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_CANARY, SherpaRecognizer.class);
        registerModel(AsrModelEnum.SHERPA_TELESPEECH, SherpaRecognizer.class);
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
    public static void removeFromCache(AsrModelEnum modelEnum) {
        modelMap.remove(modelEnum);
    }

}
