package cn.smartjavaai.pose.enums;

/**
 * 姿态估计模型枚举
 * @author dwj
 */
public enum PoseModelEnum {

    YOLO11N_POSE_PT("djl://ai.djl.pytorch/yolo11n-pose"),

    YOLOV8N_POSE_PT("djl://ai.djl.pytorch/yolov8n-pose"),
    YOLO11N_POSE_ONNX("djl://ai.djl.onnxruntime/yolo11n-pose"),

    YOLOV8N_POSE_ONNX("djl://ai.djl.onnxruntime/yolov8n-pose");

//    SIMPLE_POSE_MXNET("djl://ai.djl.mxnet/simple_pose");


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static PoseModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (PoseModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }

    private final String modelUri;

    PoseModelEnum(String modelUri) {
        this.modelUri = modelUri;
    }

    public String getModelUri() {
        return modelUri;
    }

}
