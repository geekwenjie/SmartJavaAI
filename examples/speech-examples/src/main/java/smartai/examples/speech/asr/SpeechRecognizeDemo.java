package smartai.examples.speech.asr;

import ai.djl.util.JsonUtils;
import cn.hutool.core.io.FileUtil;
import cn.smartjavaai.common.entity.Language;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.entity.AsrResult;
import cn.smartjavaai.speech.asr.entity.VoskParams;
import cn.smartjavaai.speech.asr.entity.WhisperParams;
import cn.smartjavaai.speech.asr.enums.AsrModelEnum;
import cn.smartjavaai.speech.asr.factory.SpeechRecognizerFactory;
import cn.smartjavaai.speech.asr.model.SpeechRecognizer;
import cn.smartjavaai.speech.asr.model.VoskRecognizer;
import cn.smartjavaai.speech.asr.model.WhisperRecognizer;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperGrammar;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import io.github.givimad.whisperjni.WhisperState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * 语音识别demo（Vosk、Whisper）
 * 模型下载网盘：https://pan.baidu.com/s/1kiMF5MF641R7LTn1GpB2lQ?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class SpeechRecognizeDemo {



    /**
     * 获取Whisper模型
     * 模型下载网盘：https://pan.baidu.com/s/1kiMF5MF641R7LTn1GpB2lQ?pwd=1234 提取码: 1234
     * 更多模型下载地址：https://huggingface.co/ggerganov/whisper.cpp/tree/main
     * @return
     */
    public SpeechRecognizer getWhisperRecognizer() {
        AsrModelConfig config = new AsrModelConfig();
        config.setModelEnum(AsrModelEnum.WHISPER);
        //模型下载地址：https://huggingface.co/ggerganov/whisper.cpp/tree/main
        config.setModelPath("/Users/xxx/Documents/develop/model/speech/ggml-medium.bin");
        return SpeechRecognizerFactory.getInstance().getModel(config);
    }


    /**
     * Whisper 语音识别
     * 多语言模型支持100种语言
     * 注意事项：
     * 1、不支持centos7
     * 2、模型越大越准确
     * 3、暂不支持GPU使用，如需GPU使用需要自行编译：https://github.com/ggml-org/whisper.cpp/tree/master?tab=readme-ov-file#nvidia-gpu-support
     */
    @Test
    public void testWhisper() {
        try {
            SpeechRecognizer recognizer = getWhisperRecognizer();
            WhisperParams params = new WhisperParams();
            //语言：中文
            params.setLanguage(Language.ZH);
            R<AsrResult> result = recognizer.recognize("src/main/resources/speech_zh.mp3", params);
            if (result.isSuccess()){
                log.info("识别成功:{}", JsonUtils.toJson(result.getData()));
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Whisper 语音识别(使用个性化配置)
     * 多语言模型支持100种语言
     * 注意事项：
     * 1、不支持centos7
     * 2、模型越大越准确
     * 3、暂不支持GPU使用，如需GPU使用需要自行编译：https://github.com/ggml-org/whisper.cpp/tree/master?tab=readme-ov-file#nvidia-gpu-support
     */
    @Test
    public void testWhisperWithCustomConfig() {
        try {
            SpeechRecognizer recognizer = getWhisperRecognizer();
            WhisperParams params = new WhisperParams();
            //语言：中文
            params.setLanguage(Language.ZH);
            /**
             * 解码搜索策略类型：
             * GREEDY - 贪婪解码，逐步选择概率最高的结果；
             * BEAN_SEARCH - Beam 搜索，保留多个候选路径以提高准确性。
             */
            WhisperFullParams fullParams = new WhisperFullParams(WhisperSamplingStrategy.BEAN_SEARCH);
            //语言
            fullParams.language = Language.ZH.getCode();
            //线程数，设为 0 表示使用最大核心数。
            fullParams.nThreads = 0;
            //解码器使用的历史文本作为提示的最大 token 数。
            fullParams.nMaxTextCtx = 16384;
            //解码起始偏移（毫秒）
            fullParams.offsetMs = 0;
            //解码持续时长（毫秒），超过此长度的音频将被截断
            fullParams.durationMs = 0;
            //是否翻译为英文
            fullParams.translate = false;
            // 初始提示，用于提供上下文或样例，帮助模型更准确地理解语音内容
            fullParams.initialPrompt = "简体中文";
            //禁用上下文链接，不使用前一段解码结果作为上下文
            fullParams.noContext = true;
            //是否强制仅输出一个段落（适用于短语音）
            fullParams.singleSegment = false;
            //是否打印特殊标记
            fullParams.printSpecial = false;
            //是否直接从 whisper.cpp 中打印结果（不推荐，建议使用回调方式替代）
            fullParams.printRealtime = false;
            //抑制非语音 token输出
            fullParams.suppressNonSpeechTokens = false;
            //更多参数请查看官网：https://github.com/GiviMAD/whisper-jni/blob/33854520b1f0b3697106a7932a2fd64e8191bca9/src/main/java/io/github/givimad/whisperjni/WhisperFullParams.java
            params.setParams(fullParams);
            //建议上传 WAV 格式音频。其他格式将自动转换为 WAV，可能影响处理速度
            R<AsrResult> result = recognizer.recognize("src/main/resources/speech_zh.mp3", params);
            if (result.isSuccess()){
                log.info("识别成功:{}", JsonUtils.toJson(result.getData()));
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Whisper 语音识别(使用Grammar语法规则)
     * 多语言模型支持100种语言
     * 注意事项：
     * 1、不支持centos7
     * 2、模型越大越准确
     * 3、暂不支持GPU使用，如需GPU使用需要自行编译：https://github.com/ggml-org/whisper.cpp/tree/master?tab=readme-ov-file#nvidia-gpu-support
     */
    @Test
    public void testWhisperWithGrammar() {
        try {
            WhisperRecognizer whisperRecognizer = (WhisperRecognizer)getWhisperRecognizer();
            //语法规则
            String grammarText = "root ::= \" And so, my fellow American, ask not what your country can do for you, ask what you can do for your country.\"";
            try (WhisperGrammar grammar = whisperRecognizer.parseGrammar(grammarText)){
                WhisperParams params = new WhisperParams();
                WhisperFullParams fullParams = new WhisperFullParams(WhisperSamplingStrategy.BEAN_SEARCH);
                //语言：英文
                fullParams.language = Language.EN.getCode();
                fullParams.grammar = grammar;
                params.setParams(fullParams);
                //建议上传 WAV 格式音频。其他格式将自动转换为 WAV，可能影响处理速度
                R<AsrResult> result = whisperRecognizer.recognize("src/main/resources/jfk_en.wav", params);
                if (result.isSuccess()){
                    log.info("识别成功:{}", JsonUtils.toJson(result.getData()));
                }else{
                    log.info("识别失败：{}", result.getMessage());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取Vosk模型（中文）
     * 模型下载网盘：通过网盘分享的文件：https://pan.baidu.com/s/1kiMF5MF641R7LTn1GpB2lQ?pwd=1234 提取码: 1234
     * 更多模型下载地址：https://alphacephei.com/vosk/models
     * @return
     */
    public SpeechRecognizer geVoskRecognizer() {
        AsrModelConfig config = new AsrModelConfig();
        config.setModelEnum(AsrModelEnum.VOSK);
        /**
         * 每个模型只支持一种语言，请下载对应语音的模型，模型下载地址：https://alphacephei.com/vosk/models
         * 将模型解压后，将模型目录位置填写到此处
         */
        config.setModelPath("/Users/wenjie/Documents/develop/model/speech/vosk-model-cn-0.22");
        /**
         * macos m系列芯片需要手动下载依赖库，并指定位置，其他平台不需要
         * 下载地址：https://pan.baidu.com/s/1LZ_EX1XdTTp_f5ruud82MA?pwd=1234 提取码: 1234
         */
        config.setLibPath(Paths.get("/Users/wenjie/Downloads/vosk-arrch64-dylib-main/libvosk.dylib"));
        return SpeechRecognizerFactory.getInstance().getModel(config);
    }

    /**
     * 获取Vosk模型（英文）
     * 模型下载网盘：通过网盘分享的文件：https://pan.baidu.com/s/1kiMF5MF641R7LTn1GpB2lQ?pwd=1234 提取码: 1234
     * 更多模型下载地址：https://alphacephei.com/vosk/models
     * @return
     */
    public SpeechRecognizer geEnVoskRecognizer() {
        AsrModelConfig config = new AsrModelConfig();
        config.setModelEnum(AsrModelEnum.VOSK);
        /**
         * 每个模型只支持一种语言，请下载对应语音的模型，模型下载地址：https://alphacephei.com/vosk/models
         * 将模型解压后，将模型目录位置填写到此处
         */
        config.setModelPath("/Users/xxx/Documents/develop/model/speech/vosk-model-small-en-us-0.15");
//        config.setLibPath(Paths.get("/Users/xxx/Downloads/vosk-arrch64-dylib-main/libvosk.dylib"));
        return SpeechRecognizerFactory.getInstance().getModel(config);
    }


    /**
     * Vosk 语音识别
     * 支持 20 多种语言和方言——英语、印度英语、德语、法语、西班牙语、葡萄牙语、中文、俄语、土耳其语、越南语、意大利语、荷兰语、加泰罗尼亚语、阿拉伯语、希腊语、波斯语、菲律宾语、乌克兰语、哈萨克语、瑞典语、日语、世界语、印地语、捷克语、波兰语等
     * 注意事项:
     * 1、每个模型只支持一种语言，请下载对应语言的模型
     * 2、如果音频中存在多种语言，不推荐使用vosk，可以使用Whisper
     * 3、模型越大越准确
     * 4、暂不支持GPU使用，如需GPU使用需要自行编译：https://alphacephei.com/vosk/install
     */
    @Test
    public void testVosk() {
        try {
            //解决中文乱码问题
            System.setProperty("jna.encoding","utf-8");
            SpeechRecognizer recognizer = geVoskRecognizer();
            //建议上传 WAV 格式音频。其他格式将自动转换为 WAV，可能影响处理速度
            R<AsrResult> result = recognizer.recognize("src/main/resources/lff_zh.mp3");
            if (result.isSuccess()){
                log.info("识别成功:{}", JsonUtils.toJson(result.getData()));
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vosk 语音识别(使用Grammar语法规则)
     * 支持 20 多种语言和方言——英语、印度英语、德语、法语、西班牙语、葡萄牙语、中文、俄语、土耳其语、越南语、意大利语、荷兰语、加泰罗尼亚语、阿拉伯语、希腊语、波斯语、菲律宾语、乌克兰语、哈萨克语、瑞典语、日语、世界语、印地语、捷克语、波兰语等
     * 注意事项:
     * 1、每个模型只支持一种语言，请下载对应语言的模型
     * 2、如果音频中存在多种语言，不推荐使用vosk，可以使用Whisper
     * 3、模型越大越准确
     * 4、暂不支持GPU使用，如需GPU使用需要自行编译：https://alphacephei.com/vosk/install
     */
    @Test
    public void testVoskWithGrammar() {
        try {
            //获取英文模型
            SpeechRecognizer recognizer = geEnVoskRecognizer();
            VoskParams voskParams = new VoskParams();
            //英文
            voskParams.setLanguage(Language.EN);
            voskParams.setGrammar("[\"one two three four five six seven eight nine zero oh\"]");
            //建议上传 WAV 格式音频。其他格式将自动转换为 WAV，可能影响处理速度
            R<AsrResult> result = recognizer.recognize("src/main/resources/test_en.wav",voskParams);
            if (result.isSuccess()){
                log.info("识别成功:{}", JsonUtils.toJson(result.getData()));
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vosk 语音识别(使用Vosk内部识别器)
     * 支持 20 多种语言和方言——英语、印度英语、德语、法语、西班牙语、葡萄牙语、中文、俄语、土耳其语、越南语、意大利语、荷兰语、加泰罗尼亚语、阿拉伯语、希腊语、波斯语、菲律宾语、乌克兰语、哈萨克语、瑞典语、日语、世界语、印地语、捷克语、波兰语等
     * 注意事项:
     * 1、每个模型只支持一种语言，请下载对应语言的模型
     * 2、如果音频中存在多种语言，不推荐使用vosk，可以使用Whisper
     * 3、模型越大越准确
     * 4、暂不支持GPU使用，如需GPU使用需要自行编译：https://alphacephei.com/vosk/install
     */
    @Test
    public void testVoskAdvanced() {
        try {
            //解决中文乱码问题
            System.setProperty("jna.encoding","utf-8");
            VoskRecognizer recognizer = (VoskRecognizer)geVoskRecognizer();
            //使用vosk内部接口，需要指定识别音频的采样率
            Recognizer voskRecognizer = recognizer.createAdvancedRecognizer(16000);
            voskRecognizer.setWords(true);
            voskRecognizer.setPartialWords(true);
            // 使用vosk内部接口，只支持wav格式
            String audioPath = "src/main/resources/lff_zh.wav";
            InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(audioPath)));
            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (voskRecognizer.acceptWaveForm(b, nbytes)) {
                    log.info(voskRecognizer.getResult());
                } else {
                    log.info(voskRecognizer.getPartialResult());
                }
            }
            log.info(voskRecognizer.getFinalResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实时语音识别（VOSK）
     */
    @Test
    public void testVoskRealTime() {
        try {
            //解决中文乱码问题
            System.setProperty("jna.encoding","utf-8");
            VoskRecognizer recognizer = (VoskRecognizer)geVoskRecognizer();
            //使用vosk内部接口，需要指定识别音频的采样率
            Recognizer voskRecognizer = recognizer.createAdvancedRecognizer(16000);
            voskRecognizer.setWords(true);
            voskRecognizer.setPartialWords(true);
            // 设置音频格式: 16kHz, 16bit, 单声道, PCM
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            // 获取可用的 TargetDataLine
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("麦克风不支持该格式");
                System.exit(0);
            }
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = microphone.read(b,0,b.length)) >= 0) {
                if (voskRecognizer.acceptWaveForm(b, nbytes)) {
                    log.info(voskRecognizer.getResult());
                } else {
                    log.info(voskRecognizer.getPartialResult());
                }
            }
            log.info(voskRecognizer.getFinalResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
