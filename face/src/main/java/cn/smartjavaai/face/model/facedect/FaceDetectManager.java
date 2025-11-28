package cn.smartjavaai.face.model.facedect;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.model.facedect.mtcnn.MtcnnPredictors;
import cn.smartjavaai.face.seetaface.SeetaFace6FaceDetPredictors;
import cn.smartjavaai.face.utils.FaceUtils;

import java.util.Objects;

/**
 * @author dwj
 * @date 2025/11/24
 */
public class FaceDetectManager implements AutoCloseable{


    private FaceDetModel faceDetModel;

    public FaceDetectManager(FaceDetModel faceDetModel) {
        this.faceDetModel = faceDetModel;
    }

    private MtcnnPredictors mtcnnPredictors;

    private SeetaFace6FaceDetPredictors seetaFace6FaceDetPredictors;

    private Predictor<Image, DetectedObjects> commonPredictor;



    public void borrowPredictors(){
        try {
            //mtcnn
            if(faceDetModel instanceof MtcnnFaceDetModel){
                MtcnnFaceDetModel mtcnnFaceDetModel = (MtcnnFaceDetModel) faceDetModel;
                mtcnnPredictors = mtcnnFaceDetModel.borrowPredictors();
            }else if(faceDetModel instanceof SeetaFace6FaceDetModel){
                //SeetaFace6
                SeetaFace6FaceDetModel seetaFace6FaceDetModel = (SeetaFace6FaceDetModel) faceDetModel;
                seetaFace6FaceDetPredictors = seetaFace6FaceDetModel.borrowPredictors();
            }else{
                //其他通用模型
                commonPredictor = faceDetModel.borrowPredictor();
            }
        } catch (Exception e) {
            throw new FaceException("获取predictors异常", e);
        }
    }

    public R<DetectionInfo> detectTopFace(Image image){
        DetectionResponse detectionResponse = null;
        try {
            //mtcnn
            if(faceDetModel instanceof MtcnnFaceDetModel){
                MtcnnFaceDetModel mtcnnFaceDetModel = (MtcnnFaceDetModel) faceDetModel;
                DetectedObjects detections = mtcnnFaceDetModel.detectCoreByPredictors(image, mtcnnPredictors);
                detectionResponse = FaceUtils.convertToDetectionResponse(detections, image);
            }else if(faceDetModel instanceof SeetaFace6FaceDetModel){
                //SeetaFace6
                SeetaFace6FaceDetModel seetaFace6FaceDetModel = (SeetaFace6FaceDetModel) faceDetModel;
                detectionResponse = seetaFace6FaceDetModel.detectByPredictors(image, seetaFace6FaceDetPredictors);
            }else{
                DetectedObjects detections = commonPredictor.predict(image);
                detectionResponse = FaceUtils.convertToDetectionResponse(detections, image);
            }
        } catch (Exception e) {
            throw new FaceException("获取predictors异常", e);
        }
        if(Objects.isNull(detectionResponse) || Objects.isNull(detectionResponse.getDetectionInfoList()) || detectionResponse.getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        DetectionInfo detectionInfo = detectionResponse.getDetectionInfoList().get(0);
        return R.ok(detectionInfo);
    }


    @Override
    public void close(){
        try {
            //mtcnn
            if(faceDetModel instanceof MtcnnFaceDetModel){
                mtcnnPredictors.close();
            }else if(faceDetModel instanceof SeetaFace6FaceDetModel){
                //SeetaFace6
                seetaFace6FaceDetPredictors.close();
            }else{
                faceDetModel.getPool().returnObject(commonPredictor);
            }
        } catch (Exception e) {
            throw new FaceException("归还predictors异常", e);
        }
    }
}
