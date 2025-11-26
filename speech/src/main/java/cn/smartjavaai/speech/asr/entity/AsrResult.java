package cn.smartjavaai.speech.asr.entity;

import lombok.Data;

import java.util.List;

/**
 * 语音识别结果
 * @author dwj
 */
@Data
public class AsrResult {

    private String text;
    private List<AsrSegment> segments;


    public AsrResult() {
    }

    public AsrResult(String text) {
        this.text = text;
    }

    public AsrResult(String text, List<AsrSegment> segments) {
        this.text = text;
        this.segments = segments;
    }

}
