package cn.smartjavaai.zeroshot.enums;

/**
 * 零样本目标检测模型枚举
 * @author dwj
 */
public enum ZeroDetModelEnum {

    YOLOV8S_WORLDV2("PyTorch", "djl://ai.djl.pytorch/yolov8s-worldv2"),
    OWLV2_BASE_PATCH16("PyTorch", "djl://ai.djl.huggingface.pytorch/google/owlv2-base-patch16");


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ZeroDetModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ZeroDetModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    private final String modelUri;

    /**
     * 模型引擎
     */
    private final String engine;

    ZeroDetModelEnum(String engine, String modelUri) {
        this.modelUri = modelUri;
        this.engine = engine;
    }

    public String getModelUri() {
        return modelUri;
    }

    public String getEngine() {
        return engine;
    }
}
