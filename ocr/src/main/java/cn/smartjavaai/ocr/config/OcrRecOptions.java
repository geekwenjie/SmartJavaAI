package cn.smartjavaai.ocr.config;

import lombok.Data;

/**
 * OCR 识别配置
 *
 * @author dwj
 */
@Data
public class OcrRecOptions {

    /**
     * 是否进行文本方向矫正
     */
    private boolean enableDirectionCorrect = false;

    /**
     * 是否进行结果分行
     */
    private boolean enableLineSplit = true;


    public OcrRecOptions(boolean enableDirectionCorrect, boolean enableLineSplit) {
        this.enableDirectionCorrect = enableDirectionCorrect;
        this.enableLineSplit = enableLineSplit;
    }

    public OcrRecOptions() {
    }
}
