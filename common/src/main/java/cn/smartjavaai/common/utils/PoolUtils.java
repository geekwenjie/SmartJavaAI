package cn.smartjavaai.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;

/**
 * @author dwj
 * @date 2025/5/7
 */
@Slf4j
public class PoolUtils {

    // 泛型方法，支持任意类型的 Predictor 和对象池
    public static <T> void returnToPool(ObjectPool<T> pool, T predictor) {
        if (pool == null || predictor == null) {
            return;
        }
        try {
            pool.returnObject(predictor);
        } catch (Exception e) {
            log.warn("归还Predictor到池失败", e);
        }
    }

}
