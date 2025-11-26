package cn.smartjavaai.obb.enums;

/**
 * 旋转框检测模型枚举
 * @author dwj
 */
public enum ObbDetModelEnum {

    YOLOV11("OnnxRuntime", 1024, 1024);

    /**
     * 模型引擎
     */
    private final String engine;

    /**
     * 模型输入尺寸：宽
     */
    private final int inputWidth;

    /**
     * 模型输入尺寸：高
     */
    private final int inputHeight;


    ObbDetModelEnum(String engine, int inputWidth, int inputHeight) {
        this.engine = engine;
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
    }

    public String getEngine() {
        return engine;
    }

    public int getInputWidth() {
        return inputWidth;
    }

    public int getInputHeight() {
        return inputHeight;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static ObbDetModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (ObbDetModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
