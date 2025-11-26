package cn.smartjavaai.action.enums;

/**
 * 人类动作识别模型枚举
 * @author dwj
 */
public enum ActionRecModelEnum {

    VIT_BASE_PATCH16_224_DJL("PyTorch",0,0,"djl://ai.djl.pytorch/Human-Action-Recognition-VIT-Base-patch16-224"),

    INCEPTIONV3_KINETICS400_ONNX("OnnxRuntime",299,299,""),

    INCEPTIONV1_KINETICS400_ONNX("OnnxRuntime",224,224,""),

    RESNET_V1B_KINETICS400_ONNX("OnnxRuntime",224,224,"");

    /**
     * 模型输入尺寸：宽
     */
    private final int inputWidth;

    /**
     * 模型输入尺寸：高
     */
    private final int inputHeight;

    /**
     * 模型地址
     */
    private final String modelUrl;

    /**
     * 模型引擎
     */
    private final String engine;

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ActionRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ActionRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


    ActionRecModelEnum(String engine, int inputWidth, int inputHeight, String modelUrl) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.modelUrl = modelUrl;
        this.engine = engine;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public String getEngine() {
        return engine;
    }
}
