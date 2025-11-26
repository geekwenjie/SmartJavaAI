package cn.smartjavaai.clip.pool;

import ai.djl.Model;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.nlp.preprocess.Tokenizer;
import cn.smartjavaai.clip.translator.ImageTranslator;
import cn.smartjavaai.clip.translator.TextTranslator;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author dwj
 */
public class ClipTextPredictorFactory extends BasePooledObjectFactory<Predictor<String, float[]>> {
    private final Model model;
    private final HuggingFaceTokenizer tokenizer;

    public ClipTextPredictorFactory(Model model, HuggingFaceTokenizer tokenizer) {
        this.model = model;
        this.tokenizer = tokenizer;
    }

    @Override
    public Predictor<String, float[]> create() {
        return model.newPredictor(new TextTranslator(tokenizer));
    }

    @Override
    public PooledObject<Predictor<String, float[]>> wrap(Predictor<String, float[]> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<Predictor<String, float[]>> p) {
        p.getObject().close();
    }
}
