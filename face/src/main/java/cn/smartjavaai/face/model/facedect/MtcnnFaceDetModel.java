package cn.smartjavaai.face.model.facedect;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.NoopTranslator;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.facedect.criterial.FaceDetCriteriaFactory;
import cn.smartjavaai.face.model.facedect.mtcnn.*;
import cn.smartjavaai.face.utils.FaceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * MTCNN 人脸检测模型实现
 * @author dwj
 */
@Slf4j
public class MtcnnFaceDetModel extends CommonFaceDetModel{


    public ZooModel<NDList, NDList> pNetModel;

    public ZooModel<NDList, NDList> rNetModel;

    public ZooModel<NDList, NDList> oNetModel;
    private GenericObjectPool<Predictor<NDList, NDList>> pnetPredictorPool;
    private GenericObjectPool<Predictor<NDList, NDList>> rnetPredictorPool;
    private GenericObjectPool<Predictor<NDList, NDList>> onetPredictorPool;

    private FaceDetConfig config;


    /**
     * 加载模型
     * @param config
     */
    @Override
    public void loadModel(FaceDetConfig config){
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        Path modelPath = Paths.get(config.getModelPath());
        if(!Files.isDirectory(modelPath)){
            throw new FaceException("MTCNN 模型需要指定存放模型文件的目录路径");
        }
        try {
            Device device = null;
            if(!Objects.isNull(config.getDevice())){
                device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
            }
            Path pnetPath = modelPath.resolve("pnet_script.pt");
            Path rnetPath = modelPath.resolve("rnet_script.pt");
            Path onetPath = modelPath.resolve("onet_script.pt");
            pNetModel = getModel(pnetPath, device);
            rNetModel = getModel(rnetPath, device);
            oNetModel = getModel(onetPath, device);

            this.pnetPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(pNetModel));
            this.rnetPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(rNetModel));
            this.onetPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(oNetModel));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            pnetPredictorPool.setMaxTotal(predictorPoolSize);
            rnetPredictorPool.setMaxTotal(predictorPoolSize);
            onetPredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + pNetModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new FaceException("mtcnn人脸检测模型加载失败", e);
        }
    }

    /**
     * 加载模型
     * @param modelPath
     * @throws ModelNotFoundException
     * @throws MalformedModelException
     * @throws IOException
     */
    public ZooModel<NDList, NDList> getModel(Path modelPath, Device device) throws ModelNotFoundException, MalformedModelException, IOException {
        Criteria<NDList, NDList> criteria =
                Criteria.builder()
                        .setTypes(NDList.class, NDList.class)
                        .optTranslator(new NoopTranslator())
                        .optEngine("PyTorch")
                        .optModelPath(modelPath)
                        .optProgress(new ProgressBar())
                        .optDevice(device)
                        .build();
        return criteria.loadModel();
    }


    /**
     * 人脸检测
     * @param image
     * @return
     */
    @Override
    public DetectedObjects detectCore(Image image){
        Predictor<NDList, NDList> pNetPredictor = null;
        Predictor<NDList, NDList> rNetPredictor = null;
        Predictor<NDList, NDList> oNetPredictor = null;
        try (NDManager manager = pNetModel.getNDManager().newSubManager();){
            pNetPredictor = pnetPredictorPool.borrowObject();
            rNetPredictor = rnetPredictorPool.borrowObject();
            oNetPredictor = onetPredictorPool.borrowObject();
            int h = image.getHeight();
            int w = image.getWidth();
            //第一阶段
            NDList outputPnet = PNetModel.firstStage(manager, pNetPredictor, image);

            if(CollectionUtils.isEmpty(outputPnet)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            NDArray boxes = outputPnet.get(0);
            NDArray image_inds = outputPnet.get(1);
            NDArray imgs = outputPnet.get(2);
            if(DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(image_inds) || DJLCommonUtils.isNDArrayEmpty(imgs)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            NDList pad = MtcnnUtils.pad(boxes, w, h);
            //第二阶段
            NDList outputRnet = RNetModel.secondStage(manager, rNetPredictor, imgs, boxes, pad, image_inds);
            if(CollectionUtils.isEmpty(outputRnet)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            NDArray image_indsFiltered = outputRnet.get(0);
            NDArray scoresFiltered = outputRnet.get(1);
            boxes = outputRnet.get(2);
            if(DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(image_indsFiltered) || DJLCommonUtils.isNDArrayEmpty(scoresFiltered)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            //第三阶段
            MtcnnBatchResult oNetResult = ONetModel.thirdStage(manager, oNetPredictor, imgs, boxes, w, h, scoresFiltered, image_indsFiltered);
            return FaceUtils.toDetectedObjects(oNetResult, w, h);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (pNetPredictor != null) {
                try {
                    pnetPredictorPool.returnObject(pNetPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        pNetPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
            if (rNetPredictor != null) {
                try {
                    rnetPredictorPool.returnObject(rNetPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        rNetPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
            if (oNetPredictor != null) {
                try {
                    onetPredictorPool.returnObject(oNetPredictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        oNetPredictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }


    /**
     * 使用MtcnnPredictors进行人脸检测
     * @param image
     * @param predictors
     * @return
     */
    public DetectedObjects detectCoreByPredictors(Image image, MtcnnPredictors predictors){
        Predictor<NDList, NDList> pNetPredictor = predictors.pNetPredictor;
        Predictor<NDList, NDList> rNetPredictor = predictors.rNetPredictor;
        Predictor<NDList, NDList> oNetPredictor = predictors.oNetPredictor;
        try (NDManager manager = pNetModel.getNDManager().newSubManager();){
            int h = image.getHeight();
            int w = image.getWidth();
            //第一阶段
            NDList outputPnet = PNetModel.firstStage(manager, pNetPredictor, image);

            if(CollectionUtils.isEmpty(outputPnet)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            NDArray boxes = outputPnet.get(0);
            NDArray image_inds = outputPnet.get(1);
            NDArray imgs = outputPnet.get(2);
            if(DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(image_inds) || DJLCommonUtils.isNDArrayEmpty(imgs)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            NDList pad = MtcnnUtils.pad(boxes, w, h);
            //第二阶段
            NDList outputRnet = RNetModel.secondStage(manager, rNetPredictor, imgs, boxes, pad, image_inds);
            if(CollectionUtils.isEmpty(outputRnet)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            NDArray image_indsFiltered = outputRnet.get(0);
            NDArray scoresFiltered = outputRnet.get(1);
            boxes = outputRnet.get(2);
            if(DJLCommonUtils.isNDArrayEmpty(boxes) || DJLCommonUtils.isNDArrayEmpty(image_indsFiltered) || DJLCommonUtils.isNDArrayEmpty(scoresFiltered)){
                return DJLCommonUtils.buildEmptyDetectedObjects();
            }
            //第三阶段
            MtcnnBatchResult oNetResult = ONetModel.thirdStage(manager, oNetPredictor, imgs, boxes, w, h, scoresFiltered, image_indsFiltered);
            return FaceUtils.toDetectedObjects(oNetResult, w, h);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    public GenericObjectPool<Predictor<NDList, NDList>> getPnetPredictorPool() {
        return pnetPredictorPool;
    }

    public GenericObjectPool<Predictor<NDList, NDList>> getRnetPredictorPool() {
        return rnetPredictorPool;
    }

    public GenericObjectPool<Predictor<NDList, NDList>> getOnetPredictorPool() {
        return onetPredictorPool;
    }


    private boolean fromFactory = false;

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
    public boolean isFromFactory() {
        return fromFactory;
    }


    public MtcnnPredictors borrowPredictors() throws Exception {
        if(pnetPredictorPool == null || rnetPredictorPool == null || onetPredictorPool == null){
            return null;
        }
        Predictor<NDList, NDList> p = pnetPredictorPool.borrowObject();
        Predictor<NDList, NDList> r = rnetPredictorPool.borrowObject();
        Predictor<NDList, NDList> o = onetPredictorPool.borrowObject();
        return new MtcnnPredictors(p, r, o, this);
    }

    public void returnPredictor(Predictor<NDList, NDList> pNetPredictor, Predictor<NDList, NDList> rNetPredictor, Predictor<NDList, NDList> oNetPredictor) {
        if (pNetPredictor != null) {
            try {
                pnetPredictorPool.returnObject(pNetPredictor); //归还
            } catch (Exception e) {
                log.warn("归还Predictor失败", e);
                try {
                    pNetPredictor.close(); // 归还失败才销毁
                } catch (Exception ex) {
                    log.error("关闭Predictor失败", ex);
                }
            }
        }
        if (rNetPredictor != null) {
            try {
                rnetPredictorPool.returnObject(rNetPredictor); //归还
            } catch (Exception e) {
                log.warn("归还Predictor失败", e);
                try {
                    rNetPredictor.close(); // 归还失败才销毁
                } catch (Exception ex) {
                    log.error("关闭Predictor失败", ex);
                }
            }
        }
        if (oNetPredictor != null) {
            try {
                onetPredictorPool.returnObject(oNetPredictor); //归还
            } catch (Exception e) {
                log.warn("归还Predictor失败", e);
                try {
                    oNetPredictor.close(); // 归还失败才销毁
                } catch (Exception ex) {
                    log.error("关闭Predictor失败", ex);
                }
            }
        }
    }


    @Override
    public void close() {
        if (fromFactory) {
            FaceDetModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (pnetPredictorPool != null) {
                pnetPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (rnetPredictorPool != null) {
                rnetPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (onetPredictorPool != null) {
                onetPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (pNetModel != null) {
                pNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (pNetModel != null) {
                pNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (rNetModel != null) {
                rNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (oNetModel != null) {
                oNetModel.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
    }
}
