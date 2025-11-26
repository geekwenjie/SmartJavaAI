package cn.smartjavaai.instanceseg.enums;

/**
 * 实例分割模型枚举
 * @author dwj
 */
public enum InstanceSegModelEnum {

    SEG_YOLO11N_PYTORCH("PyTorch", 640, 640, "djl://ai.djl.pytorch/yolo11n-seg"),

    SEG_YOLOV8N_PYTORCH("PyTorch", 640, 640,  "djl://ai.djl.pytorch/yolov8n-seg"),

    SEG_YOLO11N_ONNX("OnnxRuntime", 640, 640,  "djl://ai.djl.onnxruntime/yolo11n-seg"),

    SEG_YOLOV8N_ONNX("OnnxRuntime", 640, 640,  "djl://ai.djl.onnxruntime/yolov8n-seg"),

    SEG_MASK_RCNN("MXNet", 0,0, "djl://ai.djl.mxnet/mask_rcnn");


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static InstanceSegModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (InstanceSegModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    /**
     * 模型输入尺寸：宽
     */
    private final int inputWidth;

    /**
     * 模型输入尺寸：高
     */
    private final int inputHeight;

    private final String modelUri;

    /**
     * 模型引擎
     */
    private final String engine;

    InstanceSegModelEnum(String engine, int inputWidth, int inputHeight, String modelUri) {
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.modelUri = modelUri;
        this.engine = engine;
    }

    public String getModelUri() {
        return modelUri;
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
}
