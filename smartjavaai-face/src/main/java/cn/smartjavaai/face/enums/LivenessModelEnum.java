package cn.smartjavaai.face.enums;

/**
 * 活体检测模型枚举
 * @author dwj
 * @date 2025/4/10
 */
public enum LivenessModelEnum {

    SEETA_FACE6_MODEL("SeetaFace6Model");

    private final String modelClassName;

    LivenessModelEnum(String modelClassName) {
        this.modelClassName = modelClassName;
    }

    public String getModelClassName() {
        return modelClassName;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static LivenessModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (LivenessModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
