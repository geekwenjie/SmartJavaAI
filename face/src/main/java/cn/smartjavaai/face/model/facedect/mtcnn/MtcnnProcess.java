package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dwj
 */
public class MtcnnProcess {


    /**
     * 预处理
     * @param image
     * @return
     */
    public static NDArray processInput(NDManager manager, Image image){
        // (N, C, H, W)
        // Image -> NDArray (H, W, C)
        NDArray array = image.toNDArray(manager, Image.Flag.COLOR);
        // 增加 batch 维度 (1, H, W, C) ----
        array = array.expandDims(0);
        // 交换维度 (N, C, H, W)
        array = array.transpose(0, 3, 1, 2);
        // 转成模型的数据类型
        if (!array.getDataType().equals(DataType.FLOAT32)) {
            array = array.toType(DataType.FLOAT32, false);
        }
        return array;
    }

    /**
     * 生成金字塔缩放比例列表
     * @param image
     * @return
     */
    public static List<Double> generateScales(Image image){
        long h = image.getHeight();
        long w = image.getWidth();
        // 计算最小缩放比例
        double minsize = 20;
        double m = 12.0 / minsize;
        double minl = Math.min(h, w) * m;

        // 创建金字塔缩放比例列表
        double factor = 0.709;  // 你原代码的 factor
        List<Double> scales = new ArrayList<>();
        double scale_i = m;
        while (minl >= 12) {
            scales.add(scale_i);
            scale_i *= factor;
            minl *= factor;
        }
        return scales;
    }


}
