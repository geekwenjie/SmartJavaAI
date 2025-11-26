package cn.smartjavaai.speech.tts.factory;

import cn.hutool.core.io.FileUtil;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.speech.tts.exception.TtsException;
import com.k2fsa.sherpa.onnx.*;
import cn.smartjavaai.speech.tts.config.TtsModelConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author dwj
 * @date 2025/10/14
 */
public class SherpaOfflineTtsModelConfigFactory {

    /**
     * 创建Vits模型配置
     * @param config
     * @return
     */
    public static OfflineTtsVitsModelConfig createVitsConfig(TtsModelConfig config) {
        List<File> tokensFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "tokens.txt", false);
        List<File> lexiconFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "lexicon.txt", false);
        List<File> dictFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "dict", false);
        List<File> dataDirFiles = FileUtils.findFilesWithSuffix(new File(config.getModelPath()), "-data", false);
        String model = config.getModelPath() + File.separator + config.getModelName();;
        String tokens = CollectionUtils.isEmpty(tokensFiles) ? "" : tokensFiles.get(0).getAbsolutePath();
        String lexicon = CollectionUtils.isEmpty(lexiconFiles) ? "" : lexiconFiles.get(0).getAbsolutePath();
        String dictPath = CollectionUtils.isEmpty(dictFiles) ? "" : dictFiles.get(0).getAbsolutePath();
        String dataDir = CollectionUtils.isEmpty(dataDirFiles) ? "" : dataDirFiles.get(0).getAbsolutePath();
        float lengthScale = config.getCustomParam("lengthScale", Float.class, 1f);
        float noiseScale = config.getCustomParam("noiseScale", Float.class, 0.667F);
        float noiseScaleW = config.getCustomParam("noiseScaleW", Float.class, 0.8f);
        OfflineTtsVitsModelConfig vitsModelConfig =
                OfflineTtsVitsModelConfig.builder()
                        .setModel(model)
                        .setTokens(tokens)
                        .setLexicon(lexicon)
                        .setDictDir(dictPath)
                        .setDataDir(dataDir)
                        .setLengthScale(lengthScale)
                        .setNoiseScale(noiseScale)
                        .setNoiseScaleW(noiseScaleW)
                        .build();
        return vitsModelConfig;
    }

    public static OfflineTtsMatchaModelConfig createMatchaConfig(TtsModelConfig config) {
        String vocoder = config.getCustomParam("vocoder", String.class);
        if(StringUtils.isBlank(vocoder)){
            throw new TtsException("vocoder is null");
        }
        List<File> tokensFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "tokens.txt", false);
        List<File> lexiconFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "lexicon.txt", false);
        List<File> dictFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "dict", false);
        List<File> dataDirFiles = FileUtils.findFilesWithSuffix(new File(config.getModelPath()), "-data", false);
        String model = config.getModelPath() + File.separator + config.getModelName();;
        String tokens = CollectionUtils.isEmpty(tokensFiles) ? "" : tokensFiles.get(0).getAbsolutePath();
        String lexicon = CollectionUtils.isEmpty(lexiconFiles) ? "" : lexiconFiles.get(0).getAbsolutePath();
        String dictPath = CollectionUtils.isEmpty(dictFiles) ? "" : dictFiles.get(0).getAbsolutePath();
        String dataDir = CollectionUtils.isEmpty(dataDirFiles) ? "" : dataDirFiles.get(0).getAbsolutePath();
        float lengthScale = config.getCustomParam("lengthScale", Float.class, 1f);
        float noiseScale = config.getCustomParam("noiseScale", Float.class, 1f);
        OfflineTtsMatchaModelConfig vitsModelConfig =
                OfflineTtsMatchaModelConfig.builder()
                        .setAcousticModel(model)
                        .setTokens(tokens)
                        .setLexicon(lexicon)
                        .setDictDir(dictPath)
                        .setDataDir(dataDir)
                        .setVocoder(vocoder)
                        .setLengthScale(lengthScale)
                        .setNoiseScale(noiseScale)
                        .build();
        return vitsModelConfig;
    }

    public static OfflineTtsKittenModelConfig createKittenConfig(TtsModelConfig config) {
        List<File> tokensFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "tokens.txt", false);
        List<File> voicesFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "voices.bin", false);
        List<File> dataDirFiles = FileUtils.findFilesWithSuffix(new File(config.getModelPath()), "-data", false);
        String model = config.getModelPath() + File.separator + config.getModelName();;
        String tokens = CollectionUtils.isEmpty(tokensFiles) ? "" : tokensFiles.get(0).getAbsolutePath();
        String voices = CollectionUtils.isEmpty(voicesFiles) ? "" : voicesFiles.get(0).getAbsolutePath();
        String dataDir = CollectionUtils.isEmpty(dataDirFiles) ? "" : dataDirFiles.get(0).getAbsolutePath();
        float lengthScale = config.getCustomParam("lengthScale", Float.class, 1f);
        OfflineTtsKittenModelConfig vitsModelConfig =
                OfflineTtsKittenModelConfig.builder()
                        .setModel(model)
                        .setTokens(tokens)
                        .setVoices(voices)
                        .setDataDir(dataDir)
                        .setLengthScale(lengthScale)
                        .build();
        return vitsModelConfig;
    }

    public static OfflineTtsKokoroModelConfig createKokoroConfig(TtsModelConfig config) {
        List<File> tokensFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "tokens.txt", false);
        List<File> lexiconFiles = FileUtils.searchFiles(config.getModelPath(), "lexicon", ".txt",false);
        List<File> dictFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "dict", false);
        List<File> dataDirFiles = FileUtils.findFilesWithSuffix(new File(config.getModelPath()), "-data", false);
        List<File> voicesFiles = FileUtils.findFilesByName(new File(config.getModelPath()), "voices.bin", false);
        String model = config.getModelPath() + File.separator + config.getModelName();;
        String tokens = CollectionUtils.isEmpty(tokensFiles) ? "" : tokensFiles.get(0).getAbsolutePath();
        String lexicon = CollectionUtils.isEmpty(lexiconFiles) ? "" : FileUtils.joinAbsolutePaths(lexiconFiles);
        String dictPath = CollectionUtils.isEmpty(dictFiles) ? "" : dictFiles.get(0).getAbsolutePath();
        String dataDir = CollectionUtils.isEmpty(dataDirFiles) ? "" : dataDirFiles.get(0).getAbsolutePath();
        String voices = CollectionUtils.isEmpty(voicesFiles) ? "" : voicesFiles.get(0).getAbsolutePath();
        float lengthScale = config.getCustomParam("lengthScale", Float.class, 1f);
        OfflineTtsKokoroModelConfig vitsModelConfig =
                OfflineTtsKokoroModelConfig.builder()
                        .setModel(model)
                        .setTokens(tokens)
                        .setLexicon(lexicon)
                        .setDictDir(dictPath)
                        .setDataDir(dataDir)
                        .setVoices(voices)
                        .setLengthScale(lengthScale)
                        .build();
        return vitsModelConfig;
    }

    /**
     * 创建模型配置
     * @param config
     * @return
     */
    public static OfflineTtsConfig createConfig(TtsModelConfig config) {
        OfflineTtsModelConfig modelConfig = null;
        int numThreads = config.getCustomParam("numThreads", Integer.class, 1);
        boolean debug = config.getCustomParam("debug", Boolean.class, true);
        String provider = config.getCustomParam("provider", String.class, "cpu");
        String ruleFsts = "";
        switch (config.getModelEnum()){
            case SHERPA_VITS:
                modelConfig =
                        OfflineTtsModelConfig.builder()
                                .setVits(createVitsConfig(config))
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_MATCHA:
                modelConfig =
                        OfflineTtsModelConfig.builder()
                                .setMatcha(createMatchaConfig(config))
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_KITTEN:
                modelConfig =
                        OfflineTtsModelConfig.builder()
                                .setKitten(createKittenConfig(config))
                                .setNumThreads(numThreads)
                                .setDebug(debug)
                                .setProvider(provider)
                                .build();
                break;
            case SHERPA_KOKORO:
                numThreads = config.getCustomParam("numThreads", Integer.class, 2);
                modelConfig =
                    OfflineTtsModelConfig.builder()
                            .setKokoro(createKokoroConfig(config))
                            .setNumThreads(numThreads)
                            .setDebug(debug)
                            .setProvider(provider)
                            .build();
            break;
        }
        List<File> ruleFstFiles = FileUtils.findFilesWithSuffix(new File(config.getModelPath()), ".fst", false);
        ruleFsts = CollectionUtils.isEmpty(ruleFstFiles) ? "" : FileUtils.joinAbsolutePaths(ruleFstFiles);
        OfflineTtsConfig offlineTtsConfig =
                OfflineTtsConfig.builder().setModel(modelConfig).setRuleFsts(ruleFsts).build();
        return offlineTtsConfig;
    }

}
