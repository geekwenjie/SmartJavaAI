package cn.smartjavaai.action.model;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.hutool.core.img.ImgUtil;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.criteria.ActionRecCriteriaFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.Base64ImageUtils;
import cn.smartjavaai.objectdetection.criteria.CriteriaBuilderFactory;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.vision.utils.ClassificationFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

/**
 * 通用人类动作识别模型
 * @author dwj
 */
@Slf4j
public class CommonActionRecModel implements ActionRecModel{


    private ActionRecModelConfig config;

    private ZooModel<Image, Classifications> model;

    private GenericObjectPool<Predictor<Image, Classifications>> predictorPool;

    @Override
    public void loadModel(ActionRecModelConfig config) {
        if(Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型枚举");
        }
        Criteria<Image, Classifications> criteria = ActionRecCriteriaFactory.createCriteria(config);
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
    public R<Classifications> detect(Image image) {
        Classifications classifications = detectCore(image);
        // 过滤
        if(Objects.nonNull(classifications) && !classifications.items().isEmpty()){
            classifications = new ClassificationFilter(config.getAllowedClasses(), config.getThreshold()).filter(classifications);
        }
        return R.ok(classifications);
    }

    /**
     * 模型核心推理方法
     * @param image
     * @return
     */
    public Classifications detectCore(Image image) {
        Predictor<Image, Classifications> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new DetectionException("动作识别错误", e);
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
    public void close() throws Exception {
        if (fromFactory) {
            ActionRecModelFactory.removeFromCache(config.getModelEnum());
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
