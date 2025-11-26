package cn.smartjavaai.ocr.model.table;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.TableStructureConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.entity.TableStructureResult;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.factory.PlateModelFactory;
import cn.smartjavaai.ocr.factory.TableRecModelFactory;
import cn.smartjavaai.ocr.model.table.criteria.StructureCriteriaFactory;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * 表格结构模型
 * @author dwj
 */
@Slf4j
public class CommonTableStructureModel implements TableStructureModel{

    private ZooModel<Image, TableStructureResult> model;

    private GenericObjectPool<Predictor<Image, TableStructureResult>> predictorPool;

    private TableStructureConfig config;

    @Override
    public void loadModel(TableStructureConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new OcrException("modelPath is null");
        }
        Criteria<Image, TableStructureResult> criteria = StructureCriteriaFactory.createCriteria(config);
        try{
            model = ModelZoo.loadModel(criteria);
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
            throw new OcrException("表格结构识别模型加载失败", e);
        }
    }

    @Override
    public R<TableStructureResult> detect(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromBufferedImage(image);
            return detect(img);
        } catch (Exception e) {
            throw new OcrException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public R<TableStructureResult> detect(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            return detect(img);
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public R<TableStructureResult> detect(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return detect(image);
        } catch (IOException e) {
            throw new OcrException("错误的图像", e);
        }
    }

    @Override
    public R<TableStructureResult> detect(Image image) {
        Predictor<Image, TableStructureResult> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            TableStructureResult result = predictor.predict(image);
            return R.ok(result);
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
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
    public GenericObjectPool<Predictor<Image, TableStructureResult>> getPool() {
        return predictorPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            TableRecModelFactory.removeFromCache(config.getModelEnum());
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
