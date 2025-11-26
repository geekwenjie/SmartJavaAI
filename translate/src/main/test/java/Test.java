import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.entity.TranslateParam;
import cn.smartjavaai.translation.enums.LanguageCode;
import cn.smartjavaai.translation.enums.TranslationModeEnum;
import cn.smartjavaai.translation.factory.TranslationModelFactory;
import cn.smartjavaai.translation.model.TranslationModel;

/**
 * @author dwj
 * @date 2025/6/17
 */
public class Test {

    public static void main(String[] args) {
        TranslationModelConfig config = new TranslationModelConfig();
        config.setModelEnum(TranslationModeEnum.NLLB_MODEL);
        config.setModelPath("/Users/wenjie/Documents/develop/model/trans/traced_translation_cpu.pt");
        // 输入文字
        String input2 = "我爱你";
        String input = "你好，欢迎使用SmartJavaAI！";
        TranslationModel detModel = TranslationModelFactory.getInstance().getModel(config);
        TranslateParam translateParam = new TranslateParam();
        translateParam.setInput(input2);
//        translateParam.setSourceLanguage(LanguageCode.ZHO_HANS);
//        translateParam.setTargetLanguage(LanguageCode.ENG_LATN);
        translateParam.setSourceLanguage(LanguageCode.ZHO_HANS);
        translateParam.setTargetLanguage(LanguageCode.KOR_HANG);
        R<String> result = detModel.translate(translateParam);
        System.out.println(JsonUtils.toJson(result));

    }

}
