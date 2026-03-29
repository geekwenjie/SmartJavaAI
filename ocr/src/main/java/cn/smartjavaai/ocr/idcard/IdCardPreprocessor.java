package cn.smartjavaai.ocr.idcard;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.enums.AngleEnum;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * 身份证图像预处理：方向矫正（0/90/180/270°）。
 *
 * - 方向矫正：使用 OcrDirectionModel 检测整图方向并旋转至正向；
 */
@Slf4j
public class IdCardPreprocessor {

    /**
     * 预处理：
     * 1. 方向矫正（若提供 directionModel）：纠正 0/90/180/270° 整体方向；
     * 2. 若提供 listener，则在阶段结束后回调当前图像，便于测试保存中间结果。
     */
    public Image preprocess(Image src,
                            OcrDirectionModel directionModel,
                            IdCardPreprocessListener listener) {
        if (src == null) {
            return src;
        }
        if (directionModel != null) {
            return applyDirectionCorrection(src, directionModel, listener);
        }
        return src;
    }

    /**
     * 预处理（不关心调试监听器的常规调用入口）。
     */
    public Image preprocess(Image src,
                            OcrDirectionModel directionModel) {
        return preprocess(src, directionModel, null);
    }

    /**
     * 预处理增强版：
     * 1. 先做文本检测
     * 2. 过滤明显过小的文本框，降低方向判断噪声
     * 3. 使用已有文本框做方向检测
     * 4. 若无需旋转，则直接返回可复用的检测框
     * 5. 若需要旋转，则返回旋转后的图像，检测框不复用
     */
    public IdCardPreprocessResult preprocess(Image src,
                                             OcrCommonDetModel textDetModel,
                                             OcrDirectionModel directionModel,
                                             IdCardPreprocessListener listener) {
        IdCardPreprocessResult result = new IdCardPreprocessResult()
                .setProcessedImage(src)
                .setRotated(false);
        if (src == null) {
            return result;
        }
        if (textDetModel == null || directionModel == null) {
            if (directionModel != null) {
                result.setProcessedImage(preprocess(src, directionModel, listener));
                result.setRotated(result.getProcessedImage() != src);
            }
            return result;
        }
        Mat srcMat = null;
        try {
            srcMat = cn.smartjavaai.common.utils.ImageUtils.toMat(src).clone();
            List<OcrBox> detectedBoxes = textDetModel.detect(src);
            List<OcrBox> filteredBoxes = filterSmallBoxes(detectedBoxes);
            if (filteredBoxes.isEmpty()) {
                result.setReusableBoxes(detectedBoxes);
                return result;
            }
            List<OcrItem> directionItems = directionModel.detect(filteredBoxes, srcMat);
            AngleEnum dominant = dominantAngle(directionItems);
            if (dominant != null && dominant != AngleEnum.ANGLE_0) {
                log.debug("方向检测结果为 {}，执行整图旋转矫正。", dominant);
                Image rotated = OcrUtils.rotateImg(src, dominant);
                if (listener != null) {
                    listener.onAfterDirection(rotated);
                }
                return result.setProcessedImage(rotated)
                        .setReusableBoxes(null)
                        .setRotated(true);
            }
            return result.setReusableBoxes(detectedBoxes);
        } catch (Exception e) {
            log.warn("身份证方向矫正失败，使用原图继续。", e);
            return result;
        } finally {
            if (srcMat != null) {
                srcMat.release();
            }
        }
    }

