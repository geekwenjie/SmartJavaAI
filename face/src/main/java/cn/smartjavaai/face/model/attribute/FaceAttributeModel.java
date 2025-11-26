package cn.smartjavaai.face.model.attribute;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.face.config.FaceAttributeConfig;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 人脸属性识别模型
 * @author dwj
 */
public interface FaceAttributeModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(FaceAttributeConfig config); // 加载模型


    /**
     * 人脸属性识别(多人脸)
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default DetectionResponse detect(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(多人脸)
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default DetectionResponse detect(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(多人脸)
     * @param imageData 图片字节流
     * @return
     */
    @Deprecated
    default DetectionResponse detect(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(多人脸)
     * @param imagePath 图片路径
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default List<FaceAttribute> detect(String imagePath, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(单人脸)
     * @param imagePath 图片路径
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default FaceAttribute detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(多人脸)
     * @param imageData 图片数据
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default List<FaceAttribute> detect(byte[] imageData,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(单人脸)
     * @param imageData
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default FaceAttribute detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 人脸属性识别(多人脸)
     * @param image BufferedImage
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    @Deprecated
    default List<FaceAttribute> detect(BufferedImage image,DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(单人脸)
     * @param image BufferedImage
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    @Deprecated
    default FaceAttribute detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 人脸属性识别(分数最高人脸)
     * @param image
     * @return
     */
    @Deprecated
    default FaceAttribute detectTopFace(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 人脸属性识别(分数最高人脸)
     * @param imagePath
     * @return
     */
    @Deprecated
    default FaceAttribute detectTopFace(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(分数最高人脸)
     * @param imageData
     * @return
     */
    @Deprecated
    default FaceAttribute detectTopFace(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(裁剪后的人脸)
     * @param image
     * @return
     */
    @Deprecated
    default FaceAttribute detectCropedFace(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(裁剪后的人脸)
     * @param imagePath
     * @return
     */
    @Deprecated
    default FaceAttribute detectCropedFace(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(裁剪后的人脸)
     * @param imageData
     * @return
     */
    @Deprecated
    default FaceAttribute detectCropedFace(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 人脸属性识别(多人脸)
     * @param image
     * @return
     */
    default DetectionResponse detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(多人脸)
     * @param image
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default List<FaceAttribute> detect(Image image, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(单人脸)
     * @param image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default FaceAttribute detect(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸属性识别(分数最高人脸)
     * @param image
     * @return
     */
    default FaceAttribute detectTopFace(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 人脸属性识别(裁剪后的人脸)
     * @param image
     * @return
     */
    default FaceAttribute detectCropedFace(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }







}
