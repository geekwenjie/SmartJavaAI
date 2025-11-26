package cn.smartjavaai.clip.enums;

/**
 * CLIP模型枚举
 * @author dwj
 */
public enum ClipModelEnum {

    OPENAI;



    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ClipModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ClipModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

}
