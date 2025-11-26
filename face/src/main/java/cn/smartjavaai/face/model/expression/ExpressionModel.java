package cn.smartjavaai.face.model.expression;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.ExpressionResult;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author dwj
 * @date 2025/7/1
 */
public interface ExpressionModel extends AutoCloseable{


    /**
     * 加载模型
     * @param config
     */
    void loadModel(FaceExpressionConfig config); // 加载模型



    /**
     * 表情识别(多人脸)
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<DetectionResponse> detect(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(多人脸)
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default R<DetectionResponse> detect(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(多人脸)
     * @param imageData 图片字节流
     * @return
     */
    @Deprecated
    default R<DetectionResponse> detect(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(多人脸)
     * @param base64Image
     * @return
     */
    @Deprecated
    default R<DetectionResponse> detectBase64(String base64Image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(多人脸)
     * @param imagePath 图片路径
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default R<List<ExpressionResult>> detect(String imagePath, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(多人脸)
     * @param imageData 图片数据
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default R<List<ExpressionResult>> detect(byte[] imageData,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(多人脸)
     * @param image BufferedImage
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default R<List<ExpressionResult>> detect(BufferedImage image,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(多人脸)
     * @param base64Image
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default R<List<ExpressionResult>> detectBase64(String base64Image,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(单人脸)
     * @param imagePath 图片路径
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(单人脸)
     * @param imageData
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }




    /**
     * 表情识别(单人脸)
     * @param image BufferedImage
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(单人脸)
     * @param base64Image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detectBase64(String base64Image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(分数最高人脸)
     * @param image
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detectTopFace(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(分数最高人脸)
     * @param imagePath
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detectTopFace(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(分数最高人脸)
     * @param imageData
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detectTopFace(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(分数最高人脸)
     * @param base64Image
     * @return
     */
    @Deprecated
    default R<ExpressionResult> detectTopFaceBase64(String base64Image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(多人脸)
     * @param image
     * @return
     */
    default R<DetectionResponse> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 表情识别(多人脸)
     * @param image
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default R<List<ExpressionResult>> detect(Image image, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(单人脸)
     * @param image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<ExpressionResult> detect(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 表情识别(分数最高人脸)
     * @param image
     * @return
     */
    default R<ExpressionResult> detectTopFace(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }



    default GenericObjectPool<Predictor<Image, Classifications>> getPool(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
