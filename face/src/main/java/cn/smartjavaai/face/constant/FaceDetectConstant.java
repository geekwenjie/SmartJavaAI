package cn.smartjavaai.face.constant;

/**
 * 人脸检测常量
 * @author dwj
 */
public class FaceDetectConstant {

    /**
     * 置信度阈值
     */
    public static final float DEFAULT_CONFIDENCE_THRESHOLD = 0.85F;

    /**
     * 每张特征图保留的最大候选框数量
     */
    public static final int MAX_FACE_LIMIT = 5000;

    /**
     * nms阈值:控制重叠框的合并程度
     */
    public static final float NMS_THRESHOLD = 0.45F;

    /**
     * 默认相似度阈值
     */
    public static final float SEETAFACE_DEFAULT_SIMILARITY_THRESHOLD = 0.85F;

    /**
     * 默认相似度阈值
     */
    public static final float FACENET_DEFAULT_SIMILARITY_THRESHOLD = 0.8F;



}
