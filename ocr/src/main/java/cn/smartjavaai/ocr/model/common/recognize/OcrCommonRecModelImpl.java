package cn.smartjavaai.ocr.model.common.recognize;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import cn.hutool.core.img.ImgUtil;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.*;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.criteria.OcrCommonRecCriterialFactory;
import cn.smartjavaai.ocr.utils.OcrUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PPOCRV5 识别模型
 *
 * @author dwj
 */
@Slf4j
public class OcrCommonRecModelImpl implements OcrCommonRecModel {

    private GenericObjectPool<Predictor<Image, String>> recPredictorPool;

    private OcrRecModelConfig config;

    private ZooModel<Image, String> recognitionModel;

    private OcrDirectionModel directionModel;

    private OcrCommonDetModel textDetModel;

    @Override
    public void loadModel(OcrRecModelConfig config) {
        if (StringUtils.isBlank(config.getRecModelPath())) {
            throw new OcrException("recModelPath is null");
        }
        this.config = config;
        this.directionModel = config.getDirectionModel();
        this.textDetModel = config.getTextDetModel();
        //初始化 识别Criteria
        Criteria<Image, String> recCriteria = OcrCommonRecCriterialFactory.createCriteria(config);
        try {
            recognitionModel = ModelZoo.loadModel(recCriteria);
            this.recPredictorPool = new GenericObjectPool<>(new PredictorFactory<>(recognitionModel));
            int predictorPoolSize = config.getPredictorPoolSize();
            if (config.getPredictorPoolSize() <= 0) {
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            recPredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + recognitionModel.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new OcrException("识别模型加载失败", e);
        }

    }


    @Override
    public OcrInfo recognize(String imagePath, OcrRecOptions options) {
        if (StringUtils.isBlank(config.getRecModelPath())) {
            throw new OcrException("recModelPath为空，无法识别");
        }
        if (!FileUtils.isFileExists(imagePath)) {
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            return recognize(img, options);
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    /**
     * @param image
     * @param options
     * @return
     */
    @Override
    public OcrInfo recognize(Image image, OcrRecOptions options) {
        List<OcrInfo> result = batchRecognizeDJLImage(Collections.singletonList(image), options);
        if (CollectionUtils.isEmpty(result)) {
            throw new OcrException("OCR识别结果为空");
        }
        return result.get(0);
    }


    /**
     * 批量矫正文本框
     *
     * @param boxList
     * @param srcMat
     * @param manager
     * @return
     */
    private List<Image> batchAlign(List<OcrBox> boxList, Mat srcMat, NDManager manager) {
        List<Image> imageList = new ArrayList<>(boxList.size());
        for (int i = 0; i < boxList.size(); i++) {
            //透视变换 + 裁剪
            Image subImg = OcrUtils.transformAndCrop(srcMat, boxList.get(i));
            //ImageUtils.saveImage(subImg, i + "crop.png", "build/output");
            //高宽比 > 1.5
            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                //旋转图片90度
                subImg = ImageUtils.rotateImg(manager, subImg);
                //ImageUtils.saveImage(subImg, i + "rotate.png", "build/output");
            }
            imageList.add(subImg);
        }
        return imageList;
    }

    /**
     * 批量矫正文本框
     *
     * @param itemList
     * @param srcMat
     * @param manager
     * @return
     */
    private List<Image> batchAlignWithDirection(List<OcrItem> itemList, Mat srcMat, NDManager manager) {
        List<Image> imageList = new ArrayList<>(itemList.size());
        for (OcrItem ocrItem : itemList) {
            //放射变换+裁剪
            Image subImage = OcrUtils.transformAndCrop(srcMat, ocrItem.getOcrBox());
            //ImageUtils.saveImage(subImage, UUID.randomUUID().toString() + "_aaa.png", "build/output");
            //纠正文本框
            subImage = OcrUtils.rotateImg(subImage, ocrItem.getAngle());
            imageList.add(subImage);
        }
        return imageList;
    }


//    private RotatedBox recognize(OcrBox box,Mat srcMat,Predictor<Image, String> recPredictor,NDManager manager){
//        try {
//            //透视变换 + 裁剪
//            Image subImg = OcrUtils.transformAndCrop(srcMat, box);
//            //ImageUtils.saveImage(subImg, i + "crop.png", "build/output");
//            //高宽比 > 1.5
//            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
//                //旋转图片90度
//                subImg = OcrUtils.rotateImg(manager, subImg);
//                //ImageUtils.saveImage(subImg, i + "rotate.png", "build/output");
//            }
//            String name = recPredictor.predict(subImg);
//            ((Mat)subImg.getWrappedImage()).release();
//            NDArray pointsArray = manager.create(box.toFloatArray());
//            return new RotatedBox(pointsArray, name);
//        } catch (Exception e) {
//            throw new OcrException("OCR检测错误", e);
//        }
//    }


    /**
     * 后处理：排序,分行
     *
     * @param rotatedBoxes
     */
    private OcrInfo postProcessOcrResult(List<RotatedBox> rotatedBoxes, OcrRecOptions ocrRecOptions) {
        //不分行
        if (!ocrRecOptions.isEnableLineSplit()) {
            return OcrUtils.convertRotatedBoxesToOcrItems(rotatedBoxes);
        }
        //Y坐标升序排序
        List<RotatedBox> initList = new ArrayList<>();
        for (RotatedBox result : rotatedBoxes) {
            initList.add(result);
        }
        Collections.sort(initList);
        //多行文本框的集合
        List<ArrayList<RotatedBoxCompX>> lines = new ArrayList<>();
        List<RotatedBoxCompX> line = new ArrayList<>();
        RotatedBoxCompX firstBox = new RotatedBoxCompX(initList.get(0).getBox(), initList.get(0).getText());
        line.add(firstBox);
        lines.add((ArrayList) line);
        //分行判断
        for (int i = 1; i < initList.size(); i++) {
            RotatedBoxCompX tmpBox = new RotatedBoxCompX(initList.get(i).getBox(), initList.get(i).getText());
            float y1 = firstBox.getBox().toFloatArray()[1];
            float y2 = tmpBox.getBox().toFloatArray()[1];
            float dis = Math.abs(y2 - y1);
            if (dis < 20) { // 认为是同 1 行  - Considered to be in the same line
                line.add(tmpBox);
            } else { // 换行 - Line break
                firstBox = tmpBox;
                Collections.sort(line);
                line = new ArrayList<>();
                line.add(firstBox);
                lines.add((ArrayList) line);
            }
        }
        return OcrUtils.convertToOcrInfo(lines);
    }


    @Override
    public void recognizeAndDraw(String imagePath, String outputPath, int fontSize, OcrRecOptions options) {
        if (!FileUtils.isFileExists(imagePath)) {
            throw new OcrException("图像文件不存在");
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            OcrInfo ocrInfo = recognize(img, options);
            if (Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()) {
                throw new OcrException("未检测到文字");
            }
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(img);
            OcrUtils.drawOcrResult(bufferedImage, ocrInfo, fontSize);
            BufferedImageUtils.saveImage(bufferedImage, outputPath);
        } catch (IOException e) {
            throw new OcrException(e);
        }finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    @Override
    public OcrInfo recognize(BufferedImage image, OcrRecOptions options) {
        if (!BufferedImageUtils.isImageValid(image)) {
            throw new OcrException("图像无效");
        }
        Image img = SmartImageFactory.getInstance().fromBufferedImage(image);
        OcrInfo ocrInfo = recognize(img, options);
        ImageUtils.releaseOpenCVMat(img);
        return ocrInfo;
    }

    @Override
    public OcrInfo recognize(byte[] imageData, OcrRecOptions options) {
        if (Objects.isNull(imageData)) {
            throw new OcrException("图像无效");
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return recognize(image, options);
        } catch (IOException e) {
            throw new OcrException("错误的图像", e);
        }
    }

    @Override
    public BufferedImage recognizeAndDraw(BufferedImage sourceImage, int fontSize, OcrRecOptions options) {
        if (!BufferedImageUtils.isImageValid(sourceImage)) {
            throw new OcrException("图像无效");
        }
        Image img = SmartImageFactory.getInstance().fromBufferedImage(sourceImage);
        OcrInfo ocrInfo = recognize(img, options);
        if (Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()) {
            throw new OcrException("未检测到文字");
        }
        OcrUtils.drawOcrResult(sourceImage, ocrInfo, fontSize);
        return sourceImage;
    }

    @Override
    public String recognizeAndDrawToBase64(byte[] imageData, int fontSize, OcrRecOptions options) {
        if (Objects.isNull(imageData)) {
            throw new OcrException("图像无效");
        }
        OcrInfo ocrInfo = recognize(imageData, options);
        if (Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()) {
            throw new OcrException("未检测到文字");
        }
        try {
            BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(imageData));
            OcrUtils.drawOcrResult(sourceImage, ocrInfo, fontSize);
            return ImgUtil.toBase64(sourceImage, "png");
        } catch (IOException e) {
            throw new OcrException("导出图片失败", e);
        }
    }

    @Override
    public OcrInfo recognizeAndDraw(byte[] imageData, int fontSize, OcrRecOptions options) {
        if (Objects.isNull(imageData)) {
            throw new OcrException("图像无效");
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromBytes(imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        OcrInfo ocrInfo = recognize(img, options);
        if (Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()) {
            throw new OcrException("未检测到文字");
        }
        //opencv中文乱码，使用BufferedImage
        BufferedImage sourceImage = ImageUtils.toBufferedImage(img);
        OcrUtils.drawOcrResult(sourceImage, ocrInfo, fontSize);
        ocrInfo.setDrawnImage(SmartImageFactory.getInstance().fromBufferedImage(sourceImage));
        return ocrInfo;
    }

    @Override
    public List<OcrInfo> batchRecognize(List<BufferedImage> imageList, OcrRecOptions options) {
        List<Image> djlImageList = new ArrayList<>(imageList.size());
        try {
            for (BufferedImage bufferedImage : imageList) {
                djlImageList.add(SmartImageFactory.getInstance().fromBufferedImage(bufferedImage));
            }
            return batchRecognizeDJLImage(djlImageList, options);
        } catch (Exception e) {
            throw new OcrException(e);
        } finally {
            djlImageList.forEach(image -> ImageUtils.releaseOpenCVMat(image));
        }
    }

    @Override
    public List<OcrInfo> batchRecognizeDJLImage(List<Image> imageList, OcrRecOptions options) {
        if (Objects.isNull(textDetModel)) {
            throw new OcrException("textDetModel is null");
        }
        OcrRecOptions ocrRecOptions = options;
        if (Objects.isNull(options)) {
            ocrRecOptions = new OcrRecOptions();
        }
        if (CollectionUtils.isEmpty(imageList)) {
            throw new OcrException("imageList is empty");
        }
        //检测文本
        List<List<OcrBox>> boxeList = textDetModel.batchDetectDJLImage(imageList);
        if (CollectionUtils.isEmpty(boxeList) || boxeList.size() != imageList.size()) {
            throw new OcrException("未检测到文本");
        }
        Predictor<Image, String> predictor = null;
        List<OcrInfo> ocrInfoList = new ArrayList<OcrInfo>();
        try (NDManager manager = NDManager.newBaseManager()) {
            predictor = recPredictorPool.borrowObject();
            List<Image> allImageAlignList = new ArrayList<Image>();
            //检测方向
            if (ocrRecOptions.isEnableDirectionCorrect()) {
                if (Objects.isNull(directionModel)) {
                    throw new OcrException("请配置方向模型");
                }
                List<Mat> matList = imageList.stream()
                        .map(image -> ImageUtils.toMat(image))
                        .collect(Collectors.toList());
                List<List<OcrItem>> ocrItemList = directionModel.batchDetect(boxeList, matList);
                if (CollectionUtils.isEmpty(ocrItemList) || ocrItemList.size() != imageList.size()) {
                    throw new OcrException("方向检测失败");
                }
                allImageAlignList = new ArrayList<Image>();
                for (int i = 0; i < ocrItemList.size(); i++) {
                    Mat srcMat = ImageUtils.toMat(imageList.get(i));
                    List<Image> imageAlignList = batchAlignWithDirection(ocrItemList.get(i), srcMat, manager);
//                    for(int j = 0; j < imageAlignList.size(); j++){
//                        ImageUtils.saveImage(imageAlignList.get(j),"dir-"+i+"-"+j+".png","/Users/xxx/Downloads/testing33");
//                    }
                    allImageAlignList.addAll(imageAlignList);
                }
            } else {
                for (int i = 0; i < boxeList.size(); i++) {
                    Mat srcMat = ImageUtils.toMat(imageList.get(i));
                    List<Image> imageAlignList = batchAlign(boxeList.get(i), srcMat, manager);
//                    for(int j = 0; j < imageAlignList.size(); j++){
//                        ImageUtils.saveImage(imageAlignList.get(j),i+"-"+j+".png","/Users/wenjie/Downloads/testing33");
//                    }
                    allImageAlignList.addAll(imageAlignList);
                }
            }
            List<String> textList = batchRecognize(allImageAlignList);
            int textIndex = 0;
            for (int i = 0; i < boxeList.size(); i++) {
                List<RotatedBox> rotatedBoxes = new ArrayList<>();
                for (int j = 0; j < boxeList.get(i).size(); j++) {
                    if (textIndex >= textList.size()) {
                        throw new OcrException("识别失败: 第" + i + "张图片, 第" + j + "个文本块，未识别到文本");
                    }
                    OcrBox box = boxeList.get(i).get(j);
                    NDArray pointsArray = manager.create(box.toFloatArray());
                    rotatedBoxes.add(new RotatedBox(pointsArray, textList.get(textIndex)));
                    textIndex++;
                }
                OcrInfo ocrInfo = postProcessOcrResult(rotatedBoxes, ocrRecOptions);
                ocrInfoList.add(ocrInfo);
            }
            return ocrInfoList;
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        } finally {
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

    private List<String> batchRecognize(List<Image> imageAlignList) {
        Predictor<Image, String> predictor = null;
        try {
            predictor = recPredictorPool.borrowObject();
            List<String> textList = predictor.batchPredict(imageAlignList);
            imageAlignList.forEach(subImg -> ImageUtils.releaseOpenCVMat(subImg));
            return textList;
        } catch (Exception e) {
            throw new OcrException("OCR检测错误", e);
        } finally {
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
    public OcrInfo recognizeAndDraw(Image image, int fontSize, OcrRecOptions options) {
        OcrInfo ocrInfo = recognize(image, options);
        if (Objects.isNull(ocrInfo) || Objects.isNull(ocrInfo.getLineList()) || ocrInfo.getLineList().isEmpty()) {
            throw new OcrException("未检测到文字");
        }
        BufferedImage sourceImage = ImageUtils.toBufferedImage(image);
        OcrUtils.drawOcrResult(sourceImage, ocrInfo, fontSize);
        ocrInfo.setDrawnImage(SmartImageFactory.getInstance().fromBufferedImage(sourceImage));
        return ocrInfo;
    }

    @Override
    public void setTextDetModel(OcrCommonDetModel detModel) {
        this.textDetModel = detModel;
    }

    @Override
    public OcrCommonDetModel getTextDetModel() {
        return textDetModel;
    }

    @Override
    public void setDirectionModel(OcrDirectionModel directionModel) {
        this.directionModel = directionModel;
    }

    @Override
    public OcrDirectionModel getDirectionModel() {
        return directionModel;
    }


    public GenericObjectPool<Predictor<Image, String>> getRecPredictorPool() {
        return recPredictorPool;
    }

    @Override
    public void close() throws Exception {
        if (fromFactory) {
            OcrModelFactory.removeRecModelFromCache(config.getRecModelEnum());
        }
        try {
            if (recPredictorPool != null) {
                recPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (recognitionModel != null) {
                recognitionModel.close();
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
