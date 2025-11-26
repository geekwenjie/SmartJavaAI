package cn.smartjavaai.semseg.enums;

/**
 * 语义分割模型枚举
 * @author dwj
 */
public enum SemSegModelEnum {

    DEEPLABV3("djl://ai.djl.pytorch/deeplabv3");

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static SemSegModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (SemSegModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    private final String modelUri;

    SemSegModelEnum(String modelUri) {
        this.modelUri = modelUri;
    }

    public String getModelUri() {
        return modelUri;
    }

}
