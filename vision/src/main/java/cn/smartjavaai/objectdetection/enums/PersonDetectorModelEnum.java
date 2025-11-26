package cn.smartjavaai.objectdetection.enums;

/**
 * 目标检测模型枚举
 * @author dwj
 * @date 2025/4/4
 */
public enum PersonDetectorModelEnum {

    YOLOV8_PERSON("OnnxRuntime", 1280);


    /**
     * 模型输入尺寸
     */
    private final int inputSize;

    /**
     * 模型引擎
     */
    private final String engine;

    PersonDetectorModelEnum(String engine, int inputSize) {
        this.inputSize = inputSize;
        this.engine = engine;
    }


    public int getInputSize() {
        return inputSize;
    }

    public String getEngine() {
        return engine;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static PersonDetectorModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (PersonDetectorModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
