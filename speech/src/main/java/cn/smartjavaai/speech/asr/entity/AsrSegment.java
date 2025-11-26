package cn.smartjavaai.speech.asr.entity;

import lombok.Data;

/**
 * @author dwj
 */
@Data
public class AsrSegment {

    private String text;
    private long startTime;
    private long endTime;

    public AsrSegment(String text, long startTime, long endTime) {
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AsrSegment(String text) {
        this.text = text;
    }

    public AsrSegment() {
    }
}
