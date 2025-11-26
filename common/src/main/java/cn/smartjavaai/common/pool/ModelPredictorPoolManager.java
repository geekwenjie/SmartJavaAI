package cn.smartjavaai.common.pool;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 模型共享池管理器
 * @author dwj
 * @date 2025/4/8
 */
public class ModelPredictorPoolManager {

    // 每个模型的唯一key -> 对应Predictor池
    private final Map<String, GenericObjectPool<? extends Predictor<?, ?>>> poolMap = new ConcurrentHashMap<>();

    /**
     * 注册模型池
     * @param key 模型标识符（自定义，如模型路径、模型名等）
     * @param model 模型本体
     * @param config 池配置（可选）
     */
    public <I, O> void registerModel(String key, ZooModel<I, O> model, GenericObjectPoolConfig<Predictor<I, O>> config) {
        PredictorFactory<I, O> factory = new PredictorFactory<>(model);
        GenericObjectPool<Predictor<I, O>> pool = new GenericObjectPool<>(factory, config);
        poolMap.put(key, pool);
    }

    /**
     * 借出一个 Predictor
     */
    @SuppressWarnings("unchecked")
    public <I, O> Predictor<I, O> borrowPredictor(String key) throws Exception {
        GenericObjectPool<Predictor<I, O>> pool = (GenericObjectPool<Predictor<I, O>>) poolMap.get(key);
        if (pool == null) {
            throw new IllegalArgumentException("模型未注册: " + key);
        }
        return pool.borrowObject();
    }

    /**
     * 归还一个 Predictor
     */
    @SuppressWarnings("unchecked")
    public <I, O> void returnPredictor(String key, Predictor<I, O> predictor) {
        GenericObjectPool<Predictor<I, O>> pool = (GenericObjectPool<Predictor<I, O>>) poolMap.get(key);
        if (pool != null) {
            pool.returnObject(predictor);
        }
    }

    /**
     * 销毁全部池
     */
    public void closeAll() {
        for (GenericObjectPool<? extends Predictor<?, ?>> pool : poolMap.values()) {
            pool.close();
        }
        poolMap.clear();
    }


}
