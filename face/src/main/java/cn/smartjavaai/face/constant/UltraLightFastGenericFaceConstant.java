package cn.smartjavaai.face.constant;

/**
 * UltraLightFastGenericFace人脸检测模型常量
 * @author dwj
 */
public class UltraLightFastGenericFaceConstant {

    /**
     * 特征图层的基础缩放比例
     */
    public static final int[][] scales = {{10, 16, 24}, {32, 48}, {64, 96}, {128, 192, 256}};
    /**
     * 特征图相对于原图的采样步长
     */
    public static final int[] steps = {8, 16, 32, 64};
    /**
     * 缩放系数
     */
    public static final double[] variance = {0.1f, 0.2f};

    /**
     * 模型下载地址
     */
    public static final String MODEL_URL = "https://resources.djl.ai/test-models/pytorch/ultranet.zip";

}
