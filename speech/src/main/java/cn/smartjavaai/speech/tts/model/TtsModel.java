package cn.smartjavaai.speech.tts.model;

import ai.djl.modality.audio.Audio;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.tts.config.TtsModelConfig;
import cn.smartjavaai.speech.tts.entity.TtsParams;

/**
 * tts模型
 * @author dwj
 */
public interface TtsModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(TtsModelConfig config);

    default R<Audio> generate(String text, TtsParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void generate(String text, TtsParams params, String savePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
