package cn.smartjavaai.speech.asr.model;

import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.Language;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.asr.audio.SmartAudioFactory;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.*;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.asr.factory.SpeechRecognizerFactory;
import cn.smartjavaai.speech.asr.pool.WhisperStatePool;
import cn.smartjavaai.speech.utils.AudioUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ai.djl.util.JsonUtils.GSON;

/**
 * Vosk 识别器
 * @author dwj
 */
@Slf4j
public class VoskRecognizer implements SpeechRecognizer{

    private Model model;

    private AsrModelConfig config;

    @Override
    public void loadModel(AsrModelConfig config) {
        this.config = config;
        //防止中文乱码
        System.setProperty("jna.encoding","utf-8");
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
                System.load(config.getLibPath().toAbsolutePath().toString());
            }
            model = new Model(config.getModelPath());
            LibVosk.setLogLevel(LogLevel.DEBUG);
            log.debug("Vosk init success");
        } catch (IOException e) {
            throw new AsrException(e);
        }
    }


    @Override
    public R<AsrResult> recognize(String audioPath) {
        return recognize(audioPath, new VoskParams());
    }


    @Override
    public R<AsrResult> recognize(byte[] audioData) {
        return recognize(audioData, new VoskParams());
    }

    @Override
    public R<AsrResult> recognize(InputStream audioStream) {
        return recognize(audioStream, new VoskParams());
    }

    private R<AsrResult> recognizeAudioStream(AudioInputStream ais, RecParams params) {
        try (Recognizer recognizer = buildRecognizer(params, ais.getFormat().getSampleRate())){
            AudioFormat audioFormat = ais.getFormat();
            log.debug("sampleRate:{}", audioFormat.getSampleRate());
            log.debug("channels:{}", audioFormat.getChannels());
            int nbytes;
            byte[] b = new byte[4096];
            List<AsrSegment> segments = new ArrayList<AsrSegment>();
            StringBuilder text = new StringBuilder();
            String temp = "";
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    String result = recognizer.getResult();
//                    log.info("result:{}", result);
                    AsrSegment segment = parseSegment(result, params);
                    if(segment != null){
                        segments.add(segment);
                        text.append(segment.getText());
                    }
                }else{
                    temp = recognizer.getPartialResult();
//                    log.info("temp:{}", temp);
                }
            }
            if(StringUtils.isNotBlank(temp)){
                AsrSegment segment = parsePartialSegment(temp, params);
                if(segment != null){
                    segments.add(segment);
                    text.append(segment.getText());
                }
            }
            //补全结果
            String finalText = recognizer.getFinalResult();
            if(StringUtils.isNotBlank(text.toString()) && StringUtils.isNotBlank(finalText)){
                AsrSegment finalSegment = parseSegment(finalText, params);
                if(finalSegment != null){
                    //需要补全
                    if(!text.toString().endsWith(finalSegment.getText())){
                        AsrSegment alignSegment = VoskRecognizer.alignSegment(segments.get(segments.size() - 1), finalSegment);
                        //如果匹配失败，则直接使用最终片段
                        if(alignSegment != null){
                            segments.set(segments.size() - 1,alignSegment);
                        }else{
                            segments.set(segments.size() - 1,finalSegment);
                        }
                    }
                }
            }
            String result = segments.stream()
                    .map(AsrSegment::getText)
                    .collect(Collectors.joining("\n"));
            return R.ok(new AsrResult(result, segments));
        } catch (IOException e) {
            throw new AsrException(e);
        }
    }

    /**
     * 解析结果
     * @param segment
     * @return
     */
    private AsrSegment parseSegment(String segment, RecParams params) {
        JsonObject json = GSON.fromJson(segment, JsonObject.class);
        JsonArray resultArray = json.getAsJsonArray("result");
        if(Objects.isNull(resultArray) || resultArray.size() == 0){
            return null;
        }
        double segmentStart = resultArray.get(0).getAsJsonObject().get("start").getAsDouble();
        double segmentEnd = resultArray.get(resultArray.size() - 1).getAsJsonObject().get("end").getAsDouble();
        long startMs = Math.round(segmentStart * 1000);
        long endMs = Math.round(segmentEnd * 1000);
        String text = json.get("text").getAsString();
        if(Objects.nonNull(params.getLanguage()) && params.getLanguage() == Language.ZH){
            text = text.replace(" ", "");
        }
        return new AsrSegment(text, startMs, endMs);
    }

    /**
     * 解析结果
     * @param segment
     * @return
     */
    private AsrSegment parsePartialSegment(String segment, RecParams params) {
        JsonObject json = GSON.fromJson(segment, JsonObject.class);
        JsonArray resultArray = json.getAsJsonArray("partial_result");
        if(Objects.isNull(resultArray) || resultArray.size() == 0){
            return null;
        }
        double segmentStart = resultArray.get(0).getAsJsonObject().get("start").getAsDouble();
        double segmentEnd = resultArray.get(resultArray.size() - 1).getAsJsonObject().get("end").getAsDouble();
        long startMs = Math.round(segmentStart * 1000);
        long endMs = Math.round(segmentEnd * 1000);
        String text = json.get("partial").getAsString();
        if(Objects.nonNull(params.getLanguage()) && params.getLanguage() == Language.ZH){
            text = text.replace(" ", "");
        }
        return new AsrSegment(text, startMs, endMs);
    }

    /**
     * 补全
     * @param shortSeg
     * @param longSeg
     * @return
     */
    public static AsrSegment alignSegment(AsrSegment shortSeg, AsrSegment longSeg) {
        String shortText = shortSeg.getText();
        String longText = longSeg.getText();

        int index = longText.indexOf(shortText);
        if (index == -1) {
//            log.debug("短文本不在长文本中");
            return null;
        }
        String resultText = longText.substring(index);
        return new AsrSegment(resultText, shortSeg.getStartTime(), longSeg.getEndTime());
    }


    /**
     * 创建识别器
     * @param params
     * @param sampleRate
     * @return
     * @throws IOException
     */
    private Recognizer buildRecognizer(RecParams params,float sampleRate) throws IOException {
        if(!(params instanceof VoskParams)){
            throw new AsrException("params is not VoskParams");
        }
        VoskParams voskParams = (VoskParams) params;
        Recognizer recognizer = new Recognizer(model, sampleRate);
        if(StringUtils.isNotBlank(voskParams.getGrammar())){
            recognizer.setGrammar(voskParams.getGrammar());
        }
//        if(voskParams.getMaxAlternatives() > 0){
//            recognizer.setMaxAlternatives(voskParams.getMaxAlternatives());
//        }
        //暂时只支持返回一个结果
//        recognizer.setMaxAlternatives(1);
        recognizer.setWords(true);
        recognizer.setPartialWords(true);
        return recognizer;
    }


    @Override
    public R<AsrResult> recognize(String audioPath, RecParams params) {
        Path audioFilePath = Paths.get(audioPath);
        if(!audioFilePath.toFile().exists()){
             return R.fail(R.Status.FILE_NOT_FOUND);
        }
        try (InputStream is = Files.newInputStream(audioFilePath)){
            return recognize(is, params);
        } catch (IOException e) {
            throw new AsrException(e);
        }
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
        AudioInputStream ais = null;
        InputStream tryStream = null;
        InputStream conversionStream = null;
        boolean needConversion = false;
        try {
            // 缓存全部字节数据
            byte[] allBytes = audioStream.readAllBytes();
            // 创建两个独立流
            tryStream = new BufferedInputStream(new ByteArrayInputStream(allBytes));
            conversionStream = new BufferedInputStream(new ByteArrayInputStream(allBytes));
            ais = AudioSystem.getAudioInputStream(tryStream);
            return recognizeAudioStream(ais, params);
        } catch (UnsupportedAudioFileException e) {
            log.debug("Unsupported Audio file, Conversion to WAV is required");
            needConversion = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(tryStream != null){
                try {
                    tryStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //转换wav格式
        File tempFile = null;
        if(needConversion){
            try {
                tempFile = AudioUtils.audioFormatConversion(conversionStream, "wav");
//                log.info("tempFile:{}", tempFile.getAbsolutePath());
            } catch (EncoderException | IOException e) {
                throw new AsrException(e);
            }
            try (InputStream fis = new BufferedInputStream(new FileInputStream(tempFile))){
                ais = AudioSystem.getAudioInputStream(fis);
                return recognizeAudioStream(ais, params);
            } catch (IOException | UnsupportedAudioFileException e) {
                throw new AsrException("音频转换异常", e);
            } finally {
                if(tempFile != null && tempFile.exists()){
                    tempFile.delete();
                }
                if(conversionStream != null){
                    try {
                        conversionStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return R.fail(R.Status.Unknown);
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            SpeechRecognizerFactory.removeFromCache(config.getModelEnum());
        }
        if(model != null){
            model.close();
        }
    }

    /**
     * 创建高级识别器
     * @param sampleRate 采样率
     * @return
     */
    public Recognizer createAdvancedRecognizer(float sampleRate){
        try {
            return new Recognizer(model, sampleRate);
        } catch (IOException e) {
            throw new AsrException(e);
        }
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
