package cn.smartjavaai.face.model.expression.translator;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * @author dwj
 * @date 2025/6/30
 */
public class DenseNetEmotionTranslator implements Translator<Image, Classifications> {

    private final List<String> labels = Arrays.asList("angry", "disgust", "fear", "happy", "sad", "surprise", "neutral");

    private int imageSize = 224;

    public DenseNetEmotionTranslator(int imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        NDArray output = list.singletonOrThrow();
        output = output.softmax(1);
        return new Classifications(labels, output);
    }


    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        // 直接转换为灰度NDArray
        NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
        // 调整大小
        Shape shape = array.getShape();
        long height = shape.get(0);
        long width = shape.get(1);
        if (height != imageSize || width != imageSize) {
            array = NDImageUtils.resize(array, imageSize, imageSize);
        }
        array = NDImageUtils.resize(array, imageSize, imageSize);
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
