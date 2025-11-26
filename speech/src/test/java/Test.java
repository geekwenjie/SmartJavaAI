import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.Language;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.AsrResult;
import cn.smartjavaai.speech.asr.entity.AsrSegment;
import cn.smartjavaai.speech.asr.entity.RecParams;
import cn.smartjavaai.speech.asr.entity.WhisperParams;
import cn.smartjavaai.speech.asr.enums.AsrModelEnum;
import cn.smartjavaai.speech.asr.model.VoskRecognizer;
import cn.smartjavaai.speech.asr.model.WhisperRecognizer;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;
import ws.schild.jave.info.MultimediaInfo;

import java.io.*;

/**
 * @author dwj
 * @date 2025/8/1
 */
@Slf4j
public class Test {

    public static void main(String[] args) {

//        System.out.println("TMPDIR = " + System.getProperty("java.io.tmpdir"));
//
////        System.setProperty("io.github.givimad.whisperjni.libdir","/Users/wenjie/smartjavaai_cache/whisper");
//        WhisperRecognizer whisperRecognizer = new WhisperRecognizer();
//        AsrModelConfig config = new AsrModelConfig();
//        config.setModelEnum(AsrModelEnum.WHISPER);
////        config.setModelPath("/Users/wenjie/Downloads/ggml-medium.bin");
//        config.setModelPath("/Users/wenjie/Documents/develop/model/speech/ggml-medium.bin");
//        whisperRecognizer.loadModel(config);
//        WhisperParams params = new WhisperParams();
//        WhisperFullParams params1 = new WhisperFullParams(WhisperSamplingStrategy.BEAN_SEARCH);
////        params1.detectLanguage = true;
//        params1.language = Language.ZH.getCode();
//        //params1.translate = true;
//        params1.initialPrompt = "语音模型";
//        params.setParams(params1);
////        params1.printTimestamps = true;
//        params1.printRealtime = true;
//        //params.setLanguage(Language.ZH);
//        R<AsrResult> result = whisperRecognizer.recognize("/Users/wenjie/Downloads/友谊大街.m4a",params);
//        if (result.isSuccess()){
//            System.out.println("结果：" + JsonUtils.toJson(result.getData()));
//        }else{
//            System.out.println(result.getMessage());
//        }
//
//        while (true){
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

        testVosk();

//        testConvert();
    }



    public static void testVosk(){
        System.load("/Users/wenjie/Downloads/vosk-arrch64-dylib-main/libvosk.dylib");
        VoskRecognizer voskRecognizer = new VoskRecognizer();
        AsrModelConfig config = new AsrModelConfig();
        config.setModelEnum(AsrModelEnum.VOSK);
        config.setModelPath("/Users/wenjie/Documents/develop/model/speech/vosk-model-cn-0.22");
        voskRecognizer.loadModel(config);

        R<AsrResult> result = voskRecognizer.recognize("/Users/wenjie/Documents/idea_workplace/SmartJavaAI/examples/speech-examples/src/main/resources/lff_zh.mp3");
        if (result.isSuccess()){
            System.out.println("结果：" + JsonUtils.toJson(result.getData()));
        }else{
            System.out.println(result.getMessage());
        }

//        while (true){
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    public static void testConvert(){
        getAudioFormatConversionIns("/Users/wenjie/Downloads/中国建设银行(包头当代支行).m4a","/Users/wenjie/Downloads/test1.wav","wav");
    }


    /**
     * 音频格式转换
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @param format            wav/mp3/amr
     * @return
     */
    public static byte[] getAudioFormatConversionBytes(String sourceFilePath,String targetFilePath,String format) {
        InputStream fis = null;
        ByteArrayOutputStream bos = null;
        byte[] bytes = null;
        try {
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.isFile()) {
                File targetFile = new File(targetFilePath);

                // 音频格式转换
                audioFormatConversion(sourceFile, targetFile, format);

                fis = new FileInputStream(targetFile);
                bos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bytes = bos.toByteArray();
            }
        } catch (Exception e) {
            log.error("音频格式转换异常：" + e.getMessage(), e);
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                log.error("音频格式转换资源关闭异常：" + e.getMessage(), e);
            }
        }
        return bytes;
    }

    /**
     * 音频格式转换
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @param format            wav/mp3/amr
     * @return
     */
    public static InputStream getAudioFormatConversionIns(String sourceFilePath, String targetFilePath, String format) {
        try {
            File sourceFile = new File(sourceFilePath);
            if (sourceFile.isFile()) {
                File targetFile = new File(targetFilePath);

                // 音频格式转换
                audioFormatConversion(sourceFile, targetFile, format);

                return new FileInputStream(targetFile);
            }
        } catch (Exception e) {
            log.error("音频格式转换异常：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 音频格式转换
     * @param source 源音频文件
     * @param target 输出的音频文件
     * @param format wav/mp3/amr
     */
    public static void audioFormatConversion(File source,File target,String format) {
        try {
            //Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            switch (format) {
                case "wav":
                    audio.setCodec("pcm_s16le");
                    break;
                case "mp3":
                    audio.setCodec("libmp3lame");
                    break;
                case "amr":
                    audio.setCodec("libvo_amrwbenc");
                    break;
                default:
                    log.error("音频格式不合法！");
                    return;
            }
            audio.setBitRate(16000);
            audio.setChannels(1);
            audio.setSamplingRate(16000);
            //Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat(format);
            attrs.setAudioAttributes(audio);
            //Encode
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);
        } catch (Exception e) {
            log.error("音频格式转换异常：" + e.getMessage(), e);
        }
    }


}
