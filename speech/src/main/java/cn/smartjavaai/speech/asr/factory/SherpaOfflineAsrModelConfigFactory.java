package cn.smartjavaai.speech.asr.factory;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.speech.asr.config.AsrModelConfig;
import cn.smartjavaai.speech.asr.exception.AsrException;
import cn.smartjavaai.speech.tts.config.TtsModelConfig;
import cn.smartjavaai.speech.tts.exception.TtsException;
import com.k2fsa.sherpa.onnx.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author dwj
 */
public class SherpaOfflineAsrModelConfigFactory {

    public static OfflineDolphinModelConfig createDolphinConfig(AsrModelConfig config) {
        if(StringUtils.isBlank(config.getModelName())){
            throw new AsrException("modelName is null");
        }
        String model = config.getModelPath() + File.separator + config.getModelName();
        OfflineDolphinModelConfig dolphin = OfflineDolphinModelConfig.builder().setModel(model).build();
        return dolphin;
    }

    public static OfflineFireRedAsrModelConfig createFireRedConfig(AsrModelConfig config) {
        List<File> decoderFiles = FileUtils.searchFiles(config.getModelPath(), "decoder", ".onnx",false);
        List<File> encoderFiles = FileUtils.searchFiles(config.getModelPath(), "encoder", ".onnx",false);
        if (CollectionUtils.isEmpty(decoderFiles)){
            throw new AsrException("decoder onnx not found");
        }
        if (CollectionUtils.isEmpty(encoderFiles)){
            throw new AsrException("encoder onnx not found");
        }
        String encoder = encoderFiles.get(0).getAbsolutePath();
        String decoder = decoderFiles.get(0).getAbsolutePath();
        OfflineFireRedAsrModelConfig fireRedAsr =
                OfflineFireRedAsrModelConfig.builder().setEncoder(encoder).setDecoder(decoder).build();
        return fireRedAsr;
    }

    public static OfflineMoonshineModelConfig createMoonshineConfig(AsrModelConfig config) {
        List<File> preprocessorFiles = FileUtils.searchFiles(config.getModelPath(), "preprocess", ".onnx",false);
        List<File> uncachedDecoderFiles = FileUtils.searchFiles(config.getModelPath(), "uncached_decode", ".onnx",false);
        List<File> cachedDecoderFiles = FileUtils.searchFiles(config.getModelPath(), "cached_decode", ".onnx",false);
        List<File> encoderFiles = FileUtils.searchFiles(config.getModelPath(), "encode", ".onnx",false);
        if (CollectionUtils.isEmpty(uncachedDecoderFiles)){
            throw new AsrException("uncached_decode onnx not found");
        }
        if (CollectionUtils.isEmpty(cachedDecoderFiles)){
            throw new AsrException("cached_decode onnx not found");
        }
        if (CollectionUtils.isEmpty(encoderFiles)){
            throw new AsrException("encoder onnx not found");
        }
        if (CollectionUtils.isEmpty(preprocessorFiles)){
            throw new AsrException("preprocess onnx not found");
        }
        String encoder = encoderFiles.get(0).getAbsolutePath();
        String preprocessor = preprocessorFiles.get(0).getAbsolutePath();
        String cachedDecoder = cachedDecoderFiles.get(0).getAbsolutePath();
        String uncachedDecoder = uncachedDecoderFiles.get(0).getAbsolutePath();
        OfflineMoonshineModelConfig moonshine =
                OfflineMoonshineModelConfig.builder()
                        .setPreprocessor(preprocessor)
                        .setEncoder(encoder)
                        .setUncachedDecoder(uncachedDecoder)
                        .setCachedDecoder(cachedDecoder)
                        .build();
        return moonshine;
    }

    public static OfflineNemoEncDecCtcModelConfig createNemoConfig(AsrModelConfig config) {
        if(StringUtils.isBlank(config.getModelName())){
            throw new AsrException("modelName is null");
        }
        String model = config.getModelPath() + File.separator + config.getModelName();
        OfflineNemoEncDecCtcModelConfig modelConfig = OfflineNemoEncDecCtcModelConfig.builder().setModel(model).build();
        return modelConfig;
    }

    public static OfflineSenseVoiceModelConfig createSenseVoiceConfig(AsrModelConfig config) {
        if(StringUtils.isBlank(config.getModelName())){
            throw new AsrException("modelName is null");
        }
        String model = config.getModelPath() + File.separator + config.getModelName();
        OfflineSenseVoiceModelConfig senseVoice =
                OfflineSenseVoiceModelConfig.builder().setModel(model).build();
        return senseVoice;
    }

    public static OfflineTransducerModelConfig createTransducerConfig(AsrModelConfig config) {
        List<File> decoderFiles = FileUtils.searchFiles(config.getModelPath(), "decoder", ".onnx",false);
        List<File> encoderFiles = FileUtils.searchFiles(config.getModelPath(), "encoder", "int8.onnx",false);
        List<File> joinerFiles = FileUtils.searchFiles(config.getModelPath(), "joiner", ".onnx",false);
        if (CollectionUtils.isEmpty(decoderFiles)){
            throw new AsrException("decoder onnx not found");
        }
        if (CollectionUtils.isEmpty(encoderFiles)){
            throw new AsrException("encoder onnx not found");
        }
        if (CollectionUtils.isEmpty(joinerFiles)){
            throw new AsrException("joiner onnx not found");
        }
        String encoder = encoderFiles.get(0).getAbsolutePath();
        String decoder = decoderFiles.get(0).getAbsolutePath();
        String joiner = joinerFiles.get(0).getAbsolutePath();
        OfflineTransducerModelConfig transducer =
                OfflineTransducerModelConfig.builder()
                        .setEncoder(encoder)
                        .setDecoder(decoder)
                        .setJoiner(joiner)
                        .build();
        return transducer;
    }