    /**
     * 使用方向模型检测整图方向（0/90/180/270°），并将图片旋转至正向。
     */
    private Image applyDirectionCorrection(Image src,
                                           OcrDirectionModel directionModel,
                                           IdCardPreprocessListener listener) {
        if (src == null || directionModel == null) {
            return src;
        }
        try {
            long detectStart = System.nanoTime();
            List<OcrItem> items = directionModel.detect(src);
            log.debug("身份证方向模型调用耗时={}ms, 检测框数量={}", elapsedMillis(detectStart), items == null ? 0 : items.size());
            log.debug("方向检测结果：{}", items);
            AngleEnum dominant = dominantAngle(items);
            if (dominant != null && dominant != AngleEnum.ANGLE_0) {
                log.info("方向检测结果为 {}，执行整图旋转矫正。", dominant);
                long rotateStart = System.nanoTime();
                Image rotated = OcrUtils.rotateImg(src, dominant);
                log.debug("身份证整图旋转耗时={}ms", elapsedMillis(rotateStart));
                if (listener != null) {
                    listener.onAfterDirection(rotated);
                }
                return rotated;
            }
        } catch (Exception e) {
            log.warn("身份证方向矫正失败，使用原图继续。", e);
        }
        return src;
    }

    /**
     * 从方向检测结果中取主角度：
     * 1. 先过滤掉明显的小框，减少碎框噪声；
     * 2. 再按 面积 * score 做加权投票；
     * 3. 若过滤后为空，则退化为对全部框做加权投票。
     */
    private AngleEnum dominantAngle(List<OcrItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        double maxArea = 0.0;
        for (OcrItem item : items) {
            if (item == null || item.getAngle() == null || item.getOcrBox() == null) {
                continue;
            }
            maxArea = Math.max(maxArea, estimateBoxArea(item.getOcrBox()));
        }
        if (maxArea <= 0.0) {
            return weightedVote(items, 0.0);
        }

        double minArea = maxArea * 0.15;
        AngleEnum dominant = weightedVote(items, minArea);
        if (dominant != null) {
            return dominant;
        }
        return weightedVote(items, 0.0);
    }

    private AngleEnum weightedVote(List<OcrItem> items, double minArea) {
        AngleEnum first = null;
        double weight0 = 0.0, weight90 = 0.0, weight180 = 0.0, weight270 = 0.0;
        for (OcrItem item : items) {
            if (item == null || item.getAngle() == null || item.getOcrBox() == null) {
                continue;
            }
            double area = estimateBoxArea(item.getOcrBox());
            if (area < minArea) {
                continue;
            }
            double score = item.getScore() > 0 ? item.getScore() : 1.0;
            double weight = area * score;
            AngleEnum angle = item.getAngle();
            if (first == null) {
                first = angle;
            }
            switch (angle) {
                case ANGLE_0: weight0 += weight; break;
                case ANGLE_90: weight90 += weight; break;
                case ANGLE_180: weight180 += weight; break;
                case ANGLE_270: weight270 += weight; break;
            }
        }
        double maxWeight = Math.max(Math.max(weight0, weight90), Math.max(weight180, weight270));
        if (maxWeight <= 0.0) {
            return first;
        }
        if (Double.compare(weight90, maxWeight) == 0) return AngleEnum.ANGLE_90;
        if (Double.compare(weight270, maxWeight) == 0) return AngleEnum.ANGLE_270;
        if (Double.compare(weight180, maxWeight) == 0) return AngleEnum.ANGLE_180;
        if (Double.compare(weight0, maxWeight) == 0) return AngleEnum.ANGLE_0;
        return first;
    }

    private List<OcrBox> filterSmallBoxes(List<OcrBox> boxes) {
        if (boxes == null || boxes.isEmpty()) {
            return boxes;
        }
        List<OcrItem> items = new ArrayList<>(boxes.size());
        for (OcrBox box : boxes) {
            items.add(new OcrItem(box, AngleEnum.ANGLE_0, 1.0f));
        }
        List<OcrItem> filteredItems = IdCardOcrUtils.filterSmallBoxes(items, 0.15);
        List<OcrBox> filteredBoxes = new ArrayList<>(filteredItems.size());
        for (OcrItem item : filteredItems) {
            if (item != null && item.getOcrBox() != null) {
                filteredBoxes.add(item.getOcrBox());
            }
        }
        return filteredBoxes.isEmpty() ? boxes : filteredBoxes;
    }

    private double estimateBoxArea(OcrBox box) {
        return IdCardOcrUtils.estimateBoxArea(box);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

}
