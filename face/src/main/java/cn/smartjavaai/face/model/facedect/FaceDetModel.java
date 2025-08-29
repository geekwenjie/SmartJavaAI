package cn.smartjavaai.face.model.facedect;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.config.FaceDetConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * 人脸检测模型
 * @author dwj
 */
public interface FaceDetModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(FaceDetConfig config); // 加载模型


    /**
     * 人脸检测
     * @param imagePath 图片路径
     * @return
     */
    default R<DetectionResponse> detect(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸检测
     * @param imageInputStream 图片输入流
     * @return
     */
    default R<DetectionResponse> detect(InputStream imageInputStream){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸检测
     * @param image BufferedImage
     * @return
     */
    default R<DetectionResponse> detect(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸检测
     * @param imageData
     * @return
     */
    default R<DetectionResponse> detect(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 人脸检测
     * @param base64Image
     * @return
     */
    default R<DetectionResponse> detectBase64(String base64Image) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制人脸
     * @param imagePath 图片输入路径（包含文件名称）
     * @param outputPath 图片输出路径（包含文件名称）
     */
    default R<Void> detectAndDraw(String imagePath, String outputPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 检测并绘制人脸
     * @param sourceImage
     * @return
     */
    default R<BufferedImage> detectAndDraw(BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default GenericObjectPool<Predictor<Image, DetectedObjects>> getPool(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
