package cn.smartjavaai.speech.tts.entity;

import lombok.Data;

/**
 * tts参数
 * @author dwj
 */
@Data
public abstract class TtsParams {

    /**
     * 发音人
     */
    private int speakerId;

    /**
     * 语速
     */
    private float speed;


}
