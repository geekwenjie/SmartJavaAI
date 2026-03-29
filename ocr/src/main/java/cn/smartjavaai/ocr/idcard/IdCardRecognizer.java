package cn.smartjavaai.ocr.idcard;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.entity.IdCardBackInfo;
import cn.smartjavaai.ocr.entity.IdCardFrontInfo;
import cn.smartjavaai.ocr.entity.IdCardInfo;
import cn.smartjavaai.ocr.entity.OcrInfo;

/**
 * 身份证 OCR 识别服务接口
 *
 * 支持：
 * 1. 直接传入图片，由服务内部完成预处理 + OCR + 解析；
 * 2. 已有 OCR 结果的情况下，仅做单面结构化解析。
 */
public interface IdCardRecognizer {

    /**
     * 识别身份证正面（从图片开始）
     */
    IdCardFrontInfo recognizeFront(Image image);

    /**
     * 识别身份证反面（从图片开始）
     */
    IdCardBackInfo recognizeBack(Image image);

    /**
     * 识别身份证正面（仅结构化已有 OCR 结果）
     */
    IdCardFrontInfo recognizeFront(OcrInfo ocrInfo);

    /**
     * 识别身份证反面（仅结构化已有 OCR 结果）
     */
    IdCardBackInfo recognizeBack(OcrInfo ocrInfo);

    /**
     * 同时识别身份证正反面（分别传入两面图片）
     */
    IdCardInfo recognizeBoth(Image frontImage, Image backImage);


}
