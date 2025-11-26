package cn.smartjavaai.face.model.attribute;

import ai.djl.engine.Engine;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.HeadPose;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.EyeStatus;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.PoolUtils;
import cn.smartjavaai.face.config.FaceAttributeConfig;
import cn.smartjavaai.common.enums.face.GenderType;
import cn.smartjavaai.face.context.PredictorContext;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceAttributeModelFactory;
import cn.smartjavaai.face.seetaface.NativeLoader;
import cn.smartjavaai.face.utils.FaceUtils;
import cn.smartjavaai.face.utils.Seetaface6Utils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * seetaface6 人脸属性识别模型
 * @author dwj
 * @date 2025/4/30
 */
@Slf4j
public class Seetaface6FaceAttributeModel implements FaceAttributeModel {


    private FaceDetectorPool faceDetectorPool;
    private GenderPredictorPool genderPredictorPool;
    private FaceLandmarkerPool faceLandmarkerPool;
    private AgePredictorPool agePredictorPool;
    private EyeStateDetectorPool eyeStateDetectorPool;
    private MaskDetectorPool maskDetectorPool;
    private PoseEstimatorPool poseEstimatorPool;

    private FaceAttributeConfig config;


    @Override
    public void loadModel(FaceAttributeConfig config) {
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        this.config = config;
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.debug("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
        String[] faceLandmarkerModelPath = {config.getModelPath() + File.separator + "face_landmarker_pts5.csta"};
        String[] genderPredictorModelPath = {config.getModelPath() + File.separator + "gender_predictor.csta"};
        String[] agePredictorModelPath = {config.getModelPath() + File.separator + "age_predictor.csta"};
        String[] eyeStateDetectorModelPath = {config.getModelPath() + File.separator + "eye_state.csta"};
        String[] maskDetectorModelPath = {config.getModelPath() + File.separator + "mask_detector.csta"};
        String[] poseEstimatorModelPath = {config.getModelPath() + File.separator + "pose_estimation.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = 0;
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
            if(config.getGpuId() >= 0 && device == SeetaDevice.SEETA_DEVICE_GPU){
                gpuId = config.getGpuId();
            }
        }

        try {
            SeetaModelSetting faceDetectorPoolSetting = new SeetaModelSetting(gpuId, faceDetectorModelPath, device);
            SeetaConfSetting faceDetectorPoolConfSetting = new SeetaConfSetting(faceDetectorPoolSetting);

            SeetaModelSetting faceLandmarkerPoolSetting = new SeetaModelSetting(gpuId, faceLandmarkerModelPath, device);
            SeetaConfSetting faceLandmarkerPoolConfSetting = new SeetaConfSetting(faceLandmarkerPoolSetting);

            SeetaModelSetting genderPredictorPoolSetting = new SeetaModelSetting(gpuId, genderPredictorModelPath, device);
            SeetaConfSetting genderPredictorPoolConfSetting = new SeetaConfSetting(genderPredictorPoolSetting);

            SeetaModelSetting agePredictorPoolSetting = new SeetaModelSetting(gpuId, agePredictorModelPath, device);
            SeetaConfSetting agePredictorPoolConfSetting = new SeetaConfSetting(agePredictorPoolSetting);

            SeetaModelSetting eyeStateDetectorPoolSetting = new SeetaModelSetting(gpuId, eyeStateDetectorModelPath, device);
            SeetaConfSetting eyeStateDetectorPoolConfSetting = new SeetaConfSetting(eyeStateDetectorPoolSetting);

            SeetaModelSetting maskDetectorPoolSetting = new SeetaModelSetting(gpuId, maskDetectorModelPath, device);
            SeetaConfSetting maskDetectorPoolConfSetting = new SeetaConfSetting(maskDetectorPoolSetting);

            SeetaModelSetting poseEstimatorPoolSetting = new SeetaModelSetting(gpuId, poseEstimatorModelPath, device);
            SeetaConfSetting poseEstimatorPoolConfSetting = new SeetaConfSetting(poseEstimatorPoolSetting);

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);
            this.genderPredictorPool = new GenderPredictorPool(genderPredictorPoolConfSetting);
            this.agePredictorPool = new AgePredictorPool(agePredictorPoolConfSetting);
            this.eyeStateDetectorPool = new EyeStateDetectorPool(eyeStateDetectorPoolConfSetting);
            this.maskDetectorPool = new MaskDetectorPool(maskDetectorPoolConfSetting);
            this.poseEstimatorPool = new PoseEstimatorPool(poseEstimatorPoolConfSetting);

            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            faceDetectorPool.setMaxTotal(predictorPoolSize);
            faceLandmarkerPool.setMaxTotal(predictorPoolSize);
            genderPredictorPool.setMaxTotal(predictorPoolSize);
            agePredictorPool.setMaxTotal(predictorPoolSize);
            eyeStateDetectorPool.setMaxTotal(predictorPoolSize);
            maskDetectorPool.setMaxTotal(predictorPoolSize);
            poseEstimatorPool.setMaxTotal(predictorPoolSize);
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }
    }

