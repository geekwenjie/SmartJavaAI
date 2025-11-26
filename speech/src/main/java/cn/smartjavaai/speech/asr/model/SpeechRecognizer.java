package cn.smartjavaai.speech.asr.model;

import ai.djl.modality.audio.Audio;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.AsrResult;
import cn.smartjavaai.speech.asr.entity.RecParams;
import cn.smartjavaai.speech.asr.exception.AsrException;

import java.io.InputStream;

/**
 * 语音识别
 * @author dwj
 */
public interface SpeechRecognizer extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(AsrModelConfig config); // 加载模型


    default R<AsrResult> recognize(String audioPath, RecParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(byte[] audioData, RecParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(InputStream audioStream, RecParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(String audioPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(byte[] audioData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(InputStream audioStream){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(Audio audio){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<AsrResult> recognize(Audio audio, RecParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }
}
