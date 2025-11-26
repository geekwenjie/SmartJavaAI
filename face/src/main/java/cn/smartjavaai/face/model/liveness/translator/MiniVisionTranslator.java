package cn.smartjavaai.face.model.liveness.translator;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Pad;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.*;

import java.util.Arrays;

/**
 * minivision translator
 * @author dwj
 * @date 2025/6/27
 */
public class MiniVisionTranslator implements Translator<Image, float[]> {


    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        NDArray prob = list.singletonOrThrow();
        NDArray softmax = prob.softmax(-1);
        return softmax.toFloatArray();
    }


    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
        array = array.toType(ai.djl.ndarray.types.DataType.FLOAT32, false);
        // 调整数据布局: HWC -> CHW
        array = array.transpose(2, 0, 1);
        // 添加batch维度 (NCHW)
        array = array.expandDims(0);
        return new NDList(array);

    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }

}
