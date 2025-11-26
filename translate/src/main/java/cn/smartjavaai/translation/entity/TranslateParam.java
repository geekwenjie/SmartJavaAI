package cn.smartjavaai.translation.entity;

import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.translation.enums.LanguageCode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 翻译参数
 * @author dwj
 * @date 2025/6/16
 */
@Data
public class TranslateParam {

    /**
     * 输入文本
     */
    private String input;

    /**
     * 源语言
     */
    private LanguageCode sourceLanguage;

    /**
     * 目标语言
     */
    private LanguageCode targetLanguage;

    /**
     * 参数校验方法
     * @return 如果参数有误返回 R.fail，否则返回 R.ok(null)
     */
    public R<String> validate() {
        if (StringUtils.isBlank(input)) {
            return R.fail(R.Status.PARAM_ERROR.getCode(), "输入文本不能为空");
        }
        if (sourceLanguage == null) {
            return R.fail(R.Status.PARAM_ERROR.getCode(), "源语言不能为空");
        }
        if (targetLanguage == null) {
            return R.fail(R.Status.PARAM_ERROR.getCode(), "目标语言不能为空");
        }
        return R.ok(null);
    }

    public TranslateParam(String input, LanguageCode sourceLanguage, LanguageCode targetLanguage) {
        this.input = input;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public TranslateParam(String input) {
        this.input = input;
    }

    public TranslateParam() {
    }
}