    public static OfflineParaformerModelConfig createParaformerConfig(AsrModelConfig config) {
        String model = config.getModelPath() + File.separator + config.getModelName();
        OfflineParaformerModelConfig modelConfig = OfflineParaformerModelConfig.builder().setModel(model).build();
        return modelConfig;
    }

    public static OfflineWenetCtcModelConfig createWenetCtcConfig(AsrModelConfig config) {
        if(StringUtils.isBlank(config.getModelName())){
            throw new AsrException("modelName is null");
        }
        String model = config.getModelPath() + File.separator + config.getModelName();
        OfflineWenetCtcModelConfig wenetCtc =
                OfflineWenetCtcModelConfig.builder().setModel(model).build();
        return wenetCtc;
    }

    public static OfflineCanaryModelConfig createCanaryConfig(AsrModelConfig config) {
        List<File> decoderFiles = FileUtils.searchFiles(config.getModelPath(), "decoder", ".onnx",false);
        List<File> encoderFiles = FileUtils.searchFiles(config.getModelPath(), "encoder", ".onnx",false);
        if (CollectionUtils.isEmpty(decoderFiles)){
            throw new AsrException("decoder onnx not found");
        }
        if (CollectionUtils.isEmpty(encoderFiles)){
            throw new AsrException("encoder onnx not found");
        }
        String encoder = encoderFiles.get(0).getAbsolutePath();
        String decoder = decoderFiles.get(0).getAbsolutePath();
        OfflineCanaryModelConfig canary =
                OfflineCanaryModelConfig.builder()
                        .setEncoder(encoder)
                        .setDecoder(decoder)
                        .setSrcLang("en")
                        .setTgtLang("en")
                        .setUsePnc(true)
                        .build();
        return canary;
    }

    public static OfflineWhisperModelConfig createWhisperConfig(AsrModelConfig config) {
        //是否使用量化模型
        boolean useInt8 = config.getCustomParam("useInt8", Boolean.class, false);
        String extension = useInt8 ? "int8.onnx" : "onnx";
        List<File> decoderFiles = FileUtils.searchFiles(config.getModelPath(), "decoder", extension,false);
        List<File> encoderFiles = FileUtils.searchFiles(config.getModelPath(), "encoder", extension,false);
        if (CollectionUtils.isEmpty(decoderFiles)){
            throw new AsrException("decoder onnx not found");
        }
        if (CollectionUtils.isEmpty(encoderFiles)){
            throw new AsrException("encoder onnx not found");
        }
        String encoder = encoderFiles.get(0).getAbsolutePath();
        String decoder = decoderFiles.get(0).getAbsolutePath();
        OfflineWhisperModelConfig fireRedAsr =
                OfflineWhisperModelConfig.builder().setEncoder(encoder).setDecoder(decoder).build();
        return fireRedAsr;
    }

    public static OfflineZipformerCtcModelConfig createZipformerCtcConfig(AsrModelConfig config) {
        if(StringUtils.isBlank(config.getModelName())){
            throw new AsrException("modelName is null");
        }
        String model = config.getModelPath() + File.separator + config.getModelName();
        OfflineZipformerCtcModelConfig zipformerCtc =
                OfflineZipformerCtcModelConfig.builder().setModel(model).build();
        return zipformerCtc;
    }

    /**
     * 创建模型配置
     * @param config
     * @return
     */
    public static OfflineRecognizerConfig createConfig(AsrModelConfig config) {
        OfflineModelConfig modelConfig = null;
        int numThreads = config.getCustomParam("numThreads", Integer.class, 1);
        boolean debug = config.getCustomParam("debug", Boolean.class, true);
        List<File> tokensFiles = FileUtils.findFilesWithSuffix(new File(config.getModelPath()), "tokens.txt", false);
        String tokens = CollectionUtils.isEmpty(tokensFiles) ? "" : tokensFiles.get(0).getAbsolutePath();
        String provider = config.getCustomParam("provider", String.class, "cpu");
        switch (config.getModelEnum()){
            case SHERPA_PARAFORMER:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setParaformer(createParaformerConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_TRANSDUCER:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setTransducer(createTransducerConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_WHISPER:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setWhisper(createWhisperConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_FIREREDASR:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setFireRedAsr(createFireRedConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_MOONSHINE:
                numThreads = config.getCustomParam("numThreads", Integer.class, 2);
                modelConfig =
                        OfflineModelConfig.builder()
                                .setMoonshine(createMoonshineConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_NEMO:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setNemo(createNemoConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setProvider(provider)
                                .setDebug(debug)
                                .build();
                break;
            case SHERPA_SENSEVOICE:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setSenseVoice(createSenseVoiceConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_DOLPHIN:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setDolphin(createDolphinConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_ZIPFORMERCTC:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setZipformerCtc(createZipformerCtcConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_WENETCTC:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setWenetCtc(createWenetCtcConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_CANARY:
                modelConfig =
                        OfflineModelConfig.builder()
                                .setCanary(createCanaryConfig(config))
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_TELESPEECH:
                if(StringUtils.isBlank(config.getModelName())){
                    throw new AsrException("modelName is null");
                }
                String model = config.getModelPath() + File.separator + config.getModelName();
                modelConfig =
                        OfflineModelConfig.builder()
                                .setTeleSpeech(model)
                                .setTokens(tokens)
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setModelType("telespeech_ctc")
                                .setProvider(provider)
                                .build();
                break;
        }
        OfflineRecognizerConfig offlineRecognizerConfig = OfflineRecognizerConfig.builder()
                        .setOfflineModelConfig(modelConfig)
                        .setDecodingMethod("greedy_search")
                        .build();
        return offlineRecognizerConfig;
    }

}
