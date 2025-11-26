package cn.smartjavaai.face.enums;

/**
 * 表情识别模型枚举
 * @author dwj
 */
public enum ExpressionModelEnum {

    DensNet121("DensNet121"),

    FrEmotion("FrEmotion");


    private final String modelClassName;

    ExpressionModelEnum(String modelClassName) {
        this.modelClassName = modelClassName;
    }

    public String getModelClassName() {
        return modelClassName;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ExpressionModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ExpressionModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
