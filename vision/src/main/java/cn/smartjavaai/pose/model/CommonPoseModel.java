package cn.smartjavaai.pose.model;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.Joints;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.obb.exception.ObbDetException;
import cn.smartjavaai.obb.model.ObbDetModelFactory;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;
import cn.smartjavaai.objectdetection.criteria.PersonDetCriteriaFactory;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.pose.config.PoseModelConfig;
import cn.smartjavaai.pose.criteria.PoseCriteriaFactory;
import cn.smartjavaai.vision.utils.DetectedObjectsFilter;
import cn.smartjavaai.vision.utils.DetectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 姿态估计模型
 * @author dwj
 */
@Slf4j
public class CommonPoseModel implements PoseModel {


    private PoseModelConfig config;

    private ZooModel<Image, Joints[]> model;

    private GenericObjectPool<Predictor<Image, Joints[]>> predictorPool;

    @Override
    public void loadModel(PoseModelConfig config) {
        if(Objects.isNull(config.getModelEnum())){
            throw new DetectionException("未配置模型枚举");
        }
        Criteria<Image, Joints[]> criteria = PoseCriteriaFactory.createCriteria(config);
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
     * 模型核心推理方法
     * @param image
     * @return
     */
    @Override
    public R<Joints[]> detect(Image image) {
        Predictor<Image, Joints[]> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            Joints[] joints = predictor.predict(image);
            return R.ok(joints);
        } catch (Exception e) {
            throw new DetectionException("行人检测错误", e);
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
    public Image detectAndDraw(Image image) {
        Image drawnImage = ImageUtils.copy(image);
        R<Joints[]> allJoints = detect(drawnImage);
        for (Joints joints : allJoints.getData()) {
            drawnImage.drawJoints(joints);
        }
        return drawnImage;
    }

    @Override
    public R<Joints[]> detectAndDraw(String imagePath, String outputPath) {
        try {
            Image img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            R<Joints[]> allJoints = detect(img);
            for (Joints joints : allJoints.getData()) {
                img.drawJoints(joints);
            }
            // 调用 save 方法将 Image 写入字节流
            img.save(Files.newOutputStream(Paths.get(outputPath)), "png");
            ImageUtils.releaseOpenCVMat(img);
            return allJoints;
        } catch (IOException e) {
            throw new ObbDetException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            PoseDetModelFactory.removeFromCache(config.getModelEnum());
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
