package cn.smartjavaai.ocr.entity;

import ai.djl.modality.cv.Image;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OCR信息
 * @author dwj
 * @date 2025/5/20
 */
@Data
public class OcrInfo {

    private List<List<OcrItem>> lineList;

    private List<OcrItem> ocrItemList;

    private String fullText;

    private transient Image drawnImage;


    public OcrInfo(List<List<OcrItem>> lineList, String fullText) {
        this.lineList = lineList;
        this.fullText = fullText;
    }
    public OcrInfo() {
    }

    public List<OcrItem> flattenLines() {
        return lineList.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
