package cn.smartjavaai.objectdetection.model;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.criteria.CriteriaBuilderFactory;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.vision.utils.DetectedObjectsFilter;
import cn.smartjavaai.vision.utils.DetectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 目标检测模型
 * @author dwj
 */
@Slf4j
public class DetectorModel implements AutoCloseable{

    private ZooModel<Image, DetectedObjects> model;

    private GenericObjectPool<Predictor<Image, DetectedObjects>> predictorPool;

    private DetectorModelConfig config;

    private boolean fromFactory = false;

    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }

    public void loadModel(DetectorModelConfig config){
        if(Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型枚举");
        }
        Criteria<Image, DetectedObjects> criteria = CriteriaBuilderFactory.createCriteria(config);
        this.config = config;
        try {
            model = criteria.loadModel();
            // 创建池子：每个线程独享 Predictor
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
            throw new DetectionException("模型加载失败", e);
        }
    }

    /**
     * 目标检测
     * @param imagePath
     * @return
     * @throws Exception
     */
    @Deprecated
    public DetectionResponse detect(String imagePath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new DetectionException("图像文件不存在");
        }
        Image image = null;
        try {
            image = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detectCore(image);
            return DetectorUtils.convertToDetectionResponse(detectedObjects, image);
        } catch (Exception e) {
            throw new DetectionException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(image);
        }
    }


    /**
     * 目标检测-将检测结果绘制到原图
     * @param imagePath
     * @param outputPath
     */
    public void detectAndDraw(String imagePath, String outputPath){
        if(!FileUtils.isFileExists(imagePath)){
            throw new DetectionException("图像文件不存在");
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detectCore(img);
            if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
                throw new DetectionException("未检测到图片中的物体");
            }
            img.drawBoundingBoxes(detectedObjects);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 调用 save 方法将 Image 写入字节流
            img.save(new FileOutputStream(Paths.get(outputPath).toAbsolutePath().toString()), "png");
        } catch (IOException e) {
            throw new DetectionException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    /**
     * 目标检测
     * @param imageData
     * @return
     */
    @Deprecated
    public DetectionResponse detect(byte[] imageData){
        if(Objects.isNull(imageData)){
            throw new DetectionException("图像无效");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return detect(image);
        } catch (IOException e) {
            throw new DetectionException("错误的图像", e);
        }

    }


    /**
     * 目标检测
     * @param image
     * @return
     */
    @Deprecated
    public DetectionResponse detect(BufferedImage image){
        if(!BufferedImageUtils.isImageValid(image)){
            throw new DetectionException("图像无效");
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromBufferedImage(image);
            DetectedObjects detectedObjects = detectCore(img);
            return DetectorUtils.convertToDetectionResponse(detectedObjects, img);
        } catch (Exception e) {
            throw new DetectionException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }

    }

    /**
     * 目标检测-将检测结果绘制到原图
     * @param sourceImage
     * @return
     */
    @Deprecated
    public BufferedImage detectAndDraw(BufferedImage sourceImage){
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            throw new DetectionException("图像无效");
        }
        Image img = SmartImageFactory.getInstance().fromBufferedImage(sourceImage);
        DetectedObjects detectedObjects = detectCore(img);
        if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            throw new DetectionException("未检测到图片中的物体");
        }
        img.drawBoundingBoxes(detectedObjects);
        BufferedImage drawnImage = ImageUtils.toBufferedImage(img);
        ImageUtils.releaseOpenCVMat(img);
        return drawnImage;
    }

    /**
     * 目标检测
     * @param image
     * @return
     */
    public DetectionResponse detect(Image image){
        DetectedObjects detectedObjects = detectCore(image);
        return DetectorUtils.convertToDetectionResponse(detectedObjects, image);
    }


    /**
     * 检测并绘制
     * @param image
     * @return
     */
    public DetectionResponse detectAndDraw(Image image){
        DetectedObjects detectedObjects = detectCore(image);
        if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            throw new DetectionException("未检测到图片中的物体");
        }
        Image img = ImageUtils.copy(image);
        DetectionResponse detectionResponse = DetectorUtils.convertToDetectionResponse(detectedObjects, img);
        img.drawBoundingBoxes(detectedObjects);
        detectionResponse.setDrawnImage(img);
        return detectionResponse;
    }

    /**
     * 目标检测
     * @param image
     * @return
     */
    public DetectedObjects detectCore(Image image){
        Predictor<Image, DetectedObjects> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            DetectedObjects detectedObjects = predictor.predict(image);
            if(CollectionUtils.isNotEmpty(config.getAllowedClasses())
                    && Objects.nonNull(detectedObjects) && detectedObjects.getNumberOfObjects() > 0){
                DetectedObjectsFilter detectedObjectsFilter = new DetectedObjectsFilter(config.getAllowedClasses(), config.getThreshold(),config.getTopK());
                detectedObjects = detectedObjectsFilter.filter(detectedObjects);
            }
            return detectedObjects;
        } catch (Exception e) {
            throw new DetectionException("目标检测错误", e);
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


    public GenericObjectPool<Predictor<Image, DetectedObjects>> getPool() {
        return predictorPool;
    }


    /**
     * 显式释放资源
     */
    @Override
    public void close() {
        if (fromFactory) {
            ObjectDetectionModelFactory.removeFromCache(config.getModelEnum());
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
