package cn.smartjavaai.speech.asr.model;

import ai.djl.modality.audio.Audio;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.AsrResult;
import cn.smartjavaai.speech.asr.entity.RecParams;
import cn.smartjavaai.speech.asr.factory.SherpaOfflineAsrModelConfigFactory;
import cn.smartjavaai.speech.tts.exception.TtsException;
import cn.smartjavaai.speech.tts.factory.SherpaOfflineTtsModelConfigFactory;
import com.k2fsa.sherpa.onnx.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author dwj
 */
@Slf4j
public class SherpaRecognizer implements SpeechRecognizer{

    private AsrModelConfig config;
    private OfflineRecognizer recognizer;

    @Override
    public void loadModel(AsrModelConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new TtsException("modelPath is null");
        }
        Path testModelPath = Paths.get(config.getModelPath());
        if(!testModelPath.toFile().exists()){
            throw new TtsException("modelPath does not exist: " + testModelPath.toAbsolutePath());
        }
        if(Objects.isNull(config.getLibPath())){
            throw new TtsException("libPath is null");
        }
        if(!config.getLibPath().toFile().exists()){
            throw new TtsException("libPath does not exist: " + testModelPath.toAbsolutePath());
        }
        try {
            //加载依赖库
            System.setProperty("sherpa_onnx.native.path",config.getLibPath().toAbsolutePath().toString());
            OfflineRecognizerConfig offlineRecognizerConfig = SherpaOfflineAsrModelConfigFactory.createConfig(config);
            recognizer = new OfflineRecognizer(offlineRecognizerConfig);
            log.debug("Sherpa tts init success");
        } catch (Exception e) {
            throw new TtsException(e);
        }
    }

    @Override
    public R<AsrResult> recognize(Audio audio) {
        if(recognizer == null){
            throw new TtsException("模型未初始化");
        }
        OfflineStream stream = recognizer.createStream();
        stream.acceptWaveform(audio.getData(), (int)audio.getSampleRate());
        recognizer.decode(stream);
        String text = recognizer.getResult(stream).getText();
        stream.release();
        return R.ok(new AsrResult(text));

    }

    @Override
    public R<AsrResult> recognize(String audioPath) {
        if(recognizer == null){
            throw new TtsException("模型未初始化");
        }
        OfflineStream stream = recognizer.createStream();
        WaveReader reader = new WaveReader(audioPath);
        stream.acceptWaveform(reader.getSamples(), reader.getSampleRate());
        recognizer.decode(stream);
        String text = recognizer.getResult(stream).getText();
        stream.release();
        return R.ok(new AsrResult(text));
    }

    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }

    @Override
    public void close() throws Exception {
        if(recognizer != null){
            recognizer.release();
        }
    }
}
