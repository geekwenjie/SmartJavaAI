package cn.smartjavaai.face.model.quality;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.config.QualityConfig;
import cn.smartjavaai.face.entity.FaceQualitySummary;
import cn.smartjavaai.face.entity.FaceQualityResult;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 质量评估模型
 * @author dwj
 * @date 2025/6/23
 */
public interface FaceQualityModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(QualityConfig config);


    /**
     * 亮度评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateBrightness(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 亮度评估
     * @param imagePath
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateBrightness(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 亮度评估
     * @param imageData
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateBrightness(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 清晰度评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateClarity(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 清晰度评估
     * @param imagePath
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateClarity(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 清晰度评估
     * @param imageData
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateClarity(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 完整度评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateCompleteness(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 完整度评估
     * @param imagePath
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateCompleteness(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 完整度评估
     * @param imageData
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateCompleteness(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸姿态评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluatePose(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸姿态评估
     * @param imagePath
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluatePose(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸姿态评估
     * @param imageData
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluatePose(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸分辨率评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateResolution(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸分辨率评估
     * @param imagePath
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateResolution(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸分辨率评估
     * @param imageData
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualityResult> evaluateResolution(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 评估所有
     * @param imagePath
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualitySummary> evaluateAll(String imagePath, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 评估所有
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualitySummary> evaluateAll(BufferedImage image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 评估所有
     * @param imageData
     * @param rectangle
     * @param keyPoints
     * @return
     */
    @Deprecated
    default R<FaceQualitySummary> evaluateAll(byte[] imageData, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 亮度评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    default R<FaceQualityResult> evaluateBrightness(Image image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 清晰度评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    default R<FaceQualityResult> evaluateClarity(Image image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 完整度评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    default R<FaceQualityResult> evaluateCompleteness(Image image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸姿态评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    default R<FaceQualityResult> evaluatePose(Image image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸分辨率评估
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    default R<FaceQualityResult> evaluateResolution(Image image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 评估所有
     * @param image
     * @param rectangle
     * @param keyPoints
     * @return
     */
    default R<FaceQualitySummary> evaluateAll(Image image, DetectionRectangle rectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
