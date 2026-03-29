package cn.smartjavaai.ocr.idcard;

import ai.djl.modality.cv.Image;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.IdCardBackInfo;
import cn.smartjavaai.ocr.entity.IdCardFrontInfo;
import cn.smartjavaai.ocr.entity.IdCardInfo;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 身份证 OCR 识别服务实现
 *
 * - 支持从 Image 开始：内部完成预处理（方向矫正 OcrDirectionModel）+ OCR + 解析
 * - 支持从 OcrInfo 开始：只做结构化解析
 *
 * 通过 Lombok @Accessors(chain = true) 支持链式设置依赖。
 */
@Slf4j
@Data
@Accessors(chain = true)
public class DefaultIdCardRecognizer implements IdCardRecognizer {

    private static final int DEBUG_DRAW_FONT_SIZE = 20;

    /**
     * 文本识别模型（可选；仅当从 Image 开始时需要）
     */
    private OcrCommonRecModel recModel;

    /**
     * 识别选项（如是否方向矫正、是否按行返回等）
     */
    private OcrRecOptions recOptions;

    /**
     * 方向检测模型（可选；预处理时用于 0/90/180/270° 整图方向矫正）。
     * 未设置则跳过方向矫正。
     */
    private OcrDirectionModel directionModel;

    /**
     * 图像预处理器（方向矫正，可选，默认按需 lazy-init）
     */
    private IdCardPreprocessor preprocessor;

    /**
     * 正面解析器（默认按需 lazy-init 为 IdCardFrontParser）
     */
    private IdCardParser<IdCardFrontInfo> frontParser;

    /**
     * 反面解析器（默认按需 lazy-init 为 IdCardBackParser）
     */
    private IdCardParser<IdCardBackInfo> backParser;

    /**
     * 校验器（可选，默认按需 lazy-init 为 IdCardValidator）
     */
    private IdCardValidator validator;

    /**
     * 是否进行图像预处理（方向矫正）。默认 true。
     * 若图片已校正，可设为 false 以跳过预处理、节省时间。
     */
    private boolean enablePreprocess = true;

    /**
     * 预处理调试监听器（可选）。
     * 若设置，则在方向矫正阶段结束后回调当前图像，
     * 方便最终用户在需要时保存中间结果进行排查和可视化调试。
     * 默认 null，不影响正常业务逻辑。
     */
    private IdCardPreprocessListener preprocessListener;

    @Override
    public IdCardFrontInfo recognizeFront(Image image) {
        if (recModel == null) {
            throw new IllegalStateException("recModel 未配置，无法从 Image 执行身份证识别，请先设置 recModel 或改用 recognizeFront(OcrInfo)。");
        }
        long totalStart = System.nanoTime();
        Image toRecognize = image;
        IdCardPreprocessResult preprocessResult = null;
        if (enablePreprocess) {
            long preprocessStart = System.nanoTime();
            IdCardPreprocessor pp = (preprocessor != null) ? preprocessor : (preprocessor = new IdCardPreprocessor());
            preprocessResult = pp.preprocess(image, recModel.getTextDetModel(), directionModel, preprocessListener);
            toRecognize = preprocessResult.getProcessedImage();
            log.debug("身份证正面预处理耗时={}ms", elapsedMillis(preprocessStart));
        }
        long recognizeStart = System.nanoTime();
        OcrInfo ocrInfo = preprocessResult != null
                && !preprocessResult.isRotated()
                && preprocessResult.getReusableBoxes() != null
                && !preprocessResult.getReusableBoxes().isEmpty()
                ? recModel.recognize(toRecognize, preprocessResult.getReusableBoxes(), recOptions)
                : recModel.recognize(toRecognize, recOptions);
        log.debug("身份证正面OCR模型调用耗时={}ms", elapsedMillis(recognizeStart));
        notifyAfterRecognize(toRecognize, ocrInfo);
        IdCardFrontInfo info = recognizeFront(ocrInfo);
        log.debug("身份证正面总耗时={}ms", elapsedMillis(totalStart));
        return info;
    }

