package cn.smartjavaai.face.model.facedect;

import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.facedect.mtcnn.MtcnnPredictors;
import cn.smartjavaai.face.seetaface.NativeLoader;
import cn.smartjavaai.face.seetaface.SeetaFace6FaceDetPredictors;
import cn.smartjavaai.face.utils.FaceUtils;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SeetaFace6 人脸检测模型
 * @author dwj
 */
@Slf4j
public class SeetaFace6FaceDetModel implements FaceDetModel{
    private FaceDetConfig config;

    private FaceDetectorPool faceDetectorPool;
    private FaceLandmarkerPool faceLandmarkerPool;

    /**
     * 阈值
     */
    private static final double THRESHOLD = 0.9d;


    @Override
    public void loadModel(FaceDetConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.debug("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
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

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);

            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            faceDetectorPool.setMaxTotal(predictorPoolSize);
            faceLandmarkerPool.setMaxTotal(predictorPoolSize);
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }

    }

    @Override
    public R<DetectionResponse> detect(Image image) {
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector predictor = null;
        FaceLandmarker faceLandmarker = null;
        try {
            predictor = faceDetectorPool.borrowObject();
            predictor.set(FaceDetector.Property.PROPERTY_THRESHOLD, config.getConfidenceThreshold() > 0 ? config.getConfidenceThreshold() : THRESHOLD);
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaRect[] seetaResult = predictor.Detect(imageData);
            List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
            for(SeetaRect seetaRect : seetaResult){
                //提取人脸的5点人脸标识
                SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, pointFS);
                seetaPointFSList.add(pointFS);
            }
            return R.ok(FaceUtils.convertToDetectionResponse(seetaResult, seetaPointFSList));
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    faceDetectorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    public DetectionResponse detectByPredictors(Image image, SeetaFace6FaceDetPredictors predictors) {
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector predictor = predictors.faceDetector;
        FaceLandmarker faceLandmarker = predictors.faceLandmarker;
        try {
            SeetaRect[] seetaResult = predictor.Detect(imageData);
            List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
            for(SeetaRect seetaRect : seetaResult){
                //提取人脸的5点人脸标识
                SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, pointFS);
                seetaPointFSList.add(pointFS);
            }
            return FaceUtils.convertToDetectionResponse(seetaResult, seetaPointFSList);
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }
    }


    @Override
    public R<DetectionResponse> detectAndDraw(Image image) {
        R<DetectionResponse> result = detect(image);
        if(result.getCode() != R.Status.SUCCESS.getCode()){
            return R.fail(result.getCode(), result.getMessage());
        }
        if(Objects.isNull(result.getData()) || Objects.isNull(result.getData().getDetectionInfoList()) || result.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image drawnImage = ImageUtils.drawBoundingBoxes(image, result.getData());
        result.getData().setDrawnImage(drawnImage);
        return result;
    }

    @Override
    public R<DetectionResponse> detect(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
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
    public R<DetectionResponse> detect(InputStream imageInputStream) {
        if(Objects.isNull(imageInputStream)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageInputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return detect(image);
    }

    @Override
    public R<DetectionResponse> detect(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = BufferedImageUtils.getMatrixBGR(image);
        FaceDetector predictor = null;
        FaceLandmarker faceLandmarker = null;
        try {
            predictor = faceDetectorPool.borrowObject();
            predictor.set(FaceDetector.Property.PROPERTY_THRESHOLD, config.getConfidenceThreshold() > 0 ? config.getConfidenceThreshold() : THRESHOLD);
            faceLandmarker = faceLandmarkerPool.borrowObject();
            SeetaRect[] seetaResult = predictor.Detect(imageData);
            List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
            for(SeetaRect seetaRect : seetaResult){
                //提取人脸的5点人脸标识
                SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaRect, pointFS);
                seetaPointFSList.add(pointFS);
            }
            return R.ok(FaceUtils.convertToDetectionResponse(seetaResult, seetaPointFSList));
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    faceDetectorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<DetectionResponse> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return detect(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<DetectionResponse> detectBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData);
    }

    @Override
    public R<DetectionResponse> detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image image = null;
        Image drawImage = null;
        try {
            image = SmartImageFactory.getInstance().fromFile(imagePath);
            R<DetectionResponse> result = detect(image);
            if(result.getCode() != R.Status.SUCCESS.getCode()){
                return R.fail(result.getCode(), result.getMessage());
            }
            if(Objects.isNull(result.getData()) || Objects.isNull(result.getData().getDetectionInfoList()) || result.getData().getDetectionInfoList().isEmpty()){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            //绘制人脸框
            drawImage = ImageUtils.drawBoundingBoxes(image, result.getData());
            ImageUtils.save(drawImage, Paths.get(outputPath), "png");
            return result;
        } catch (IOException e) {
            throw new FaceException("保存图片失败", e);
        } finally {
            ImageUtils.releaseOpenCVMat(image);
            ImageUtils.releaseOpenCVMat(drawImage);
        }
    }

    @Override
    public R<BufferedImage> detectAndDraw(BufferedImage sourceImage) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        R<DetectionResponse> result = detect(sourceImage);
        if(result.getCode() != R.Status.SUCCESS.getCode()){
            return R.fail(result.getCode(), result.getMessage());
        }
        if(Objects.isNull(result.getData()) || Objects.isNull(result.getData().getDetectionInfoList()) || result.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        //绘制人脸框
        BufferedImage drawnImage = BufferedImageUtils.copyBufferedImage(sourceImage);
        BufferedImageUtils.drawBoundingBoxes(drawnImage, result.getData());
        return R.ok(drawnImage);
    }

    public SeetaFace6FaceDetPredictors borrowPredictors() throws Exception {
        if(faceDetectorPool == null || faceLandmarkerPool == null){
            return null;
        }
        FaceDetector predictor = faceDetectorPool.borrowObject();
        predictor.set(FaceDetector.Property.PROPERTY_THRESHOLD, config.getConfidenceThreshold() > 0 ? config.getConfidenceThreshold() : THRESHOLD);
        FaceLandmarker faceLandmarker = faceLandmarkerPool.borrowObject();
        return new SeetaFace6FaceDetPredictors(predictor, faceLandmarker, this);
    }

    public void returnPredictor(FaceDetector predictor, FaceLandmarker faceLandmarker) {
        if (predictor != null) {
            try {
                faceDetectorPool.returnObject(predictor); //归还
            } catch (Exception e) {
                log.warn("归还Predictor失败", e);
            }
        }
        if (faceLandmarker != null) {
            try {
                faceLandmarkerPool.returnObject(faceLandmarker); //归还
            } catch (Exception e) {
                log.warn("归还Predictor失败", e);
            }
        }
    }




    public FaceDetectorPool getFaceDetectorPool() {
        return faceDetectorPool;
    }

    public FaceLandmarkerPool getFaceLandmarkerPool() {
        return faceLandmarkerPool;
    }


    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }


    @Override
    public void close() throws Exception {
        if (fromFactory) {
            FaceDetModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (faceDetectorPool != null) {
                faceDetectorPool.close();
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

}
