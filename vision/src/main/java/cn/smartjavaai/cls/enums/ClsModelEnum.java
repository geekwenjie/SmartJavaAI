package cn.smartjavaai.cls.enums;

/**
 * 分类模型枚举
 * @author dwj
 */
public enum ClsModelEnum {

    YOLOV8("OnnxRuntime",224,224),
    YOLOV11("OnnxRuntime",224,224);

    /**
     * 模型输入尺寸：宽
     */
    private final int inputWidth;

    /**
     * 模型输入尺寸：高
     */
    private final int inputHeight;

    /**
     * 模型引擎
     */
    private final String engine;

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ClsModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ClsModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


    ClsModelEnum(String engine, int inputWidth, int inputHeight) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.engine = engine;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    public String getEngine() {
        return engine;
    }
}