    @Override
    public IdCardBackInfo recognizeBack(Image image) {
        if (recModel == null) {
            throw new IllegalStateException("recModel 未配置，无法从 Image 执行身份证识别，请先设置 recModel 或改用 recognizeBack(OcrInfo)。");
        }
        long totalStart = System.nanoTime();
        Image toRecognize = image;
        IdCardPreprocessResult preprocessResult = null;
        if (enablePreprocess) {
            long preprocessStart = System.nanoTime();
            IdCardPreprocessor pp = (preprocessor != null) ? preprocessor : (preprocessor = new IdCardPreprocessor());
            preprocessResult = pp.preprocess(image, recModel.getTextDetModel(), directionModel, preprocessListener);
            toRecognize = preprocessResult.getProcessedImage();
            log.debug("身份证反面预处理耗时={}ms", elapsedMillis(preprocessStart));
        }
        long recognizeStart = System.nanoTime();
        OcrInfo ocrInfo = preprocessResult != null
                && !preprocessResult.isRotated()
                && preprocessResult.getReusableBoxes() != null
                && !preprocessResult.getReusableBoxes().isEmpty()
                ? recModel.recognize(toRecognize, preprocessResult.getReusableBoxes(), recOptions)
                : recModel.recognize(toRecognize, recOptions);
        log.debug("身份证反面OCR模型调用耗时={}ms", elapsedMillis(recognizeStart));
        notifyAfterRecognize(toRecognize, ocrInfo);
        IdCardBackInfo info = recognizeBack(ocrInfo);
        log.debug("身份证反面总耗时={}ms", elapsedMillis(totalStart));
        return info;
    }

    private void notifyAfterRecognize(Image toRecognize, OcrInfo ocrInfo) {
        if (preprocessListener == null || toRecognize == null || ocrInfo == null) {
            return;
        }
        try {
            //打印
            log.debug("身份证 OCR 调试结果：{}", JsonUtils.toJson(ocrInfo));
            Image drawImage = ImageUtils.copy(toRecognize);
            List<OcrItem> ocrItems = ocrInfo.getOcrItemList();
            if ((ocrItems == null || ocrItems.isEmpty()) && ocrInfo.getLineList() != null) {
                ocrItems = ocrInfo.flattenLines();
            }
            if (ocrItems != null && !ocrItems.isEmpty()) {
                OcrUtils.drawOcrResult(drawImage, ocrItems, DEBUG_DRAW_FONT_SIZE);
            }
            preprocessListener.onAfterRecognize(drawImage, ocrInfo);
        } catch (Exception e) {
            log.warn("身份证 OCR 调试结果绘制失败，跳过 onAfterRecognize 回调。", e);
        }
    }


    @Override
    public IdCardFrontInfo recognizeFront(OcrInfo ocrInfo) {
        long start = System.nanoTime();
        IdCardParser<IdCardFrontInfo> parser =
                (frontParser != null) ? frontParser : (frontParser = new IdCardFrontParser());
        IdCardFrontInfo info = parser.parse(ocrInfo);
        IdCardValidator v = (validator != null) ? validator : (validator = new IdCardValidator());
        v.validateFront(info);
        return info;
    }

    @Override
    public IdCardBackInfo recognizeBack(OcrInfo ocrInfo) {
        IdCardParser<IdCardBackInfo> parser =
                (backParser != null) ? backParser : (backParser = new IdCardBackParser());
        IdCardBackInfo info = parser.parse(ocrInfo);
        IdCardValidator v = (validator != null) ? validator : (validator = new IdCardValidator());
        v.validateBack(info);
        return info;
    }

    @Override
    public IdCardInfo recognizeBoth(Image frontImage, Image backImage) {
        long totalStart = System.nanoTime();
        IdCardInfo info = new IdCardInfo();
        info.setFront(recognizeFront(frontImage));
        info.setBack(recognizeBack(backImage));
        IdCardValidator v = (validator != null) ? validator : (validator = new IdCardValidator());
        v.validate(info);
        log.debug("身份证正反面识别总耗时={}ms", elapsedMillis(totalStart));
        return info;
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

}
