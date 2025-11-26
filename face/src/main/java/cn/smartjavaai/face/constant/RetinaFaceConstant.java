package cn.smartjavaai.face.constant;

/**
 * RetinaFace人脸检测模型常量
 * @author dwj
 * @date 2025/7/2
 */
public class RetinaFaceConstant {

    /**
     * 特征图层的基础缩放比例
     */
    public static final int[][] scales = {{16, 32}, {64, 128}, {256, 512}};
    /**
     * 特征图相对于原图的采样步长
     */
    public static final int[] steps = {8, 16, 32};
    /**
     * 缩放系数
     */
    public static final double[] variance = {0.1f, 0.2f};


}
