package cn.smartjavaai.speech.tts.model;

import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.asr.pool.WhisperStatePool;
import cn.smartjavaai.speech.tts.config.TtsModelConfig;
import cn.smartjavaai.speech.tts.entity.SherpaTtsParams;
import cn.smartjavaai.speech.tts.entity.TtsParams;
import cn.smartjavaai.speech.tts.exception.TtsException;
import cn.smartjavaai.speech.tts.factory.SherpaOfflineTtsModelConfigFactory;
import com.k2fsa.sherpa.onnx.GeneratedAudio;
import com.k2fsa.sherpa.onnx.OfflineTts;
import com.k2fsa.sherpa.onnx.OfflineTtsCallback;
import com.k2fsa.sherpa.onnx.OfflineTtsConfig;
import io.github.givimad.whisperjni.WhisperJNI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author dwj
 */
@Slf4j
public class SherpaTtsModel implements TtsModel{

    private TtsModelConfig config;

    private OfflineTts offlineTts;


    @Override
    public void loadModel(TtsModelConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new TtsException("modelPath is null");
        }
        if(StringUtils.isBlank(config.getModelName())){
            throw new TtsException("modelName is null");
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
            OfflineTtsConfig offlineTtsConfig = SherpaOfflineTtsModelConfigFactory.createConfig(config);
            offlineTts = new OfflineTts(offlineTtsConfig);
            log.debug("Sherpa tts init success");
        } catch (Exception e) {
            throw new TtsException(e);
        }
    }

    private GeneratedAudio generateCore(String text, TtsParams params) {
        if(offlineTts == null){
            throw new TtsException("模型未初始化");
        }
        SherpaTtsParams sherpaTtsParams =null;
        if(params == null){
            sherpaTtsParams = new SherpaTtsParams();
        }else{
            if(params instanceof SherpaTtsParams){
                sherpaTtsParams = (SherpaTtsParams) params;
            }else{
                throw new TtsException("params参数类型不是 SherpaTtsParams");
            }
        }
        try {
            int sid = 0;
            float speed = 1.0f;
            if(params != null){
                sid = sherpaTtsParams.getSpeakerId();
                speed = sherpaTtsParams.getSpeed() > 0.0f ? sherpaTtsParams.getSpeed() : 1.0f;
            }
            GeneratedAudio audio = null;
            if(sherpaTtsParams.getCallback() != null){
                audio = offlineTts.generateWithCallback(text, sid, speed, sherpaTtsParams.getCallback());
            }else{
                audio = offlineTts.generate(text, sid, speed);
            }
            float audioDuration = audio.getSamples().length / (float) audio.getSampleRate();
            log.debug("-- audio duration: {} seconds", String.format("%.3f", audioDuration));
            return audio;
        } catch (Exception e) {
            throw new TtsException(e);
        }
    }

    @Override
    public R<Audio> generate(String text, TtsParams params) {
        GeneratedAudio audio = generateCore(text, params);
        return R.ok(new Audio(audio.getSamples(), audio.getSampleRate(), 1));
    }

    @Override
    public void generate(String text, TtsParams params, String savePath) {
        GeneratedAudio audio = generateCore(text, params);
        audio.save(savePath);
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
        if(offlineTts != null){
            offlineTts.release();
        }
    }
}
