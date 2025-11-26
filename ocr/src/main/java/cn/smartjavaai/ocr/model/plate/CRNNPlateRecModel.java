package cn.smartjavaai.ocr.model.plate;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.lang.generator.UUIDGenerator;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.*;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.config.PlateRecModelConfig;
import cn.smartjavaai.ocr.entity.PlateInfo;
import cn.smartjavaai.ocr.entity.PlateResult;
import cn.smartjavaai.ocr.enums.PlateType;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.factory.PlateModelFactory;
import cn.smartjavaai.ocr.model.plate.criteria.PlateDetCriterialFactory;
import cn.smartjavaai.ocr.model.plate.criteria.PlateRecCriterialFactory;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author dwj
 */
@Slf4j
public class CRNNPlateRecModel implements PlateRecModel{


    private GenericObjectPool<Predictor<Image, PlateResult>> recPredictorPool;

    private ZooModel<Image, PlateResult> recModel;

    private PlateRecModelConfig config;

    @Override
    public void loadModel(PlateRecModelConfig config) {
        if(StringUtils.isBlank(config.getModelPath())){
            throw new OcrException("modelPath is null");
        }
        this.config = config;
        //初始化 检测Criteria
        Criteria<Image, PlateResult> detCriteria = PlateRecCriterialFactory.createCriteria(config);
        try{
            recModel = ModelZoo.loadModel(detCriteria);
            // 创建池子：每个线程独享 Predictor
            this.recPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(recModel));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            recPredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + recModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("检测模型加载失败", e);
        }
    }

    @Override
    public R<List<PlateInfo>> recognize(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            R<List<PlateInfo>> plateResult = recognize(img);
            return plateResult;
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public R<List<PlateInfo>> recognizeBase64(String base64Image) {
        if(StringUtils.isBlank(base64Image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        byte[] imageData = Base64ImageUtils.base64ToImage(base64Image);
        return recognize(imageData);
    }

    @Override
    public R<List<PlateInfo>> recognize(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = SmartImageFactory.getInstance().fromBufferedImage(image);
        R<List<PlateInfo>> plateResult = recognize(img);
        ImageUtils.releaseOpenCVMat(img);
        return plateResult;
    }

    @Override
    public R<List<PlateInfo>> recognize(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        return recognize(new ByteArrayInputStream(imageData));
    }

    @Override
    public R<List<PlateInfo>> recognize(Image image) {
        if(Objects.isNull(config.getPlateDetModel())){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "未指定车牌检测模型");
        }
        DetectedObjects detectedObjects = config.getPlateDetModel().detectCore(image);
        if(Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0){
            return R.fail(R.Status.NO_OBJECT_DETECTED);
        }
        List<PlateInfo> plateInfoList = OcrUtils.convertToPlateInfo(detectedObjects, image);
        Predictor<Image, PlateResult> predictor = null;
        try {
            predictor = recPredictorPool.borrowObject();
            for (PlateInfo plateInfo : plateInfoList){
                DetectionRectangle detectionRectangle = plateInfo.getDetectionRectangle();
//                Image subImage = image.getSubImage(detectionRectangle.getX(), detectionRectangle.getY(), detectionRectangle.getWidth(), detectionRectangle.getHeight());
                Mat imageMat = ImageUtils.toMat(image);
                //透视变换
                Mat subMat = OcrUtils.transformAndCropToMat(imageMat, plateInfo.getBox());
                //双层车牌
                if(plateInfo.getPlateType() == PlateType.DOUBLE){
                    subMat = getSplitMerge(subMat);
                }
                Image subImage = SmartImageFactory.getInstance().fromMat(subMat);
                PlateResult plateResult = predictor.predict(subImage);
                if(Objects.nonNull(plateResult)){
                    plateInfo.setPlateNumber(plateResult.getPlateNo());
                    plateInfo.setPlateColor(plateResult.getPlateColor());
                }
                ImageUtils.releaseOpenCVMat(subImage);
            }
           return R.ok(plateInfoList);
        } catch (Exception e) {
            throw new OcrException("车牌识别错误", e);
        }finally {

            if (predictor != null) {
                try {
                    recPredictorPool.returnObject(predictor); //归还
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

    /**
     * 双层车牌进行分割后识别
     * @param img
     * @return
     */
    private Mat getSplitMerge(Mat img) {
        int h = img.rows();
        int w = img.cols();

        // 上半部分：高度的前 5/12
        Rect upperRect = new Rect(0, 0, w, (int)(5.0 / 12 * h));
        Mat imgUpper = new Mat(img, upperRect);

        // 下半部分：高度从 1/3 开始
        Rect lowerRect = new Rect(0, (int)(1.0 / 3 * h), w, h - (int)(1.0 / 3 * h));
        Mat imgLower = new Mat(img, lowerRect);

        // 将上半部分 resize 到与下半部分相同大小
        Mat resizedUpper = new Mat();
        Size lowerSize = imgLower.size();
        Imgproc.resize(imgUpper, resizedUpper, lowerSize);

        // 水平拼接（将上下拼成左右）
        List<Mat> mergeList = new ArrayList<>();
        mergeList.add(resizedUpper);
        mergeList.add(imgLower);

        Mat merged = new Mat();
        Core.hconcat(mergeList, merged);
        return merged;
    }

    @Override
    public PlateResult recognizeCropped(Image image) {
        Predictor<Image, PlateResult> predictor = null;
        try {
            predictor = recPredictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new OcrException("车牌检测错误", e);
        }finally {
            if (predictor != null) {
                try {
                    recPredictorPool.returnObject(predictor); //归还
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
    public R<List<PlateInfo>> recognize(InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromInputStream(inputStream);
            return recognize(img);
        } catch (IOException e) {
            throw new OcrException("无效图片输入流", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public R<Void> recognizeAndDraw(String imagePath, String outputPath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            R<List<PlateInfo>> plateResult = recognize(img);
            if(!plateResult.isSuccess()){
                return R.fail(plateResult.getCode(), plateResult.getMessage());
            }
            if(CollectionUtils.isEmpty(plateResult.getData())){
                return R.fail(R.Status.NO_OBJECT_DETECTED);
            }
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(img);
            OcrUtils.drawPlateInfo(bufferedImage, plateResult.getData());
            ImageIO.write(bufferedImage, "png", new File(outputPath));
            return R.ok();
        } catch (IOException e) {
            throw new OcrException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public R<BufferedImage> recognizeAndDraw(BufferedImage sourceImage) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            R<List<PlateInfo>> plateResult = recognize(sourceImage);
            if(!plateResult.isSuccess()){
                return R.fail(plateResult.getCode(), plateResult.getMessage());
            }
            if(CollectionUtils.isEmpty(plateResult.getData())){
                return R.fail(R.Status.NO_OBJECT_DETECTED);
            }
            OcrUtils.drawPlateInfo(sourceImage, plateResult.getData());
            return R.ok(sourceImage);
        } catch (Exception e) {
            throw new OcrException("导出图片失败", e);
        }
    }

    @Override
    public R<Image> recognizeAndDraw(Image image) {
        try {
            R<List<PlateInfo>> plateResult = recognize(image);
            if(!plateResult.isSuccess()){
                return R.fail(plateResult.getCode(), plateResult.getMessage());
            }
            if(CollectionUtils.isEmpty(plateResult.getData())){
                return R.fail(R.Status.NO_OBJECT_DETECTED);
            }
            //opencv中文乱码，使用BufferedImage
            BufferedImage sourceImage = ImageUtils.toBufferedImage(image);
            OcrUtils.drawPlateInfo(sourceImage, plateResult.getData());
            Image drawImage = SmartImageFactory.getInstance().fromBufferedImage(sourceImage);
            return R.ok(drawImage);
        } catch (Exception e) {
            throw new OcrException("导出图片失败", e);
        }
    }

    @Override
    public GenericObjectPool<Predictor<Image, PlateResult>> getPool() {
        return recPredictorPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            PlateModelFactory.removeRecModelFromCache(config.getModelEnum());
        }
        try {
            if (recPredictorPool != null) {
                recPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (recModel != null) {
                recModel.close();
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
