package cn.smartjavaai.ocr.entity;

import cn.smartjavaai.ocr.enums.AngleEnum;
import lombok.Data;

/**
 * @author dwj
 * @date 2025/5/20
 */
@Data
public class OcrItem {

    /**
     * 识别框
     */
    private OcrBox ocrBox;

    /**
     * 文本
     */
    private String text;

    /**
     * 方向
     */
    private AngleEnum angle;

    /**
     * 检测得分
     */
    private float score;


    public OcrItem(OcrBox ocrBox, String text) {
        this.ocrBox = ocrBox;
        this.text = text;
    }

    public OcrItem() {
    }

    public OcrItem(OcrBox ocrBox, String text, AngleEnum angle) {
        this.ocrBox = ocrBox;
        this.text = text;
        this.angle = angle;
    }

    public OcrItem(OcrBox ocrBox, AngleEnum angle, float score) {
        this.ocrBox = ocrBox;
        this.angle = angle;
        this.score = score;
    }

}
