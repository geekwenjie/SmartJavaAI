package cn.smartjavaai.speech.asr.pool;

import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperJNI;
import io.github.givimad.whisperjni.WhisperState;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * WhisperState 实例对象池
 * @author dwj
 */
public class WhisperStatePool extends GenericObjectPool<WhisperState> {

    public WhisperStatePool(WhisperJNI whisperInstance, WhisperContext context) {
        super(new PooledObjectFactory<WhisperState>(){

            @Override
            public void activateObject(PooledObject<WhisperState> pooledObject) throws Exception {

            }

            @Override
            public void destroyObject(PooledObject<WhisperState> pooledObject) throws Exception {
                pooledObject.getObject().close();
            }

            @Override
            public PooledObject<WhisperState> makeObject() throws Exception {
                WhisperState state = whisperInstance.initState(context);
                return new DefaultPooledObject(state);
            }

            @Override
            public void passivateObject(PooledObject<WhisperState> pooledObject) throws Exception {

            }

            @Override
            public boolean validateObject(PooledObject<WhisperState> pooledObject) {
                return false;
            }
        });
    }
}
