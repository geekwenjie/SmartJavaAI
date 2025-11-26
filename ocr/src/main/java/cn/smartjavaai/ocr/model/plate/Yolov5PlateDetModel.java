package cn.smartjavaai.ocr.model.plate;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.PlateInfo;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.factory.PlateModelFactory;
import cn.smartjavaai.ocr.model.common.detect.criteria.OcrCommonDetCriterialFactory;
import cn.smartjavaai.ocr.model.plate.criteria.PlateDetCriterialFactory;
import cn.smartjavaai.ocr.utils.OcrUtils;
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
import java.util.List;
import java.util.Objects;

/**
 * Yolov5 车牌检测模型
 * @author dwj
 * @date 2025/7/23
 */
@Slf4j
public class Yolov5PlateDetModel implements PlateDetModel{

    private GenericObjectPool<Predictor<Image, DetectedObjects>> detPredictorPool;

    private ZooModel<Image, DetectedObjects> detectionModel;

    private PlateDetModelConfig config;

    @Override
    public void loadModel(PlateDetModelConfig config) {
        if(StringUtils.isBlank(config.getModelPath())){
            throw new OcrException("modelPath is null");
        }
        this.config = config;
        //初始化 检测Criteria
        Criteria<Image, DetectedObjects> detCriteria = PlateDetCriterialFactory.createCriteria(config);
        try{
            detectionModel = ModelZoo.loadModel(detCriteria);
            // 创建池子：每个线程独享 Predictor
            this.detPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(detectionModel));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            detPredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + detectionModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("检测模型加载失败", e);
        }
    }

    @Override
    public R<List<PlateInfo>> detect(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            R<List<PlateInfo>> plateInfoList = detect(img);
            return plateInfoList;
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }

    }

    @Override
    public R<List<PlateInfo>> detectBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return detect(imageData);
    }

    @Override
    public R<List<PlateInfo>> detect(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = SmartImageFactory.getInstance().fromBufferedImage(image);
        R<List<PlateInfo>> plateInfoList = detect(img);
        ImageUtils.releaseOpenCVMat(img);
        return plateInfoList;
    }

    @Override
    public R<List<PlateInfo>> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        return detect(new ByteArrayInputStream(imageData));
    }

    @Override
    public DetectedObjects detectCore(Image image) {
        Predictor<Image, DetectedObjects> predictor = null;
        try {
            predictor = detPredictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new OcrException("车牌检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    detPredictorPool.returnObject(predictor); //归还
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
    public R<List<PlateInfo>> detect(InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromInputStream(inputStream);
            R<List<PlateInfo>> plateInfoList = detect(img);
            return plateInfoList;
        } catch (IOException e) {
            throw new OcrException("无效图片输入流", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public R<Void> detectAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        try {
            Image img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detectCore(img);
            if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
                return R.fail(R.Status.NO_FACE_DETECTED);
            }
            img.drawBoundingBoxes(detectedObjects);
            Path output = Paths.get(outputPath);
            log.debug("Saving to {}", output.toAbsolutePath().toString());
            img.save(Files.newOutputStream(output), "png");
            return R.ok();
        } catch (IOException e) {
            throw new OcrException(e);
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
            throw new OcrException("导出图片失败", e);
        }
    }

    @Override
    public R<List<PlateInfo>> detect(Image image) {
        DetectedObjects detectedObjects = detectCore(image);
        if (Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            return R.fail(R.Status.NO_OBJECT_DETECTED);
        }
        List<PlateInfo> plateInfoList = OcrUtils.convertToPlateInfo(detectedObjects, image);
        return R.ok(plateInfoList);
    }

    @Override
    public Image detectAndDraw(Image image) {
        DetectedObjects detectedObjects = detectCore(image);
        if (Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            throw new OcrException("未检测到车牌");
        }
        Image img = ImageUtils.copy(image);
        img.drawBoundingBoxes(detectedObjects);
        return img;
    }

    @Override
    public GenericObjectPool<Predictor<Image, DetectedObjects>> getPool() {
        return detPredictorPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            PlateModelFactory.removeDetModelFromCache(config.getModelEnum());
        }
        try {
            if (detPredictorPool != null) {
                detPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (detectionModel != null) {
                detectionModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
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
