package cn.smartjavaai.ocr.entity;

import lombok.Data;

import java.util.List;

/**
 * @author dwj
 */
@Data
public class TableStructureResult {

    private List<OcrItem> ocrItemList;

    private List<String> tableTagList;

    private String html;


    public TableStructureResult(List<OcrItem> ocrItemList, List<String> tableTagList) {
        this.ocrItemList = ocrItemList;
        this.tableTagList = tableTagList;
    }

    public TableStructureResult() {
    }

    public TableStructureResult(List<OcrItem> ocrItemList, List<String> tableTagList, String html) {
        this.ocrItemList = ocrItemList;
        this.tableTagList = tableTagList;
        this.html = html;
    }
}
