package cn.smartjavaai.face.seetaface;

import cn.smartjavaai.face.model.facedect.SeetaFace6FaceDetModel;
import com.seeta.sdk.FaceDetector;
import com.seeta.sdk.FaceLandmarker;

/**
 * SeetaFace6 人脸检测Detector
 * @author dwj
 */
public class SeetaFace6FaceDetPredictors implements AutoCloseable{

    public FaceDetector faceDetector;
    public FaceLandmarker faceLandmarker;
    public SeetaFace6FaceDetModel model;

    public SeetaFace6FaceDetPredictors(FaceDetector faceDetector, FaceLandmarker faceLandmarker, SeetaFace6FaceDetModel model) {
        this.faceDetector = faceDetector;
        this.faceLandmarker = faceLandmarker;
        this.model = model;
    }

    @Override
    public void close(){
        model.returnPredictor(faceDetector, faceLandmarker);
    }
}
