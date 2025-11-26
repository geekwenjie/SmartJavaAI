package cn.smartjavaai.clip.pool;

import ai.djl.Model;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.util.Pair;
import cn.smartjavaai.clip.translator.ImageTextTranslator;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author dwj
 */
public class ClipImageTextPredictorFactory extends BasePooledObjectFactory<Predictor<Pair<Image, String>, float[]>> {

    private final Model model;

    private final HuggingFaceTokenizer tokenizer;

    public ClipImageTextPredictorFactory(Model model, HuggingFaceTokenizer tokenizer) {
        this.model = model;
        this.tokenizer = tokenizer;
    }

    @Override
    public Predictor<Pair<Image, String>, float[]> create() {
        return model.newPredictor(new ImageTextTranslator(tokenizer));
    }

    @Override
    public PooledObject<Predictor<Pair<Image, String>, float[]>> wrap(Predictor<Pair<Image, String>, float[]> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<Predictor<Pair<Image, String>, float[]>> p) {
        p.getObject().close();
    }

}
