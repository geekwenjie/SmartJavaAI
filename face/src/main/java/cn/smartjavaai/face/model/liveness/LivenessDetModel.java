package cn.smartjavaai.face.model.liveness;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.face.config.LivenessConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 活体检测模型
 * @author dwj
 */
public interface LivenessDetModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(LivenessConfig config); // 加载模型

    /**
     * 视频活体检测
     * @param videoInputStream
     * @return
     */
    default R<LivenessResult> detectVideo(InputStream videoInputStream){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 视频活体检测
     * @param videoPath
     * @return
     */
    default R<LivenessResult> detectVideo(String videoPath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 活体检测(多人脸)
     * @param image
     * @return
     */
    default R<DetectionResponse> detect(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(多人脸)
     * @param image
     * @param faceDetectionResponse 人脸检测结果
     * @return
     */
    default R<List<LivenessResult>> detect(Image image, DetectionResponse faceDetectionResponse){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(单人脸)
     * @param image
     * @param faceDetectionRectangle 人脸检测结果-人脸框
     * @return
     */
    default R<LivenessResult> detect(Image image, DetectionRectangle faceDetectionRectangle){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 活体检测(分数最高人脸)
     * @param image
     * @return
     */
    default R<LivenessResult> detectTopFace(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default GenericObjectPool<Predictor<Image, Float>> getPool() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }



}
