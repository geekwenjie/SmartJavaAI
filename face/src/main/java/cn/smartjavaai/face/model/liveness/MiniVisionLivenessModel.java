package cn.smartjavaai.face.model.liveness;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.preprocess.BufferedImagePreprocessor;
import cn.smartjavaai.common.preprocess.DJLImagePreprocessor;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.constant.MiniVisionConstant;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.LivenessModelFactory;
import cn.smartjavaai.face.model.liveness.translator.MiniVisionTranslator;
import com.seeta.sdk.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.*;

/**
 * 小视科技 活体检测模型
 * @author dwj
 * @date 2025/6/27
 */
@Slf4j
public class MiniVisionLivenessModel extends CommonLivenessModel{

    /**
     * 个性化参数：seModelPath
     */
    private static final String SE_MODEL_PATH_KEY = "seModelPath";

    private GenericObjectPool<Predictor<Image, float[]>> predictorPool;

    private GenericObjectPool<Predictor<Image, float[]>> sePredictorPool;


    /**
     * 模型策略
     */
    private ModelStrategy modelStrategy;

    private ZooModel<Image, float[]> model;

    private ZooModel<Image, float[]> seModel;

    @Override
    public void loadModel(LivenessConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        String seModelPath = config.getCustomParam(SE_MODEL_PATH_KEY, String.class);
        if(StringUtils.isBlank(config.getModelPath()) && StringUtils.isBlank(seModelPath)){
            throw new FaceException("modelPath 和 seModelPath 至少有一个不能为空");
        }
        this.config = config;
        //设置真人阈值
        Float realityThreshold = Objects.isNull(config.getRealityThreshold()) ? MiniVisionConstant.REALITY_THRESHOLD : config.getRealityThreshold();
        this.config.setRealityThreshold(realityThreshold);
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        if(StringUtils.isNotBlank(config.getModelPath()) && StringUtils.isBlank(seModelPath)){
            //2.7_80x80_MiniFASNetV2
            modelStrategy = ModelStrategy.MINIFASNET_V2;
        }else if (StringUtils.isBlank(config.getModelPath()) && StringUtils.isNotBlank(seModelPath)){
            //4_0_0_80x80_MiniFASNetV1SE
            modelStrategy = ModelStrategy.MINIFASNET_V1_SE;
        }else{
            //融合
            modelStrategy = ModelStrategy.FUSION;
        }

        if(modelStrategy == ModelStrategy.MINIFASNET_V2 || modelStrategy == ModelStrategy.FUSION){
            //初始化 检测Criteria
            Criteria<Image, float[]> criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, float[].class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optTranslator(new MiniVisionTranslator())
                            .optProgress(new ProgressBar())
                            .optDevice(device)
                            .build();
            try {
                model = criteria.loadModel();
                this.predictorPool = new GenericObjectPool<>(new PredictorFactory<>(model));
            } catch (IOException | ModelNotFoundException | MalformedModelException e) {
                throw new FaceException("MiniFASNetV2模型加载失败", e);
            }

        }

        if(modelStrategy == ModelStrategy.MINIFASNET_V1_SE || modelStrategy == ModelStrategy.FUSION){
            //初始化 检测Criteria
            Criteria<Image, float[]> seCriteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, float[].class)
                            .optModelPath(Paths.get(seModelPath))
                            .optTranslator(new MiniVisionTranslator())
                            .optProgress(new ProgressBar())
                            .optDevice(device)
                            .build();
            try {
                seModel = seCriteria.loadModel();
                this.sePredictorPool = new GenericObjectPool<>(new PredictorFactory<>(seModel));
            } catch (IOException | ModelNotFoundException | MalformedModelException e) {
                throw new FaceException("MiniFASNetV1SE模型加载失败", e);
            }
        }

        int predictorPoolSize = config.getPredictorPoolSize();
        if(config.getPredictorPoolSize() <= 0){
            predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
        }
        predictorPool.setMaxTotal(predictorPoolSize);
        sePredictorPool.setMaxTotal(predictorPoolSize);
        log.debug("当前设备: " + model.getNDManager().getDevice());
        log.debug("当前引擎: " + Engine.getInstance().getEngineName());
        log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
    }


    @Override
    public R<LivenessResult> detect(Image image, DetectionRectangle faceDetectionRectangle) {
        Predictor<Image, float[]> predictor = null;
        Predictor<Image, float[]> sePredictor = null;
        try {
            float[] result = null;
            float[] seResult = null;
            if(Objects.nonNull(predictorPool)){
                //预处理图片
                Image processedImage = new DJLImagePreprocessor(image, faceDetectionRectangle)
                        .setExtendRatio(2.7f)
                        .enableSquarePadding(true)
                        .enableScaling(true)
                        .setTargetSize(80)
                        .process();
                predictor = predictorPool.borrowObject();
                result = predictor.predict(processedImage);
                ImageUtils.releaseOpenCVMat(processedImage);
            }
            if(Objects.nonNull(sePredictorPool)){
                //预处理图片
                Image processedImage = new DJLImagePreprocessor(image, faceDetectionRectangle)
                        .setExtendRatio(4)
                        .enableSquarePadding(true)
                        .enableScaling(true)
                        .setTargetSize(80)
                        .process();
                sePredictor = sePredictorPool.borrowObject();
                seResult = sePredictor.predict(processedImage);
                ImageUtils.releaseOpenCVMat(processedImage);
            }
            if(Objects.isNull(result) && Objects.isNull(seResult)){
                throw new FaceException("活体检测错误");
            }
            //计算结果
            int maxIndex = ArrayUtils.sumAndFindMaxIndex(result, seResult, 3);
            BigDecimal score = Objects.isNull(result) ? BigDecimal.ZERO : BigDecimal.valueOf(result[maxIndex]);
            BigDecimal seScore = Objects.isNull(seResult) ? BigDecimal.ZERO : BigDecimal.valueOf(seResult[maxIndex]);
            BigDecimal avgSocre = score.add(seScore).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            //活体
            if(maxIndex == 1){
                LivenessStatus livenessStatus = avgSocre.floatValue() > config.getRealityThreshold() ? LivenessStatus.LIVE : LivenessStatus.NON_LIVE;
                return R.ok(new LivenessResult(livenessStatus, avgSocre.floatValue()));
            }else{//非活体
                return R.ok(new LivenessResult(LivenessStatus.NON_LIVE, BigDecimal.ONE.subtract(avgSocre).floatValue()));
            }
        } catch (Exception e) {
            throw new FaceException("活体检测错误", e);
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
            if (sePredictor != null) {
                try {
                    sePredictorPool.returnObject(sePredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        sePredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    public GenericObjectPool<Predictor<Image, float[]>> getPredictorPool() {
        return predictorPool;
    }

    public GenericObjectPool<Predictor<Image, float[]>> getSePredictorPool() {
        return sePredictorPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            LivenessModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (predictorPool != null) {
                predictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }

        try {
            if (sePredictorPool != null) {
                sePredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 sePredictorPool 失败", e);
        }
        try {
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (seModel != null) {
                seModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }

    /**
     * 模型策略
     */
    protected enum ModelStrategy {
        MINIFASNET_V2,
        MINIFASNET_V1_SE,
        FUSION // 融合模型
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
