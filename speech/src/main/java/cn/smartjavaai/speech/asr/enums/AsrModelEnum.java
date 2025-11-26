package cn.smartjavaai.speech.asr.enums;

/**
 * 语音识别模型枚举
 * @author dwj
 */
public enum AsrModelEnum {

    WHISPER,

    VOSK,

    SHERPA_PARAFORMER,

    SHERPA_TRANSDUCER,

    SHERPA_WHISPER,

    SHERPA_FIREREDASR,

    SHERPA_MOONSHINE,

    SHERPA_NEMO,

    SHERPA_SENSEVOICE,

    SHERPA_DOLPHIN,

    SHERPA_ZIPFORMERCTC,

    SHERPA_WENETCTC,

    SHERPA_CANARY,

    SHERPA_TELESPEECH;

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static AsrModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (AsrModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

}
