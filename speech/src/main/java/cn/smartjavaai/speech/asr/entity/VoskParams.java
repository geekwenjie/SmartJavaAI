package cn.smartjavaai.speech.asr.entity;

import cn.smartjavaai.common.entity.Language;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;
import lombok.Data;

/**
 * @author dwj
 */
@Data
public class VoskParams extends RecParams{

    /**
     * 限定词汇表
     */
    private String grammar;


}
