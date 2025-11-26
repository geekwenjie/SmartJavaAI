package cn.smartjavaai.speech.asr.entity;

import cn.smartjavaai.common.entity.Language;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import lombok.Data;

/**
 * @author dwj
 */
@Data
public class WhisperParams extends RecParams{

    private WhisperFullParams params;

    public WhisperParams(WhisperFullParams params) {
        this.params = params;
    }

    public WhisperParams() {
        this.params = new WhisperFullParams(WhisperSamplingStrategy.BEAN_SEARCH);
        this.params.language = Language.ZH.getCode();
    }

    @Override
    public void setLanguage(Language language) {
        super.setLanguage(language);
        if (language != null) {
            this.params.language = language.getCode(); // 关键：设置给底层 whisper 参数
        }
    }
}
