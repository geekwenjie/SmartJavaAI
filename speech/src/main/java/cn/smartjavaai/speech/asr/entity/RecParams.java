package cn.smartjavaai.speech.asr.entity;

import cn.smartjavaai.common.entity.Language;
import lombok.Data;

/**
 * 语音识别参数
 * @author dwj
 */
@Data
public abstract class RecParams {

    /**
     * 语言
     */
    private Language language = Language.ZH;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }


}
