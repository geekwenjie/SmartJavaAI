package cn.smartjavaai.speech.tts.enums;

/**
 * tts模型枚举
 * @author dwj
 */
public enum TtsModelEnum {

    SHERPA_VITS("vits"),
    SHERPA_MATCHA("matcha"),
    SHERPA_KOKORO("kokoro"),
    SHERPA_KITTEN("kitten");

    TtsModelEnum(String engine) {
        this.engine = engine;
    }

    /**
     * 模型引擎
     */
    private final String engine;

    public String getEngine() {
        return engine;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static TtsModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (TtsModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

}
