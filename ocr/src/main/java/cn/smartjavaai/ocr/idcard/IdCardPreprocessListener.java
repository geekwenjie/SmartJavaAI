package cn.smartjavaai.ocr.idcard;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.entity.OcrInfo;

/**
 * 身份证预处理调试监听器。
 *
 * 在预处理或识别流程中，只有当某个阶段真的产出了中间结果时，才会回调当前图像，
 * 便于测试环境中将中间结果保存到磁盘进行对比和调试。
 *
 * 生产环境可不设置监听器，完全不影响正常流程。
 */
public interface IdCardPreprocessListener {

    /**
     * 方向矫正完成后回调。
     * 如果方向检测结果为 0 且未发生旋转，则不会调用。
     */
    default void onAfterDirection(Image image) {}

    /**
     * OCR 识别完成后回调。
     *
     * 传入的 image 为预处理后的图片副本，已绘制 OCR 检测框，便于直接保存调试结果。
     * 如果识别流程失败或未进入识别阶段，则不会调用。
     */
    default void onAfterRecognize(Image image, OcrInfo ocrInfo) {}
}
