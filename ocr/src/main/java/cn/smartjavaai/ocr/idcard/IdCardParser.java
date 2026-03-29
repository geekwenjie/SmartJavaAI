package cn.smartjavaai.ocr.idcard;

import cn.smartjavaai.ocr.entity.OcrInfo;

/**
 * 身份证解析接口，用于从 OCR 结果中解析结构化字段。
 */
public interface IdCardParser<T> {

    /**
     * 从 OCR 结果中解析出指定类型的身份证信息
     */
    T parse(OcrInfo ocrInfo);
}

