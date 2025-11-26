package cn.smartjavaai.face.model.facerec.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.face.model.facerec.FaceRecPreprocessConfig;

/**
 * 通用人脸识别模型转换器
 * @author dwj
 */
public class CommonFaceRecTranslator implements Translator<Image, float[]> {

    private FaceRecPreprocessConfig preprocessConfig;

    public CommonFaceRecTranslator(FaceRecPreprocessConfig preprocessConfig) {
        this.preprocessConfig = preprocessConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray array = input.toNDArray(ctx.getNDManager(), preprocessConfig.getImageFlag());
        NDList ndList = null;
        if(preprocessConfig.isUsePipeline()){
            Pipeline pipeline = new Pipeline();
            if(input.getWidth() != preprocessConfig.getInputWidth() || input.getHeight() != preprocessConfig.getInputHeight()){
                pipeline.add(new Resize(preprocessConfig.getInputWidth(), preprocessConfig.getInputHeight()));
            }
            pipeline.add(new ToTensor());
            if(preprocessConfig.isNormalize()){
                pipeline.add(new Normalize(
                        preprocessConfig.getMean(),
                        preprocessConfig.getStd()));
            }
            ndList = pipeline.transform(new NDList(array));
        }else{
            if(input.getWidth() != preprocessConfig.getInputWidth() || input.getHeight() != preprocessConfig.getInputHeight()){
                array = NDImageUtils.resize(array, preprocessConfig.getInputWidth(), preprocessConfig.getInputHeight());
            }
            array = array.toType(DataType.FLOAT32, false);
            if (preprocessConfig.isNormalize()){
                array = array.sub(preprocessConfig.getMean()[0]).div(preprocessConfig.getStd()[0]);
            }
            array = array.transpose(2, 0, 1);
            return new NDList(array);
        }
        return ndList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        NDArray embedding = list.get(preprocessConfig.getOutputIndex());
        embedding = embedding.div(embedding.norm()); // L2归一化
        return embedding.toFloatArray();
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }

}
