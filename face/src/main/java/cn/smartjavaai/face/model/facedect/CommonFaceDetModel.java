package cn.smartjavaai.face.model.facedect;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.ExpressionModelFactory;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.facedect.criterial.FaceDetCriteriaFactory;
import cn.smartjavaai.face.utils.FaceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * DJL通用人脸检测模型实现
 * @author dwj
 */
@Slf4j
public class CommonFaceDetModel implements FaceDetModel{

    private GenericObjectPool<Predictor<Image, DetectedObjects>> predictorPool;

    private ZooModel<Image, DetectedObjects> model;

    private FaceDetConfig config;


    /**
     * 加载模型
     * @param config
     */
    @Override
    public void loadModel(FaceDetConfig config){
        Criteria<Image, DetectedObjects> criteria = FaceDetCriteriaFactory.createCriteria(config);
        try {
            this.config = config;
            model = criteria.loadModel();
            this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            predictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + model.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("人脸检测模型加载失败", e);
        }
    }

    @Override
    public R<DetectionResponse> detect(Image image) {
        DetectedObjects detection = detectCore(image);
        return R.ok(FaceUtils.convertToDetectionResponse(detection, image));
    }


    @Override
    public R<DetectionResponse> detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detection = detectCore(img);
            DetectionResponse detectionResponse = FaceUtils.convertToDetectionResponse(detection, img);
            if(detectionResponse == null){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            return R.ok(detectionResponse);
        } catch (IOException e) {
            throw new FaceException("无效的图片", e);
        } finally {
            if (img != null && img.getWrappedImage() instanceof Mat) {
                ((Mat)img.getWrappedImage()).release();
            }
        }

    }

    /**
     * 检测人脸
     * @param imageInputStream 图片流
     * @return
     * @throws Exception
     */
    @Override
    public R<DetectionResponse> detect(InputStream imageInputStream){
        if(Objects.isNull(imageInputStream)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromInputStream(imageInputStream);
            DetectedObjects detection = detectCore(img);
            DetectionResponse detectionResponse = FaceUtils.convertToDetectionResponse(detection,img);
            if(detectionResponse == null){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            return R.ok(detectionResponse);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        } finally {
            if (img != null && img.getWrappedImage() instanceof Mat) {
                ((Mat)img.getWrappedImage()).release();
            }
        }

    }

    @Override
    public R<DetectionResponse> detect(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromBufferedImage(image);
            DetectedObjects detection = detectCore(img);
            DetectionResponse detectionResponse = FaceUtils.convertToDetectionResponse(detection,img);
            if(detectionResponse == null){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            return R.ok(detectionResponse);
        } catch (Exception e) {
            throw new FaceException(e);
        } finally {
            if (img != null && img.getWrappedImage() instanceof Mat) {
                ((Mat)img.getWrappedImage()).release();
            }
        }

    }

    @Override
    public R<DetectionResponse> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        return detect(new ByteArrayInputStream(imageData));
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
    public R<DetectionResponse> detectAndDraw(Image image) {
        DetectedObjects detectedObjects = detectCore(image);
        if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image drawnImage = ImageUtils.copy(image);
        drawnImage.drawBoundingBoxes(detectedObjects);
        DetectionResponse detectionResponse = FaceUtils.convertToDetectionResponse(detectedObjects, drawnImage);
        detectionResponse.setDrawnImage(drawnImage);
        return R.ok(detectionResponse);
    }

    @Override
    public R<DetectionResponse> detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detectCore(img);
            if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            img.drawBoundingBoxes(detectedObjects);
            Path output = Paths.get(outputPath);
            log.debug("Saving to {}", output.toAbsolutePath().toString());
            img.save(Files.newOutputStream(output), "png");
            DetectionResponse detectionResponse = FaceUtils.convertToDetectionResponse(detectedObjects,img);
            if(detectionResponse == null){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            return R.ok(detectionResponse);
        } catch (IOException e) {
            throw new FaceException(e);
        } finally {
            if (img != null && img.getWrappedImage() instanceof Mat) {
                ((Mat)img.getWrappedImage()).release();
            }
        }
    }

    @Override
    public R<BufferedImage> detectAndDraw(BufferedImage sourceImage) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = SmartImageFactory.getInstance().fromBufferedImage(sourceImage);
        DetectedObjects detectedObjects = detectCore(img);
        if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        img.drawBoundingBoxes(detectedObjects);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(outputStream, "png");
            // 将字节流转换为 BufferedImage
            byte[] imageBytes = outputStream.toByteArray();
            return R.ok(ImageIO.read(new ByteArrayInputStream(imageBytes)));
        } catch (IOException e) {
            throw new FaceException("导出图片失败", e);
        } finally {
            if (img != null && img.getWrappedImage() instanceof Mat) {
                ((Mat)img.getWrappedImage()).release();
            }
        }
    }



    /**
     * 人脸检测
     * @param image
     * @return
     */
    @Override
    public DetectedObjects detectCore(Image image){
        Predictor<Image, DetectedObjects> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new FaceException("人脸检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    predictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }


    @Override
    public GenericObjectPool<Predictor<Image, DetectedObjects>> getPool() {
        return predictorPool;
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
    public void close() {
        if (fromFactory) {
            FaceDetModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (predictorPool != null) {
                predictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }
}
