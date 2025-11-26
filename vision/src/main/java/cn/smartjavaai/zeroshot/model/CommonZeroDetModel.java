package cn.smartjavaai.zeroshot.model;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.VisionLanguageInput;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.vision.utils.DetectedObjectsFilter;
import cn.smartjavaai.vision.utils.DetectorUtils;
import cn.smartjavaai.zeroshot.config.ZeroDetConfig;
import cn.smartjavaai.zeroshot.criteria.ZeroDetCriteriaFactory;
import cn.smartjavaai.zeroshot.exception.ZeroDetException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 零样本目标检测模型
 * @author dwj
 */
@Slf4j
public class CommonZeroDetModel implements ZeroDetModel {


    private ZeroDetConfig config;

    private ZooModel<VisionLanguageInput, DetectedObjects> model;

    private GenericObjectPool<Predictor<VisionLanguageInput, DetectedObjects>> predictorPool;

    @Override
    public void loadModel(ZeroDetConfig config) {
        if(Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型枚举");
        }
        Criteria<VisionLanguageInput, DetectedObjects> criteria = ZeroDetCriteriaFactory.createCriteria(config);
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

    @Override
    public R<DetectionResponse> detect(Image image, String[] candidates) {
        DetectedObjects detectedObjects = detectCore(new VisionLanguageInput(image, candidates));
        DetectionResponse detectionResponse = DetectorUtils.convertToDetectionResponse(detectedObjects, image);
        return R.ok(detectionResponse);
    }


    /**
     * 模型核心推理方法
     * @param input
     * @return
     */
    @Override
    public DetectedObjects detectCore(VisionLanguageInput input) {
        Predictor<VisionLanguageInput, DetectedObjects> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            DetectedObjects detectedObjects = predictor.predict(input);
            //过滤
            if(Objects.nonNull(detectedObjects) && detectedObjects.getNumberOfObjects() > 0){
                DetectedObjectsFilter detectedObjectsFilter = new DetectedObjectsFilter(null, config.getThreshold());
                detectedObjects = detectedObjectsFilter.filter(detectedObjects);
            }
            return detectedObjects;
        } catch (Exception e) {
            throw new DetectionException("零样本目标检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    predictorPool.returnObject(predictor); //归还
                    log.debug("释放资源");
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
    public R<DetectionResponse> detectAndDraw(Image image, String[] candidates) {
        DetectedObjects detectedObjects = detectCore(new VisionLanguageInput(image, candidates));
        image.drawBoundingBoxes(detectedObjects);
        DetectionResponse detectionResponse = DetectorUtils.convertToDetectionResponse(detectedObjects, image);
        detectionResponse.setDrawnImage(image);
        return R.ok(detectionResponse);
    }

    @Override
    public R<DetectionResponse> detectAndDraw(String[] candidates, String imagePath, String outputPath) {
        try {
            Image img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            DetectedObjects detectedObjects = detectCore(new VisionLanguageInput(img, candidates));
            img.drawBoundingBoxes(detectedObjects);
            img.save(Files.newOutputStream(Paths.get(outputPath)), "png");
            DetectionResponse detectionResponse = DetectorUtils.convertToDetectionResponse(detectedObjects, img);
            return R.ok(detectionResponse);
        } catch (IOException e) {
            throw new ZeroDetException(e);
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

    @Override
    public void close() throws Exception {
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
