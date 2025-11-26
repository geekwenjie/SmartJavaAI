package cn.smartjavaai.common.pool;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Translator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Predictor 工厂类
 * @author dwj
 * @date 2025/4/8
 */
@Slf4j
public class PredictorFactory<I, O> extends BasePooledObjectFactory<Predictor<I, O>> {
    private final ZooModel<I, O> model;

    public PredictorFactory(ZooModel<I, O> model) {
        this.model = model;
    }

    @Override
    public Predictor<I, O> create() {
        log.debug("create predictor");
        return model.newPredictor();
    }

    @Override
    public PooledObject<Predictor<I, O>> wrap(Predictor<I, O> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<Predictor<I, O>> p) {
        log.debug("close predictor");
        p.getObject().close();
    }
}
