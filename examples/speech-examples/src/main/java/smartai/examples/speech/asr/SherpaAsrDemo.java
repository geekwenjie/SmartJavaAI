package smartai.examples.speech.asr;

import ai.djl.modality.audio.Audio;
import ai.djl.modality.audio.AudioFactory;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.Language;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.AsrResult;
import cn.smartjavaai.speech.asr.entity.WhisperParams;
import cn.smartjavaai.speech.asr.enums.AsrModelEnum;
import cn.smartjavaai.speech.asr.factory.SpeechRecognizerFactory;
import cn.smartjavaai.speech.asr.model.SpeechRecognizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * 语音识别ASR demo
 * sherpa-onnx模型及依赖库下载链接:
 * 1、（推荐）依赖库官网下载：https://github.com/k2-fsa/sherpa-onnx/releases
 * 2、（推荐）ASR模型官网下载：https://github.com/k2-fsa/sherpa-onnx/releases/tag/asr-models
 * 3、百度网盘下载：https://pan.baidu.com/s/19p3WhVEM7dgdkvXFaeeAxg?pwd=1234 提取码: 1234
 * @author dwj
 * @date 2025/10/23
 */
@Slf4j
public class SherpaAsrDemo {


    /**
     * 语音识别：Sherpa Paraformer（中文）
     */
    @Test
    public void testSherpaParaformerAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_PARAFORMER);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-paraformer-zh-2023-09-14");
            config.setModelName("model.int8.onnx");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-paraformer-zh-2023-09-14/test_wavs/0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa Dolphin（中文）
     */
    @Test
    public void testSherpaDolphinAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_DOLPHIN);
            config.setModelName("model.int8.onnx");
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-dolphin-base-ctc-multi-lang-int8-2025-04-02");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-dolphin-base-ctc-multi-lang-int8-2025-04-02/test_wavs/0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa zipformer（中文）
     */
    @Test
    public void testSherpaZipformerAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_ZIPFORMERCTC);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-zipformer-ctc-zh-int8-2025-07-03");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            config.setModelName("model.int8.onnx");
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-zipformer-ctc-zh-int8-2025-07-03/test_wavs/0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa FireRedAsr（中英）
     */
    @Test
    public void testSherpaFireRedAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_FIREREDASR);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-fire-red-asr-large-zh_en-2025-02-16");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-fire-red-asr-large-zh_en-2025-02-16/test_wavs/3.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa SenseVoice （多语言模型）
     */
    @Test
    public void testSherpaSenseVoiceAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_SENSEVOICE);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17");
            config.setModelName("model.onnx");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17/test_wavs/zh.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa WenetCtc（多语言模型：粤语）
     */
    @Test
    public void testSherpaWenetCtcAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_WENETCTC);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-wenetspeech-yue-u2pp-conformer-ctc-zh-en-cantonese-int8-2025-09-10");
            config.setModelName("model.int8.onnx");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-wenetspeech-yue-u2pp-conformer-ctc-zh-en-cantonese-int8-2025-09-10/test_wavs/yue-0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa FireRedAsr（方言：四川、天津、河南）
     */
    @Test
    public void testSherpaFireRedAsrSichuan() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_FIREREDASR);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-fire-red-asr-large-zh_en-2025-02-16");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-fire-red-asr-large-zh_en-2025-02-16/test_wavs/3-sichuan.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa Telespeech（方言-天津、河南、四川）
     */
    @Test
    public void testSherpaTelespeechAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_TELESPEECH);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-telespeech-ctc-int8-zh-2024-06-04");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.setModelName("model.int8.onnx");
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-telespeech-ctc-int8-zh-2024-06-04/test_wavs/4-tianjin.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa Nemo（英文）
     */
    @Test
    public void testSherpaNemoAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_NEMO);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-nemo-ctc-en-citrinet-512");
            config.setModelName("model.onnx");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-nemo-ctc-en-citrinet-512/test_wavs/0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa Moonshine（英文）
     */
    @Test
    public void testSherpaMoonshineAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_MOONSHINE);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-moonshine-tiny-en-int8");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 2);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-moonshine-tiny-en-int8/test_wavs/0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 语音识别：Sherpa Whisper（英文）
     */
    @Test
    public void testSherpaWhisperAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_WHISPER);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-whisper-tiny");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-whisper-tiny/test_wavs/0.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa Transducer（英文）
     */
    @Test
    public void testSherpaTransducerAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_TRANSDUCER);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-zipformer-gigaspeech-2023-12-12");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-zipformer-gigaspeech-2023-12-12/test_wavs/1221-135766-0001.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语音识别：Sherpa Canary（德语-英文）
     */
    @Test
    public void testSherpaCanaryAsr() {
        try {
            //获取模型
            AsrModelConfig config = new AsrModelConfig();
            config.setModelEnum(AsrModelEnum.SHERPA_CANARY);
            config.setModelPath("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-nemo-canary-180m-flash-en-es-de-fr-int8");
            config.setLibPath(Paths.get("/Users/wenjie/smartjavaai_cache/sherpa-onnx-v1.12.14-osx-arm64-jni/lib"));
            config.putCustomParam("debug", false);
            config.putCustomParam("numThreads", 1);
            SpeechRecognizer recognizer = SpeechRecognizerFactory.getInstance().getModel(config);
            Audio audio = AudioFactory.newInstance().fromFile(Paths.get("/Users/wenjie/Documents/develop/model/speech/sherpa-asr/sherpa-onnx-nemo-canary-180m-flash-en-es-de-fr-int8/test_wavs/de.wav"));
            R<AsrResult> result = recognizer.recognize(audio);
            if (result.isSuccess()){
                log.info("识别成功:{}", result.getData());
            }else{
                log.error("识别失败:{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }











}
