package cn.smartjavaai.face.enums;

/**
 * 人脸识别模型枚举
 * @author dwj
 */
public enum FaceRecModelEnum {

    FACENET_MODEL("PyTorch", 112, 112, 0.7f),
    SEETA_FACE6_MODEL("c++", 0, 0, 0.62f),
    SEETA_FACE6_LIGHT_MODEL("c++", 0, 0, 0.62f),
    INSIGHT_FACE_IRSE50_MODEL("PyTorch", 112, 112, 0.62f),
    INSIGHT_FACE_MOBILE_FACENET_MODEL("PyTorch", 112, 112, 0.64f),
    ELASTIC_FACE_MODEL("PyTorch", 112, 112, 0.61f),
    SPHERE_FACE_20A_ONNX("OnnxRuntime", 96, 112, 0.7f),
    SPHERE_FACE_20A_PT("PyTorch", 96, 112, 0.7f),
    DREAM_IJBA_RES18_NAIVE("OnnxRuntime", 224, 224, 0.74f),
    EVOLVE_FACE_IR50("PyTorch", 112, 112, 0.62f),
    EVOLVE_FACE_IR50_ASIA("PyTorch", 112, 112, 0.62f),
    EVOLVE_FACE_IR152("PyTorch", 112, 112, 0.62f),
    VGG_FACE("PyTorch", 224, 224, 0.75f);

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
     * 相似度阈值
     */
    private final float threshold;

    FaceRecModelEnum(String engine, int inputWidth, int inputHeight, float threshold) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.engine = engine;
        this.threshold = threshold;
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

    public float getThreshold() {
        return threshold;
    }

    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static FaceRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (FaceRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
