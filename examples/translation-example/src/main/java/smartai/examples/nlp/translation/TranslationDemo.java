package smartai.examples.nlp.translation;

import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.entity.TranslateParam;
import cn.smartjavaai.translation.enums.LanguageCode;
import cn.smartjavaai.translation.enums.TranslationModeEnum;
import cn.smartjavaai.translation.factory.TranslationModelFactory;
import cn.smartjavaai.translation.model.TranslationModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * 机器翻译Demo
 * 模型下载地址：https://pan.baidu.com/s/1wf7btnb4cyBFv7DB7baHnw?pwd=1234 提取码: 1234
 * 开发文档：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class TranslationDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取模型(NLLB)
     * @return
     */
    public TranslationModel getNllbModel() {
        TranslationModelConfig config = new TranslationModelConfig();
        //指定翻译模型：NLLB,切换模型需同时修改modelEnum及modelPath
        config.setModelEnum(TranslationModeEnum.NLLB_MODEL);
        //指定模型路径，需将模型路径修改为本地的模型路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/translate/nllb/traced_translation_cpu.pt");
        config.setDevice(DeviceEnum.CPU);
        return TranslationModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取中文模型(OPUS 中文->英文)
     * @return
     */
    public TranslationModel getOPUSModelZH_EN() {
        TranslationModelConfig config = new TranslationModelConfig();
        //指定翻译模型,切换模型需同时修改modelEnum及modelPath
        config.setModelEnum(TranslationModeEnum.OPUS_MT_ZH_EN);
        //指定模型路径，需将模型路径修改为本地的模型路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/trans/opus-mt-zh-en/traced_translation.pt");
        config.setDevice(DeviceEnum.CPU);
        return TranslationModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取中文模型(OPUS 英文->中文)
     * @return
     */
    public TranslationModel getOPUSModelEN_ZH() {
        TranslationModelConfig config = new TranslationModelConfig();
        //指定翻译模型,切换模型需同时修改modelEnum及modelPath
        config.setModelEnum(TranslationModeEnum.OPUS_MT_EN_ZH);
        //指定模型路径，需将模型路径修改为本地的模型路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/trans/opus-mt-en-zh/traced_translation.pt");
        config.setDevice(DeviceEnum.CPU);
        return TranslationModelFactory.getInstance().getModel(config);
    }


    /**
     * 翻译(nllb模型)
     */
    @Test
    public void nllbTranslate() {
        try {
            TranslationModel translationModel = getNllbModel();
            //翻译参数
            TranslateParam translateParam = new TranslateParam();
            //输入文字
            translateParam.setInput("你好，欢迎使用SmartJavaAI！");
            //源语言：中文
            translateParam.setSourceLanguage(LanguageCode.ZHO_HANS);
            //目标语言：英文
            translateParam.setTargetLanguage(LanguageCode.ENG_LATN);
            R<String> result = translationModel.translate(translateParam);
            if(result.isSuccess()){
                log.info("翻译结果：{}", result.getData());
            }else{
                log.error("翻译失败：{}", result.getMessage());
            }
            //目标语言：韩语
            translateParam.setTargetLanguage(LanguageCode.KOR_HANG);
            R<String> result2 = translationModel.translate(translateParam);
            if(result2.isSuccess()){
                log.info("翻译结果：{}", result2.getData());
            }else{
                log.error("翻译失败：{}", result2.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 翻译(opus 中文->英文)
     */
    @Test
    public void opusTranslate1() {
        try {
            TranslationModel translationModel = getOPUSModelZH_EN();
            //翻译参数
            TranslateParam translateParam = new TranslateParam();
            //输入文字
            translateParam.setInput("SmartJavaAI是专为JAVA 开发者打造的一个功能丰富、开箱即用的 JAVA AI算法工具包");
            R<String> result = translationModel.translate(translateParam);
            if(result.isSuccess()){
                log.info("翻译结果：{}", result.getData());
            }else{
                log.error("翻译失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 翻译(opus 英文->中文)
     */
    @Test
    public void opusTranslate2() {
        try {
            TranslationModel translationModel = getOPUSModelEN_ZH();
            //翻译参数
            TranslateParam translateParam = new TranslateParam();
            //输入文字
            translateParam.setInput("You don't have to be machine learning expert to get started");
            R<String> result = translationModel.translate(translateParam);
            if(result.isSuccess()){
                log.info("翻译结果：{}", result.getData());
            }else{
                log.error("翻译失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
