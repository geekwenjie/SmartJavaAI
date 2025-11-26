package cn.smartjavaai.speech.asr.model;

import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import cn.smartjavaai.common.entity.Language;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.speech.asr.audio.SmartAudioFactory;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.AsrResult;
import cn.smartjavaai.speech.asr.entity.AsrSegment;
import cn.smartjavaai.speech.asr.entity.RecParams;
import cn.smartjavaai.speech.asr.entity.WhisperParams;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.asr.factory.SpeechRecognizerFactory;
import cn.smartjavaai.speech.asr.pool.WhisperStatePool;
import io.github.givimad.whisperjni.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author dwj
 */
@Slf4j
public class WhisperRecognizer implements SpeechRecognizer{

    private WhisperJNI whisper;

    private WhisperContext ctx;

    private WhisperStatePool statePool;

    private AsrModelConfig config;

    @Override
    public void loadModel(AsrModelConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new AsrException("modelPath is null");
        }
        Path testModelPath = Paths.get(config.getModelPath());
        if(!testModelPath.toFile().exists()){
            throw new AsrException("Missing model file: " + testModelPath.toAbsolutePath());
        }
        try {
            //加载自定义依赖库
            if(Objects.nonNull(config.getLibPath())){
                System.setProperty("io.github.givimad.whisperjni.libdir",config.getLibPath().toAbsolutePath().toString());
            }
            WhisperJNI.loadLibrary();
            WhisperJNI.setLibraryLogger(null);
            whisper = new WhisperJNI();
            WhisperJNI.setLibraryLogger(new WhisperJNI.LibraryLogger() {
                @Override
                public void log(String s) {
                    log.debug("WhisperJNI {}", s);
                }
            });
            ctx = whisper.initNoState(testModelPath);
            Boolean initOpenVINO = config.getCustomParam("initOpenVINO", Boolean.class);
            if(Objects.nonNull(initOpenVINO) && initOpenVINO){
                String device = Objects.isNull(config.getDevice()) ? DeviceEnum.CPU.name() : config.getDevice().name();
                whisper.initOpenVINO(ctx, device);
                log.debug("WhisperJNI initOpenVINO success");
            }
            statePool = new WhisperStatePool(whisper, ctx);
            log.debug("WhisperJNI init success");
        } catch (IOException e) {
            throw new AsrException(e);
        }

    }


    @Override
    public R<AsrResult> recognize(String audioPath, RecParams params) {
        Path audioFilePath = Paths.get(audioPath);
        if(!audioFilePath.toFile().exists()){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Audio audio = null;
        try {
            audio = SmartAudioFactory.getInstance().fromFile(audioFilePath, getDefaultAudioFormat());
        } catch (IOException e) {
            throw new AsrException("读取音频异常",e);
        }
        return recognize(audio, params);
    }

    @Override
    public R<AsrResult> recognize(byte[] audioData, RecParams params) {
        try (InputStream is = new ByteArrayInputStream(audioData)) {
            return recognize(is, params);
        } catch (IOException e) {
            throw new AsrException(e);
        }
    }

    @Override
    public R<AsrResult> recognize(InputStream audioStream, RecParams params) {
        Audio audio = null;
        try {
            audio = SmartAudioFactory.getInstance().fromInputStream(audioStream, getDefaultAudioFormat());
        } catch (IOException e) {
            throw new AsrException("读取音频异常",e);
        }
        return recognize(audio, params);
    }

    public R<AsrResult> recognize(Audio audio, RecParams params) {
        WhisperState state = null;
        StringBuilder text = new StringBuilder();
        try {
            WhisperParams whisperParams = (WhisperParams) params;
            if(Objects.isNull(whisperParams.getParams().language)){
                return R.fail(1003, "请指定语言");
            }
            //不是英语，需要检查是否是多语言模型
            if(!Language.EN.getCode().equals(whisperParams.getParams().language)){
                if(!whisper.isMultilingual(ctx)){
                    return R.fail(1002, "当前为非多语种模型，仅支持英语识别，暂不支持其他语言，请更换多语种模型");
                }
            }
            state = statePool.borrowObject();
            int result = whisper.fullWithState(ctx, state, whisperParams.getParams(), audio.getData(), audio.getData().length);
            if(result != 0) {
                return R.fail(1000, "Transcription failed with code " + result);
            }
            int numSegments = whisper.fullNSegmentsFromState(state);
            if(numSegments <= 0){
                return R.fail(1001, "未识别出有效语音");
            }
            List<AsrSegment> segments = new ArrayList<AsrSegment>(numSegments);
            for(int i = 0; i < numSegments; i++){
                long startTime = whisper.fullGetSegmentTimestamp0FromState(state,i);
                long endTime = whisper.fullGetSegmentTimestamp1FromState(state,i);
                String content = whisper.fullGetSegmentTextFromState(state, i);
                segments.add(new AsrSegment(content, startTime * 10, endTime * 10));
                text.append(content).append("\n");
            }
            return R.ok(new AsrResult(text.toString(), segments));
        } catch (Exception e) {
            throw new AsrException(e);
        } finally {
            if(state != null){
                try {
                    statePool.returnObject(state);
                } catch (Exception e) {
                    log.warn("returnObject失败", e);
                }
            }
        }
    }

    @Override
    public R<AsrResult> recognize(String audioPath) {
        return recognize(audioPath, new WhisperParams());
    }

    @Override
    public R<AsrResult> recognize(byte[] audioData) {
        return recognize(audioData, new WhisperParams());
    }

    @Override
    public R<AsrResult> recognize(InputStream audioStream) {
        return recognize(audioStream, new WhisperParams());
    }

    public WhisperGrammar parseGrammar(String grammarText) {
        try {
            return whisper.parseGrammar(grammarText);
        } catch (IOException e) {
            throw new AsrException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            SpeechRecognizerFactory.removeFromCache(config.getModelEnum());
        }
        if(statePool != null){
            statePool.close();
        }
        if(ctx != null){
            ctx.close();
        }
    }

    /**
     * 获取一个WhisperState对象
     * @return
     */
    public WhisperState getWhisperState(){
        try {
            return statePool.borrowObject();
        } catch (Exception e) {
            throw new AsrException(e);
        }
    }

    private AudioFormat getDefaultAudioFormat(){
        return new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000,
                16,
                1,
                2,
                16000,
                false
        );
    }

    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }
}