    @Override
    public DetectionResponse detect(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return detect(image);
    }

    @Override
    public DetectionResponse detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public DetectionResponse detect(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
        List<FaceAttribute> faceAttributeList = new ArrayList<FaceAttribute>();
        try {
            detectPredictor = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = BufferedImageUtils.getMatrixBGR(image);
            //检测人脸
            SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("无人脸数据");
            }
            for(SeetaRect seetaRect : seetaResult){
                SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, landmarks);
                seetaPointFSList.add(landmarks);
                //人脸属性检测
                FaceAttribute faceAttribute = detect(imageData, seetaRect, landmarks, predictorContext);
                faceAttributeList.add(faceAttribute);
            }
            return Seetaface6Utils.convertToFaceAttributeResponse(seetaResult, seetaPointFSList, faceAttributeList);
        } catch (Exception e) {
            throw new FaceException("人脸属性检测错误", e);
        } finally {
            // 统一归还所有 Predictor 到池
            PoolUtils.returnToPool(faceDetectorPool, detectPredictor);
            PoolUtils.returnToPool(faceLandmarkerPool, faceLandmarker);
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
    }

    /**
     * 单人脸属性检测
     * @param imageData
     * @param seetaRect
     * @param landmarks
     * @param predictorContext
     * @return
     */
    private FaceAttribute detect(SeetaImageData imageData, SeetaRect seetaRect, SeetaPointF[] landmarks, PredictorContext predictorContext){
        FaceAttribute faceAttribute = new FaceAttribute();
        //性别检测
        GenderType genderType = null;
        if (config.isEnableGender()){
            GenderPredictor.GENDER[] gender = new GenderPredictor.GENDER[1];
            boolean isSuccess = predictorContext.genderPredictor.PredictGenderWithCrop(imageData, landmarks, gender);
            genderType = isSuccess ? Seetaface6Utils.convertToGenderType(gender[0]) : GenderType.UNKNOWN;
        }
        //眼睛状态检测
        EyeStatus leftEyeStatus = null;
        EyeStatus rightEyeStatus = null;
        if (config.isEnableEyeStatus()){
            EyeStateDetector.EYE_STATE[] eyeState  = predictorContext.eyeStateDetector.detect(imageData, landmarks);
            leftEyeStatus = Seetaface6Utils.convertToEyeStatus(eyeState[0]);
            rightEyeStatus = Seetaface6Utils.convertToEyeStatus(eyeState[1]);
        }
        //年龄检测
        Integer age = 0;
        if (config.isEnableAge()){
            age = predictorContext.agePredictor.predictAgeWithCrop(imageData, landmarks);
        }
        //口罩检测
        Boolean wearingMask = null;
        if (config.isEnableMask()){
            float[] score = new float[1];
            wearingMask = predictorContext.maskDetector.detect(imageData, seetaRect, score);
        }
        //姿态检测
        if (config.isEnableHeadPose()){
            float[] yaw = new float[1];//左右转头（水平旋转）
            float[] pitch = new float[1]; //上下抬头/低头（垂直旋转）
            float[] roll = new float[1]; //头部左右倾斜（平面旋转）
            predictorContext.poseEstimator.Estimate(imageData, seetaRect, yaw, pitch, roll);
            faceAttribute.setHeadPose(new HeadPose(yaw[0], pitch[0], roll[0]));
        }
        faceAttribute.setGenderType(genderType);
        faceAttribute.setAge(age);
        faceAttribute.setLeftEyeStatus(leftEyeStatus);
        faceAttribute.setRightEyeStatus(rightEyeStatus);
        faceAttribute.setWearingMask(wearingMask);
        return faceAttribute;
    }

    @Override
    public List<FaceAttribute> detect(String imagePath, DetectionResponse faceDetectionResponse) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return detect(image, faceDetectionResponse);
    }

    @Override
    public List<FaceAttribute> detect(byte[] imageData, DetectionResponse faceDetectionResponse) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionResponse);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public List<FaceAttribute> detect(BufferedImage image, DetectionResponse faceDetectionResponse) {
        if(!BufferedImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无人脸数据");
        }
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        FaceLandmarker faceLandmarker = null;
        List<FaceAttribute> faceAttributeList = new ArrayList<FaceAttribute>();
        try {
            faceLandmarker = faceLandmarkerPool.borrowObject();
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = BufferedImageUtils.getMatrixBGR(image);
                SeetaRect seetaRect = Seetaface6Utils.convertToSeetaRect(detectionInfo.getDetectionRectangle());
                SeetaPointF[] landmarks = null;
                FaceInfo faceInfo = detectionInfo.getFaceInfo();
                //如果没有人脸标识，则提取人脸标识
                if(faceInfo == null || faceInfo.getKeyPoints() == null || faceInfo.getKeyPoints().isEmpty()){
                    //提取人脸的5点人脸标识
                    landmarks = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, landmarks);
                }else{
                    landmarks = Seetaface6Utils.convertToSeetaPointF(faceInfo.getKeyPoints());
                }
                //人脸属性检测
                FaceAttribute faceAttribute = detect(imageData, seetaRect, landmarks, predictorContext);
                faceAttributeList.add(faceAttribute);
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
        return faceAttributeList;
    }

    @Override
    public FaceAttribute detect(String imagePath, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return detect(image, faceDetectionRectangle, keyPoints);
    }




    @Override
    public FaceAttribute detect(byte[] imageData, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)), faceDetectionRectangle, keyPoints);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }


    @Override
    public FaceAttribute detect(BufferedImage image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(!BufferedImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }
        if(Objects.isNull(faceDetectionRectangle)){
            throw new FaceException("无人脸数据");
        }
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        try {
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = BufferedImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = Seetaface6Utils.convertToSeetaRect(faceDetectionRectangle);
            SeetaPointF[] landmarks = null;
            if(keyPoints == null || keyPoints.isEmpty()){
                throw new FaceException("人脸关键点keyPoints为空");
            }
            landmarks = Seetaface6Utils.convertToSeetaPointF(keyPoints);
            //人脸属性检测
            FaceAttribute faceAttribute = detect(imageData, seetaRect, landmarks, predictorContext);
            return faceAttribute;
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
    }

    @Override
    public FaceAttribute detectTopFace(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return detectTopFace(image);
    }

    @Override
    public FaceAttribute detectTopFace(byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            return detectTopFace(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }


    @Override
    public FaceAttribute detectTopFace(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            throw new FaceException("图像无效");
        }

        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        try {
            detectPredictor = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = BufferedImageUtils.getMatrixBGR(image);
            //检测人脸
            SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("无人脸数据");
            }
            SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData, seetaResult[0], landmarks);
            //人脸属性检测
            FaceAttribute faceAttribute = detect(imageData, seetaResult[0], landmarks, predictorContext);
            return faceAttribute;
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

            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
    }

    @Override
    public DetectionResponse detect(Image image) {
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
        List<FaceAttribute> faceAttributeList = new ArrayList<FaceAttribute>();
        try {
            detectPredictor = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            //检测人脸
            SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("无人脸数据");
            }
            for(SeetaRect seetaRect : seetaResult){
                SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, landmarks);
                seetaPointFSList.add(landmarks);
                //人脸属性检测
                FaceAttribute faceAttribute = detect(imageData, seetaRect, landmarks, predictorContext);
                faceAttributeList.add(faceAttribute);
            }
            return Seetaface6Utils.convertToFaceAttributeResponse(seetaResult, seetaPointFSList, faceAttributeList);
        } catch (Exception e) {
            throw new FaceException("人脸属性检测错误", e);
        } finally {
            // 统一归还所有 Predictor 到池
            PoolUtils.returnToPool(faceDetectorPool, detectPredictor);
            PoolUtils.returnToPool(faceLandmarkerPool, faceLandmarker);
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
    }

    @Override
    public List<FaceAttribute> detect(Image image, DetectionResponse faceDetectionResponse) {
        if(Objects.isNull(faceDetectionResponse) || Objects.isNull(faceDetectionResponse.getDetectionInfoList()) || faceDetectionResponse.getDetectionInfoList().isEmpty()){
            throw new FaceException("无人脸数据");
        }
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        FaceLandmarker faceLandmarker = null;
        List<FaceAttribute> faceAttributeList = new ArrayList<FaceAttribute>();
        try {
            faceLandmarker = faceLandmarkerPool.borrowObject();
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            for(DetectionInfo detectionInfo : faceDetectionResponse.getDetectionInfoList()){
                SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
                imageData.data = ImageUtils.getMatrixBGR(image);
                SeetaRect seetaRect = Seetaface6Utils.convertToSeetaRect(detectionInfo.getDetectionRectangle());
                SeetaPointF[] landmarks = null;
                FaceInfo faceInfo = detectionInfo.getFaceInfo();
                //如果没有人脸标识，则提取人脸标识
                if(faceInfo == null || faceInfo.getKeyPoints() == null || faceInfo.getKeyPoints().isEmpty()){
                    //提取人脸的5点人脸标识
                    landmarks = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, landmarks);
                }else{
                    landmarks = Seetaface6Utils.convertToSeetaPointF(faceInfo.getKeyPoints());
                }
                //人脸属性检测
                FaceAttribute faceAttribute = detect(imageData, seetaRect, landmarks, predictorContext);
                faceAttributeList.add(faceAttribute);
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
        return faceAttributeList;
    }

    @Override
    public FaceAttribute detect(Image image, DetectionRectangle faceDetectionRectangle, List<Point> keyPoints) {
        if(Objects.isNull(faceDetectionRectangle)){
            throw new FaceException("无人脸数据");
        }
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        try {
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            SeetaRect seetaRect = Seetaface6Utils.convertToSeetaRect(faceDetectionRectangle);
            SeetaPointF[] landmarks = null;
            if(keyPoints == null || keyPoints.isEmpty()){
                throw new FaceException("人脸关键点keyPoints为空");
            }
            landmarks = Seetaface6Utils.convertToSeetaPointF(keyPoints);
            //人脸属性检测
            FaceAttribute faceAttribute = detect(imageData, seetaRect, landmarks, predictorContext);
            return faceAttribute;
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
        } finally {
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
    }

    @Override
    public FaceAttribute detectTopFace(Image image) {
        FaceLandmarker faceLandmarker = null;
        FaceDetector detectPredictor = null;
        //创建推力器上下文
        PredictorContext predictorContext = new PredictorContext();
        try {
            detectPredictor = faceDetectorPool.borrowObject();
            faceLandmarker = faceLandmarkerPool.borrowObject();
            predictorContext.genderPredictor = config.isEnableGender() ? genderPredictorPool.borrowObject() : null;
            predictorContext.agePredictor = config.isEnableAge() ? agePredictorPool.borrowObject() : null;
            predictorContext.maskDetector = config.isEnableMask() ? maskDetectorPool.borrowObject() : null;
            predictorContext.eyeStateDetector = config.isEnableEyeStatus() ? eyeStateDetectorPool.borrowObject() : null;
            predictorContext.poseEstimator = config.isEnableHeadPose() ? poseEstimatorPool.borrowObject() : null;
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            //检测人脸
            SeetaRect[] seetaResult = detectPredictor.Detect(imageData);
            if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                throw new FaceException("无人脸数据");
            }
            SeetaPointF[] landmarks = new SeetaPointF[faceLandmarker.number()];
            faceLandmarker.mark(imageData, seetaResult[0], landmarks);
            //人脸属性检测
            FaceAttribute faceAttribute = detect(imageData, seetaResult[0], landmarks, predictorContext);
            return faceAttribute;
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

            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker);
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            PoolUtils.returnToPool(genderPredictorPool, predictorContext.genderPredictor);
            PoolUtils.returnToPool(agePredictorPool, predictorContext.agePredictor);
            PoolUtils.returnToPool(maskDetectorPool, predictorContext.maskDetector);
            PoolUtils.returnToPool(eyeStateDetectorPool, predictorContext.eyeStateDetector);
            PoolUtils.returnToPool(poseEstimatorPool, predictorContext.poseEstimator);
        }
    }

    public FaceDetectorPool getFaceDetectorPool() {
        return faceDetectorPool;
    }

    public GenderPredictorPool getGenderPredictorPool() {
        return genderPredictorPool;
    }

    public FaceLandmarkerPool getFaceLandmarkerPool() {
        return faceLandmarkerPool;
    }

    public AgePredictorPool getAgePredictorPool() {
        return agePredictorPool;
    }

    public EyeStateDetectorPool getEyeStateDetectorPool() {
        return eyeStateDetectorPool;
    }

    public MaskDetectorPool getMaskDetectorPool() {
        return maskDetectorPool;
    }

    public PoseEstimatorPool getPoseEstimatorPool() {
        return poseEstimatorPool;
    }

    private boolean fromFactory = false;

    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            FaceAttributeModelFactory.removeFromCache(config.getModelEnum());
        }
        if(Objects.nonNull(faceDetectorPool)){
            faceDetectorPool.close();
        }
        if(Objects.nonNull(genderPredictorPool)){
            genderPredictorPool.close();
        }
        if(Objects.nonNull(faceLandmarkerPool)){
            faceLandmarkerPool.close();
        }
        if(Objects.nonNull(agePredictorPool)){
            agePredictorPool.close();
        }
        if(Objects.nonNull(eyeStateDetectorPool)){
            eyeStateDetectorPool.close();
        }
        if(Objects.nonNull(maskDetectorPool)){
            maskDetectorPool.close();
        }
        if(Objects.nonNull(poseEstimatorPool)){
            poseEstimatorPool.close();
        }
    }
}
