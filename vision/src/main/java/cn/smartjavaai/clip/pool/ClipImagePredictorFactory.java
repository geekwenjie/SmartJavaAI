package cn.smartjavaai.clip.pool;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.clip.translator.ImageTranslator;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author dwj
 * @date 2025/10/20
 */
public class ClipImagePredictorFactory extends BasePooledObjectFactory<Predictor<Image, float[]>> {
    private final Model model;

    public ClipImagePredictorFactory(Model model) {
        this.model = model;
    }

    @Override
    public Predictor<Image, float[]> create() {
        return model.newPredictor(new ImageTranslator());
    }

    @Override
    public PooledObject<Predictor<Image, float[]>> wrap(Predictor<Image, float[]> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<Predictor<Image, float[]>> p) {
        p.getObject().close();
    }
}
