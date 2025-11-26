package cn.smartjavaai.translation.enums;
/**
 * 机器翻译模型枚举
 * @author lwx
 * @date 2025/6/05
 */
public enum TranslationModeEnum {

    NLLB_MODEL,

    OPUS_MT_ZH_EN,

    OPUS_MT_EN_ZH;

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static TranslationModeEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (TranslationModeEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }
}
