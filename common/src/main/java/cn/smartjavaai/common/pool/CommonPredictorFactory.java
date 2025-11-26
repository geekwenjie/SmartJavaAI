package cn.smartjavaai.common.pool;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoBatchifyTranslator;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author dwj
 * @date 2025/6/14
 */
public class CommonPredictorFactory extends BasePooledObjectFactory<Predictor<?, ?>> {

    private final ZooModel<?, ?> model;
    private final NoBatchifyTranslator<?, ?> translator;

    public CommonPredictorFactory(ZooModel<?, ?> model, NoBatchifyTranslator<?, ?> translator) {
        this.model = model;
        this.translator = translator;
    }

    @Override
    public Predictor<?, ?> create() {
        return model.newPredictor(translator);
    }

    @Override
    public PooledObject<Predictor<?, ?>> wrap(Predictor<?, ?> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<Predictor<?, ?>> p) {
        p.getObject().close();
    }
}
