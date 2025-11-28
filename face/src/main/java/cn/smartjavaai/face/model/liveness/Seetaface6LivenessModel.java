package cn.smartjavaai.face.model.liveness;

import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.LivenessModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetectManager;
import cn.smartjavaai.face.model.facedect.SeetaFace6FaceDetModel;
import cn.smartjavaai.face.seetaface.NativeLoader;
import cn.smartjavaai.face.utils.FaceUtils;
import cn.smartjavaai.face.utils.Seetaface6Utils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * seetaface6 活体检测模型
 * @author dwj
 * @date 2025/4/30
 */
@Slf4j
public class Seetaface6LivenessModel implements LivenessDetModel{


    private FaceDetectorPool faceDetectorPool;
    private FaceAntiSpoofingPool faceAntiSpoofingPool;
    private FaceLandmarkerPool faceLandmarkerPool;

    private LivenessConfig config;

    static {
        //视频功能需要
        OpenCV.loadLocally();
    }

    @Override
    public void loadModel(LivenessConfig config) {
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        if(Objects.isNull(config.getDetectModel())){
            throw new FaceException("未指定人脸检测模型");
        }
        this.config = config;
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.debug("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
        String[] faceAntiSpoofingModelPath = {config.getModelPath() + File.separator + "fas_first.csta",config.getModelPath() + File.separator + "fas_second.csta"};
        String[] faceLandmarkerModelPath = {config.getModelPath() + File.separator + "face_landmarker_pts5.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = config.getGpuId();
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
        }

        try {
            SeetaModelSetting faceDetectorPoolSetting = new SeetaModelSetting(gpuId, faceDetectorModelPath, device);
            SeetaConfSetting faceDetectorPoolConfSetting = new SeetaConfSetting(faceDetectorPoolSetting);

            SeetaModelSetting faceLandmarkerPoolSetting = new SeetaModelSetting(gpuId, faceLandmarkerModelPath, device);
            SeetaConfSetting faceLandmarkerPoolConfSetting = new SeetaConfSetting(faceLandmarkerPoolSetting);

            SeetaModelSetting faceAntiSpoofingSetting = new SeetaModelSetting(gpuId, faceAntiSpoofingModelPath, device);
            SeetaConfSetting faceAntiSpoofingPoolConfSetting = new SeetaConfSetting(faceAntiSpoofingSetting);

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceAntiSpoofingPool = new FaceAntiSpoofingPool(faceAntiSpoofingPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);

            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }

            faceDetectorPool.setMaxTotal(predictorPoolSize);
            faceAntiSpoofingPool.setMaxTotal(predictorPoolSize);
            faceLandmarkerPool.setMaxTotal(predictorPoolSize);
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);

            //初始化模型参数
            initConfig();
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }
    }

    /**
     * 初始化模型参数
     */
    private void initConfig(){
        FaceAntiSpoofing faceAntiSpoofing = null;
        //设置参数
        try {
            //人脸清晰度阈值
            float faceClarityThreshold = LivenessConstant.DEFAULT_FACE_CLARITY_THRESHOLD;
            //活体阈值
            float realityThreshold = LivenessConstant.DEFAULT_REALITY_THRESHOLD;
            Float faceClarityThresholdValue = config.getCustomParam("faceClarityThreshold", Float.class);
            if(Objects.nonNull(faceClarityThresholdValue)){
                faceClarityThreshold = faceClarityThresholdValue;
            }
            Float realityThresholdValue = config.getRealityThreshold();
            if(Objects.nonNull(realityThresholdValue)){
                realityThreshold = realityThresholdValue;
            }
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            faceAntiSpoofing.SetThreshold(faceClarityThreshold, realityThreshold);
            faceAntiSpoofing.SetVideoFrameCount(config.getFrameCount());
        } catch (Exception e) {
            throw new FaceException(e);
        } finally {
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }


    private R<LivenessResult> detect(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints, boolean isImage) {
        if(Objects.isNull(faceDetectionRectangle)){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        if(keyPoints == null || keyPoints.isEmpty()){
            return R.fail(1002,"人脸关键点keyPoints为空");
        }
        FaceAntiSpoofing.Status status = null;
        FaceAntiSpoofing faceAntiSpoofing = null;
        try {
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = Seetaface6Utils.convertToSeetaRect(faceDetectionRectangle);
            SeetaPointF[] landmarks = Seetaface6Utils.convertToSeetaPointF(keyPoints);
            //检测图片
            if(isImage){
                status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
            }else{
                //检测视频
                status = faceAntiSpoofing.PredictVideo(imageData, seetaRect, landmarks);
            }
            return R.ok(new LivenessResult(Seetaface6Utils.convertToLivenessStatus(status)));
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    private R<LivenessResult> detectVideoFrame(Image image, FaceDetectManager faceDetectManager, FaceAntiSpoofing faceAntiSpoofing) {
        //检测人脸
        R<DetectionInfo> detectResult = faceDetectManager.detectTopFace(image);
        if(!detectResult.isSuccess()){
            return R.fail(detectResult.getCode(), detectResult.getMessage());
        }
        DetectionInfo detectionInfo = detectResult.getData();
        if(Objects.isNull(detectionInfo)){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        if(detectionInfo.getFaceInfo().getKeyPoints() == null || detectionInfo.getFaceInfo().getKeyPoints().isEmpty()){
            return R.fail(1002,"人脸关键点keyPoints为空");
        }
        FaceAntiSpoofing.Status status = null;
        try {
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = Seetaface6Utils.convertToSeetaRect(detectionInfo.getDetectionRectangle());
            SeetaPointF[] landmarks = Seetaface6Utils.convertToSeetaPointF(detectionInfo.getFaceInfo().getKeyPoints());
            //检测视频
            status = faceAntiSpoofing.PredictVideo(imageData, seetaRect, landmarks);
            return R.ok(new LivenessResult(Seetaface6Utils.convertToLivenessStatus(status)));
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        }
    }




    private R<LivenessResult> detectVideo(FFmpegFrameGrabber grabber) {
        FaceAntiSpoofing faceAntiSpoofing = null;
        try (FaceDetectManager faceDetectManager = new FaceDetectManager(config.getDetectModel())){
            //初始化predictors
            faceDetectManager.borrowPredictors();
            faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
            //重置视频
            faceAntiSpoofing.ResetVideo();
            grabber.start();
            // 获取视频总帧数
            int totalFrames = grabber.getLengthInFrames();
            int videoFrameCountConfig = faceAntiSpoofing.GetVideoFrameCount();
            log.debug("视频总帧数：{}，检测帧数：{}", totalFrames, videoFrameCountConfig);
            if(totalFrames < videoFrameCountConfig){
                return R.fail(1001, "视频帧数低于检测帧数");
            }
            // 逐帧处理视频
            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                if(frameIndex >= config.getMaxVideoDetectFrames()){
                    return R.fail(10002, "视频中未检测到人脸，超出最大检测帧数：" + config.getMaxVideoDetectFrames());
                }
                // 获取当前帧
                Frame frame = grabber.grabImage();
                if (frame != null) {
                    BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
                    Image image = SmartImageFactory.getInstance().fromBufferedImage(bufferedImage);
                    R<LivenessResult> livenessStatus = detectVideoFrame(image, faceDetectManager, faceAntiSpoofing);
                    if(!livenessStatus.isSuccess()){
                        log.debug("第" + frameIndex + "帧处理失败：" + livenessStatus.getMessage());
                        continue;
                    }
                    //满足检测帧数之后停止检测
                    if(livenessStatus.getData().getStatus() != LivenessStatus.DETECTING){
                        return livenessStatus;
                    }
                }
            }
            grabber.stop();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new FaceException(e);
        } catch (Exception e) {
            throw new FaceException(e);
        } finally {
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            try {
                grabber.release();
            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        }
        return R.fail(1000, "有效帧数量不足，无法完成活体检测");
    }



    @Override
    public R<DetectionResponse> detect(Image image) {
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        try {
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
                detectPredictor = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
                List<LivenessStatus> livenessStatusList = new ArrayList<LivenessStatus>();
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = ImageUtils.getMatrixBGR(image);
                //检测人脸
                SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
                if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(SeetaRect seetaRect : seetaResult){
                    SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, landmarks);
                    seetaPointFSList.add(landmarks);
                    //检测图片
                    FaceAntiSpoofing.Status status = faceAntiSpoofing.Predict(imageData, seetaRect, landmarks);
                    livenessStatusList.add(Seetaface6Utils.convertToLivenessStatus(status));
                }
                return R.ok(Seetaface6Utils.convertToDetectionResponse(seetaResult, seetaPointFSList, livenessStatusList));
            }else{
                R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
                if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(DetectionInfo detectionInfo : faceDetectionResponse.getData().getDetectionInfoList()){
                    R<LivenessResult> result = detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
                    if(!result.isSuccess()){
                        return R.fail(result.getCode(), result.getMessage());
                    }
                    if(Objects.isNull(detectionInfo.getFaceInfo())){
                        detectionInfo.setFaceInfo(new FaceInfo());
                    }
                    detectionInfo.getFaceInfo().setLivenessStatus(result.getData());
                }
                return faceDetectionResponse;
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (detectPredictor != null) {
                try {
                    faceDetectorPool.returnObject(detectPredictor);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<List<LivenessResult>> detect(Image image, DetectionResponse faceDetectionResponse) {
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        List<LivenessResult> livenessStatusList = new ArrayList<LivenessResult>();
        for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
            }
            R<LivenessResult> result = detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
            if(!result.isSuccess()){
                return R.fail(result.getCode(), result.getMessage());
            }
            livenessStatusList.add(result.getData());
        }
        return R.ok(livenessStatusList);
    }

    @Override
    public R<LivenessResult> detect(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        return detect(image, faceDetectionRectangle, keyPoints, true);
    }

    @Override
    public R<LivenessResult> detectTopFace(Image image) {
        return detectTopFace(image, true);
    }

    private R<LivenessResult> detectTopFace(Image image, boolean isImage) {
        FaceAntiSpoofing faceAntiSpoofing = null;
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        try {
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                detectPredictor = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                faceAntiSpoofing = faceAntiSpoofingPool.borrowObject();
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = ImageUtils.getMatrixBGR(image);
                //检测人脸
                SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
                if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaResult[0], landmarks);
                FaceAntiSpoofing.Status status = null;
                //检测图片
                if(isImage){
                    status = faceAntiSpoofing.Predict(imageData, seetaResult[0], landmarks);
                }else{
                    status = faceAntiSpoofing.PredictVideo(imageData, seetaResult[0], landmarks);
                }
                return R.ok(new LivenessResult(Seetaface6Utils.convertToLivenessStatus(status)));
            }else{
                R<DetectionResponse> faceDetectionResponse = config.getDetectModel().detect(image);
                if(Objects.isNull(faceDetectionResponse.getData()) || Objects.isNull(faceDetectionResponse.getData().getDetectionInfoList()) || faceDetectionResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                DetectionInfo detectionInfo = faceDetectionResponse.getData().getDetectionInfoList().get(0);
                R<LivenessResult> detectionResponseR = detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints(), isImage);
                return detectionResponseR;
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (detectPredictor != null) {
                try {
                    faceDetectorPool.returnObject(detectPredictor);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceAntiSpoofing != null) {
                try {
                    faceAntiSpoofingPool.returnObject(faceAntiSpoofing);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<LivenessResult> detectVideo(InputStream videoInputStream) {
        if(Objects.isNull(videoInputStream)){
            throw new FaceException("视频无效");
        }
        return detectVideo(new FFmpegFrameGrabber(videoInputStream));
    }

    @Override
    public R<LivenessResult> detectVideo(String videoPath) {
        if(!FileUtils.isFileExists(videoPath)){
            throw new FaceException("视频文件不存在");
        }
        return detectVideo(new FFmpegFrameGrabber(videoPath));
    }

    public FaceDetectorPool getFaceDetectorPool() {
        return faceDetectorPool;
    }

    public FaceAntiSpoofingPool getFaceAntiSpoofingPool() {
        return faceAntiSpoofingPool;
    }

    public FaceLandmarkerPool getFaceLandmarkerPool() {
        return faceLandmarkerPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            LivenessModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (faceDetectorPool != null) {
                faceDetectorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (faceAntiSpoofingPool != null) {
                faceAntiSpoofingPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (faceLandmarkerPool != null) {
                faceLandmarkerPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
    }

    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }
}
