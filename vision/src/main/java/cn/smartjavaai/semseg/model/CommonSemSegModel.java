package cn.smartjavaai.semseg.model;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.CategoryMask;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.instanceseg.exception.InstanceSegException;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.pose.model.PoseDetModelFactory;
import cn.smartjavaai.semseg.config.SemSegModelConfig;
import cn.smartjavaai.semseg.criteria.SemSegCriteriaFactory;
import cn.smartjavaai.vision.utils.CategoryMaskFilter;
import cn.smartjavaai.vision.utils.DetectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 语义分割模型
 * @author dwj
 */
@Slf4j
public class CommonSemSegModel implements SemSegModel {


    private SemSegModelConfig config;

    private ZooModel<Image, CategoryMask> model;

    private GenericObjectPool<Predictor<Image, CategoryMask>> predictorPool;

    @Override
    public void loadModel(SemSegModelConfig config) {
        if(Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型枚举");
        }
        Criteria<Image, CategoryMask> criteria = SemSegCriteriaFactory.createCriteria(config);
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
    public R<CategoryMask> detect(Image image) {
        CategoryMask categoryMask = detectCore(image);
        // 过滤
        if(CollectionUtils.isNotEmpty(config.getAllowedClasses())
                && Objects.nonNull(categoryMask) && CollectionUtils.isNotEmpty(categoryMask.getClasses())){
            categoryMask = new CategoryMaskFilter(config.getAllowedClasses()).filter(categoryMask);
        }
        return R.ok(categoryMask);
    }

    /**
     * 模型核心推理方法
     * @param image
     * @return
     */
    public CategoryMask detectCore(Image image) {
        Predictor<Image, CategoryMask> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new DetectionException("语义分割错误", e);
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
    public R<CategoryMask> detectAndDraw(String imagePath, String outputPath) {
        try {
            Image img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            CategoryMask categoryMask = detectCore(img);
            if(Objects.isNull(categoryMask) || CollectionUtils.isEmpty(categoryMask.getClasses())){
                throw new InstanceSegException("未检测到实例");
            }
            ImageUtils.drawMask(categoryMask, img, 180, 0);
            img.save(Files.newOutputStream(Paths.get(outputPath)), "png");
            ImageUtils.releaseOpenCVMat(img);
            return R.ok(categoryMask);
        } catch (IOException e) {
            throw new InstanceSegException(e);
        }
    }

    @Override
    public Image detectAndDraw(Image image) {
        CategoryMask categoryMask = detectCore(image);
        if(Objects.isNull(categoryMask) || CollectionUtils.isEmpty(categoryMask.getClasses())){
            throw new InstanceSegException("未检测到实例");
        }
        Image drawnImage = ImageUtils.copy(image);
        ImageUtils.drawMask(categoryMask, drawnImage, 180, 0);
        return drawnImage;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            SemSegModelFactory.removeFromCache(config.getModelEnum());
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

    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }
}
