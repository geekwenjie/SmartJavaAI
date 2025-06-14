package smartai.examples.nlb;

import ai.djl.Device;
import ai.djl.ModelException;
import ai.djl.translate.TranslateException;
import smartai.examples.nlb.generate.SearchConfig;
import smartai.examples.nlb.model.NllbModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 文本翻译，支持202种语言互译
 *
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
public final class TextTranslationCPU {

    private static final Logger logger = LoggerFactory.getLogger(TextTranslationCPU.class);

    private TextTranslationCPU() {
    }

    public static void main(String[] args) throws ModelException, IOException,
            TranslateException {

        SearchConfig config = new SearchConfig();
        // 设置输出文字的最大长度
        config.setMaxSeqLength(128);
        // 设置源语言：中文 "zho_Hans": 256200
        config.setSrcLangId(256200);
        // 设置目标语言：英文 "eng_Latn": 256047
        config.setForcedBosTokenId(256047);
        config.setForcedBosTokenId(256201);

        // 输入文字
        String input = "智利北部的丘基卡马塔矿是世界上最大的露天矿之一，长约4公里，宽3公里，深1公里。";

        String modelPath = "E:\\ai\\models\\nlp\\";
        String cpuModelName = "traced_translation_cpu.pt";
        String gpuModelName = "traced_translation_gpu.pt";
        try (NllbModel nllbModel = new NllbModel(config, modelPath, cpuModelName, Device.cpu())) {

            System.setProperty("ai.djl.pytorch.graph_optimizer", "false");

            // 运行模型，获取翻译结果
            String result = nllbModel.translate(input);

            logger.info("result========={}", result);
        } finally {
            System.clearProperty("ai.djl.pytorch.graph_optimizer");
        }
    }
}