package cn.smartjavaai.face.model.liveness.translator;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.Arrays;


public class IicFrTranslator implements Translator<Image, Float> {


    @Override
    public Float processOutput(TranslatorContext ctx, NDList list) {
        NDArray prob = list.singletonOrThrow();
        return prob.toFloatArray()[1];
    }


    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        array = array.transpose(2, 0, 1);
        array = array.expandDims(0);
        // 归一化
        array = array.toType(DataType.FLOAT32, false).div(255.0f);
        return new NDList(array);

    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }
}
