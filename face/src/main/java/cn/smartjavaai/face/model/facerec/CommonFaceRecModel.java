package cn.smartjavaai.face.model.facerec;

import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.SimilarityUtil;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facerec.criteria.FaceRecCriteriaFactory;
import cn.smartjavaai.face.preprocess.DJLImageFacePreprocessor;
import cn.smartjavaai.face.utils.*;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.constant.VectorDBConstants;
import cn.smartjavaai.face.vector.core.VectorDBClient;
import cn.smartjavaai.face.vector.core.VectorDBFactory;
import cn.smartjavaai.face.vector.entity.FaceVector;
import cn.smartjavaai.face.vector.exception.VectorDBException;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * FaceNet 人脸特征提取模型
 * @author dwj
 */
@Slf4j
public class CommonFaceRecModel implements FaceRecModel{

    /**
     * 特征维度
     */
    private static final int DIMENSION = 512;

    /**
     * 是否加载人脸库完毕
     */
    private static volatile boolean isLoadCompleted = false;


    private GenericObjectPool<Predictor<Image, float[]>> predictorPool;


    private ZooModel<Image, float[]> model;

    private FaceRecConfig config;

    /**
     * 是否归一化相似度
     */
    public static final boolean NORMALIZE_SIMILARITY = true;

    private VectorDBClient vectorDBClient;


    /**
     * 加载人脸特征提取模型
     * @param config
     */
    @Override
    public void loadModel(FaceRecConfig config) {
        if(Objects.isNull(config)){
            throw new FaceException("config为null");
        }
        if(Objects.isNull(config.getDetectModel())){
            throw new FaceException("请指定人脸检测模型");
        }
        this.config = config;
        Criteria<Image, float[]> faceFeatureCriteria = FaceRecCriteriaFactory.createCriteria(config);
        try {
            model = faceFeatureCriteria.loadModel();
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
            throw new FaceException("模型加载失败", e);
        }

        //初始化人脸库
        if(config.getVectorDBConfig() != null && config.getVectorDBConfig().getType() != null){
            if(config.getVectorDBConfig() instanceof MilvusConfig){
                MilvusConfig milvusConfig = ((MilvusConfig) config.getVectorDBConfig());
                //设置向量维度
                milvusConfig.setDimension(DIMENSION);
                //相似度计算方式 为空，设置默认值
                if(Objects.isNull(milvusConfig.getMetricType())){
                    //FaceNet 默认使用内积
                    milvusConfig.setMetricType(MetricType.IP);
                }

            }else if (config.getVectorDBConfig() instanceof SQLiteConfig){
                SQLiteConfig  sqliteConfig = (SQLiteConfig) config.getVectorDBConfig();
                if(Objects.isNull(sqliteConfig.getSimilarityType())){
                    //seetaface6 默认使用内积
                    sqliteConfig.setSimilarityType(SimilarityType.IP);
                }
            }
            vectorDBClient = VectorDBFactory.createClient(config.getVectorDBConfig());

            // 加载人脸数据库
            if(config.isAutoLoadFace()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.debug("start load face...");
                            vectorDBClient.initialize();
                            isLoadCompleted = true;
                            log.debug("Load face success!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public float[] featureExtraction(Image image){
        Predictor<Image, float[]> predictor = null;
        try {
            predictor = predictorPool.borrowObject();
            return predictor.predict(image);
        } catch (Exception e) {
            throw new FaceException("人脸特征提取错误", e);
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


    /**
     * 计算相似度，返回归一化结果
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     */
    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        //默认返回归一化结果
        return SimilarityUtil.calculate(feature1, feature2, SimilarityType.IP, true);
    }

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     */
    @Override
    public R<Float> featureComparison(String imagePath1, String imagePath2) {
        if(!FileUtils.isFileExists(imagePath1) || !FileUtils.isFileExists(imagePath2)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        try {
            image1 = ImageIO.read(new File(Paths.get(imagePath1).toAbsolutePath().toString()));
            image2 = ImageIO.read(new File(Paths.get(imagePath2).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return featureComparison(image1, image2);
    }


    @Override
    public R<Float> featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2) {
        if(!BufferedImageUtils.isImageValid(sourceImage1) || !BufferedImageUtils.isImageValid(sourceImag2)){
            throw new FaceException("图像无效");
        }
        R<float[]> feature1 = extractTopFaceFeature(sourceImage1);
        if (!feature1.isSuccess()){
            return R.fail(feature1.getCode(), feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(sourceImag2);
        if (!feature2.isSuccess()){
            return R.fail(feature2.getCode(), feature2.getMessage());
        }
        float ret = calculSimilar(feature1.getData(), feature2.getData());
        return R.ok(ret);
    }

    @Override
    public R<Float> featureComparison(byte[] imageData1, byte[] imageData2) {
        if(Objects.isNull(imageData1) || Objects.isNull(imageData2)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            BufferedImage bufferedImage1 = ImageIO.read(new ByteArrayInputStream(imageData1));
            BufferedImage bufferedImage2 = ImageIO.read(new ByteArrayInputStream(imageData2));
            return featureComparison(bufferedImage1, bufferedImage2);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<DetectionResponse> extractFeatures(BufferedImage image) {
        R<DetectionResponse> detectedResult = config.getDetectModel().detect(image);
        if(!detectedResult.isSuccess()){
            return detectedResult;
        }
        if(Objects.isNull(detectedResult.getData()) || Objects.isNull(detectedResult.getData().getDetectionInfoList()) || detectedResult.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image djlImage = SmartImageFactory.getInstance().fromBufferedImage(image);
        try (NDManager manager = model.getNDManager().newSubManager()) {
            DJLImageFacePreprocessor djlImagePreprocessor = new DJLImageFacePreprocessor(djlImage, manager);
            for (DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()){
                DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
                FaceInfo faceInfo = detectionInfo.getFaceInfo();
                float[] features = null;
                Image subImage = djlImage;
                //人脸对齐
                if(config.isAlign()){
                    //人脸对齐
                    double[][] pointsArray = FaceUtils.facePoints(faceInfo.getKeyPoints());
                    djlImagePreprocessor.enableCrop(rectangle).enableAffine(pointsArray, 96, 112);
                    subImage = djlImagePreprocessor.process();
                }else{
                    //裁剪
                    djlImagePreprocessor.enableCrop(rectangle);
                    if(config.isCropFace()){
                        subImage = djlImagePreprocessor.process();
                    }
                }
                features = featureExtraction(subImage);
                if (subImage != null && subImage.getWrappedImage() instanceof Mat) {
                    ((Mat)subImage.getWrappedImage()).release();
                }
                faceInfo.setFeature(features);
            }
        }finally {
            if (djlImage != null && djlImage.getWrappedImage() instanceof Mat) {
                ((Mat)djlImage.getWrappedImage()).release();
            }
        }
        return detectedResult;
    }


    @Override
    public R<DetectionResponse> extractFeatures(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return extractFeatures(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<DetectionResponse> extractFeatures(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractFeatures(image);
    }

    @Override
    public R<float[]> extractTopFaceFeature(BufferedImage image) {
        R<DetectionResponse> detectedResult = config.getDetectModel().detect(image);
        if(!detectedResult.isSuccess()){
            return R.fail(detectedResult.getCode(), detectedResult.getMessage());
        }
        if(Objects.isNull(detectedResult.getData()) || Objects.isNull(detectedResult.getData().getDetectionInfoList()) || detectedResult.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        Image djlImage = SmartImageFactory.getInstance().fromBufferedImage(image);
        float[] features = null;
        try (NDManager manager = model.getNDManager().newSubManager()) {
            DJLImageFacePreprocessor djlImagePreprocessor = new DJLImageFacePreprocessor(djlImage, manager);
            //只取第一个人脸
            DetectionInfo detectionInfo = detectedResult.getData().getDetectionInfoList().get(0);
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            Image subImage = djlImage;
            //人脸对齐
            if(config.isAlign()){
                //人脸对齐
                double[][] pointsArray = FaceUtils.facePoints(faceInfo.getKeyPoints());
                djlImagePreprocessor.enableCrop(rectangle).enableAffine(pointsArray, 96, 112);
                subImage = djlImagePreprocessor.process();
            }else{
                //裁剪
                djlImagePreprocessor.enableCrop(rectangle);
                if(config.isCropFace()){
                    subImage = djlImagePreprocessor.process();
                }
            }
            features = featureExtraction(subImage);
            if (subImage != null && subImage.getWrappedImage() instanceof Mat) {
                ((Mat)subImage.getWrappedImage()).release();
            }
        }finally {
            if (djlImage != null && djlImage.getWrappedImage() instanceof Mat) {
                ((Mat)djlImage.getWrappedImage()).release();
            }
        }
        return Objects.isNull(features) ? R.fail(R.Status.Unknown) : R.ok(features);
    }

    @Override
    public R<float[]> extractTopFaceFeature(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        // 将图片路径转换为 BufferedImage
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return extractTopFaceFeature(image);
    }

    @Override
    public R<float[]> extractTopFaceFeature(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return extractTopFaceFeature(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }



    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return register(faceRegisterInfo, bufferedImage);
    }


    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, BufferedImage sourceImage) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return register(faceRegisterInfo, featureResponse.getData());
    }


    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, InputStream inputStream) {
        if(Objects.isNull(inputStream)){
            throw new FaceException("图像输入流无效");
        }
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new FaceException("无效图片输入流", e);
        }
        return register(faceRegisterInfo, image);
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return register(faceRegisterInfo, ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, float[] feature) {
        if(vectorDBClient == null){
            return R.fail(1000, "向量数据库未初始化成功");
        }
        if(Objects.isNull(feature)){
            return R.fail(R.Status.PARAM_ERROR.getCode(), "人脸特征为空");
        }
        FaceVector faceVector = new FaceVector();
        if(faceRegisterInfo != null){
            faceVector.setId(faceRegisterInfo.getId());
            faceVector.setMetadata(faceRegisterInfo.getMetadata());
        }
        faceVector.setVector(feature);
        return R.ok(vectorDBClient.insert(faceVector));
    }

    @Override
    public void removeRegister(String... keys) {
        vectorDBClient.deleteBatch(Arrays.asList(keys));
    }

    @Override
    public void clearFace() {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        vectorDBClient.dropCollection(VectorDBConstants.Defaults.DEFAULT_COLLECTION_NAME);
    }



    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            throw new FaceException("图像文件不存在");
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        upsertFace(faceRegisterInfo, bufferedImage);
    }


    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, BufferedImage sourceImage) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        if(Objects.isNull(faceRegisterInfo)){
            throw new FaceException("注册信息为空");
        }
        if(StringUtils.isBlank(faceRegisterInfo.getId())){
            throw new FaceException("注册信息中ID为空");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            throw new FaceException(featureResponse.getMessage());
        }
        upsertFace(faceRegisterInfo, featureResponse.getData());
    }

    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, float[] feature) {
        if(Objects.isNull(feature)){
            throw new FaceException("人脸特征为空");
        }
        if(Objects.isNull(vectorDBClient)){
            throw new FaceException("未初始化人脸库");
        }
        FaceVector faceVector = new FaceVector();
        if(faceRegisterInfo != null){
            faceVector.setId(faceRegisterInfo.getId());
            faceVector.setMetadata(faceRegisterInfo.getMetadata());
        }
        faceVector.setVector(feature);
        vectorDBClient.upsert(faceVector);
    }


    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, byte[] imageData) {
        if(Objects.isNull(imageData)){
            throw new FaceException("图像无效");
        }
        try {
            upsertFace(faceRegisterInfo, ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(String imagePath, FaceSearchParams params) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return searchByTopFace(bufferedImage, params);
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(byte[] imageData, FaceSearchParams params) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return searchByTopFace(ImageIO.read(new ByteArrayInputStream(imageData)), params);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(BufferedImage sourceImage, FaceSearchParams params) {
        if(vectorDBClient == null){
            return R.fail(1000, "向量数据库未初始化成功");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return R.ok(search(featureResponse.getData(), params));
    }



    @Override
    public List<FaceSearchResult> search(float[] feature, FaceSearchParams params) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        if(Objects.isNull(feature)){
            throw new FaceException("人脸特征为空");
        }
        if(Objects.isNull(params)){
            throw new FaceException("人脸查询参数为空");
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? config.getModelEnum().getThreshold() : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        List<FaceSearchResult> searchResults = vectorDBClient.search(feature, searchParams);
        return searchResults;
    }

    @Override
    public R<DetectionResponse> search(String imagePath, FaceSearchParams params) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(Paths.get(imagePath).toAbsolutePath().toString()));
        } catch (IOException e) {
            throw new FaceException("无效图片路径", e);
        }
        return search(bufferedImage, params);
    }

    @Override
    public R<DetectionResponse> search(BufferedImage sourceImage, FaceSearchParams params) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取所有人脸特征
        R<DetectionResponse> detectionResponse = extractFeatures(sourceImage);
        if(!detectionResponse.isSuccess()){
            return detectionResponse;
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? config.getModelEnum().getThreshold() : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
            if(Objects.nonNull(detectionInfo.getFaceInfo()) && Objects.nonNull(detectionInfo.getFaceInfo().getFeature())){
                List<FaceSearchResult> searchResults = vectorDBClient.search(detectionInfo.getFaceInfo().getFeature(), searchParams);
                if (CollectionUtils.isEmpty(searchResults)){
                    return R.fail(1000, "未找到匹配结果");
                }
                detectionInfo.getFaceInfo().setFaceSearchResults(searchResults);
            }
        }
        return detectionResponse;
    }

    @Override
    public R<DetectionResponse> search(byte[] imageData, FaceSearchParams params) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return search(ImageIO.read(new ByteArrayInputStream(imageData)), params);
        } catch (IOException e) {
            throw new FaceException("错误的图像", e);
        }
    }

    @Override
    public R<FaceVector> getFaceInfoById(String id) {
        if(vectorDBClient == null){
            return R.fail(1000, "向量数据库未初始化成功");
        }
        return R.ok(vectorDBClient.getFaceInfoById(id));
    }

    @Override
    public R<List<FaceVector>> listFaces(long pageNum, long pageSize) {
        if(vectorDBClient == null){
            return R.fail(1000, "向量数据库未初始化成功");
        }
        return R.ok(vectorDBClient.listFaces(pageNum, pageSize));
    }

    @Override
    public void loadFaceFeatures() {
        if(Objects.isNull(vectorDBClient)){
            throw new FaceException("未初始化人脸库");
        }
        vectorDBClient.loadFaceFeatures();
    }

    @Override
    public void releaseFaceFeatures() {
        if(Objects.isNull(vectorDBClient)){
            throw new FaceException("未初始化人脸库");
        }
        vectorDBClient.releaseFaceFeatures();
    }

    @Override
    public R<Float> featureComparison(Image image1, Image image2) {
        R<float[]> feature1 = extractTopFaceFeature(image1);
        if (!feature1.isSuccess()){
            return R.fail(feature1.getCode(), feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(image2);
        if (!feature2.isSuccess()){
            return R.fail(feature2.getCode(), feature2.getMessage());
        }
        float ret = calculSimilar(feature1.getData(), feature2.getData());
        return R.ok(ret);
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, Image image) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(image);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return register(faceRegisterInfo, featureResponse.getData());
    }

    @Override
    public void upsertFace(FaceRegisterInfo faceRegisterInfo, Image image) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        if(Objects.isNull(faceRegisterInfo)){
            throw new FaceException("注册信息为空");
        }
        if(StringUtils.isBlank(faceRegisterInfo.getId())){
            throw new FaceException("注册信息中ID为空");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(image);
        if(!featureResponse.isSuccess()){
            throw new FaceException(featureResponse.getMessage());
        }
        upsertFace(faceRegisterInfo, featureResponse.getData());
    }

    @Override
    public R<DetectionResponse> search(Image image, FaceSearchParams params) {
        if(vectorDBClient == null){
            throw new VectorDBException("向量数据库未初始化成功");
        }
        //提取所有人脸特征
        R<DetectionResponse> detectionResponse = extractFeatures(image);
        if(!detectionResponse.isSuccess()){
            return detectionResponse;
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? config.getModelEnum().getThreshold() : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
            if(Objects.nonNull(detectionInfo.getFaceInfo()) && Objects.nonNull(detectionInfo.getFaceInfo().getFeature())){
                List<FaceSearchResult> searchResults = vectorDBClient.search(detectionInfo.getFaceInfo().getFeature(), searchParams);
                if (CollectionUtils.isEmpty(searchResults)){
                    return R.fail(1000, "未找到匹配结果");
                }
                detectionInfo.getFaceInfo().setFaceSearchResults(searchResults);
            }
        }
        return detectionResponse;
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(Image image, FaceSearchParams params) {
        if(vectorDBClient == null){
            return R.fail(1000, "向量数据库未初始化成功");
        }
        //提取最大人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(image);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return R.ok(search(featureResponse.getData(), params));
    }

    @Override
    public R<DetectionResponse> extractFeatures(Image image) {
        R<DetectionResponse> detectedResult = config.getDetectModel().detect(image);
        if(!detectedResult.isSuccess()){
            return detectedResult;
        }
        if(Objects.isNull(detectedResult.getData()) || Objects.isNull(detectedResult.getData().getDetectionInfoList()) || detectedResult.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        try (NDManager manager = model.getNDManager().newSubManager()) {
            DJLImageFacePreprocessor djlImagePreprocessor = new DJLImageFacePreprocessor(image, manager);
            for (DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()){
                DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
                FaceInfo faceInfo = detectionInfo.getFaceInfo();
                float[] features = null;
                Image subImage = null;
                //人脸对齐
                if(config.isAlign()){
                    //人脸对齐
                    double[][] pointsArray = FaceUtils.facePoints(faceInfo.getKeyPoints());
                    djlImagePreprocessor.enableCrop(rectangle).enableAffine(pointsArray, 96, 112);
                    subImage = djlImagePreprocessor.process();
                }else{
                    //裁剪
                    djlImagePreprocessor.enableCrop(rectangle);
                    if(config.isCropFace()){
                        subImage = djlImagePreprocessor.process();
                    }
                }
                features = featureExtraction(subImage);
                if (subImage != null && subImage.getWrappedImage() instanceof Mat) {
                    ((Mat)subImage.getWrappedImage()).release();
                }
                faceInfo.setFeature(features);
            }
        }
        return detectedResult;
    }

    @Override
    public R<float[]> extractFeatures(Image image, DetectionInfo detectionInfo) {
        float[] features = null;
        try (NDManager manager = model.getNDManager().newSubManager()) {
            DJLImageFacePreprocessor djlImagePreprocessor = new DJLImageFacePreprocessor(image, manager);
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            Image subImage = null;
            //人脸对齐
            if(config.isAlign()){
                //人脸对齐
                double[][] pointsArray = FaceUtils.facePoints(faceInfo.getKeyPoints());
                djlImagePreprocessor.enableCrop(rectangle).enableAffine(pointsArray, 96, 112);
                subImage = djlImagePreprocessor.process();
            }else{
                //裁剪
                djlImagePreprocessor.enableCrop(rectangle);
                if(config.isCropFace()){
                    subImage = djlImagePreprocessor.process();
                }
            }
            features = featureExtraction(subImage);
            if (subImage != null && subImage.getWrappedImage() instanceof Mat) {
                ((Mat)subImage.getWrappedImage()).release();
            }
        }
        return Objects.isNull(features) ? R.fail(R.Status.Unknown) : R.ok(features);
    }

    @Override
    public R<float[]> extractTopFaceFeature(Image image) {
        R<DetectionResponse> detectedResult = config.getDetectModel().detect(image);
        if(!detectedResult.isSuccess()){
            return R.fail(detectedResult.getCode(), detectedResult.getMessage());
        }
        if(Objects.isNull(detectedResult.getData()) || Objects.isNull(detectedResult.getData().getDetectionInfoList()) || detectedResult.getData().getDetectionInfoList().isEmpty()){
            return R.fail(R.Status.NO_FACE_DETECTED);
        }
        float[] features = null;
        try (NDManager manager = model.getNDManager().newSubManager()) {
            DJLImageFacePreprocessor djlImagePreprocessor = new DJLImageFacePreprocessor(image, manager);
            //只取第一个人脸
            DetectionInfo detectionInfo = detectedResult.getData().getDetectionInfoList().get(0);
            DetectionRectangle rectangle = detectionInfo.getDetectionRectangle();
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            Image subImage = null;
            //人脸对齐
            if(config.isAlign()){
                //人脸对齐
                double[][] pointsArray = FaceUtils.facePoints(faceInfo.getKeyPoints());
                djlImagePreprocessor.enableCrop(rectangle).enableAffine(pointsArray, 96, 112);
                subImage = djlImagePreprocessor.process();
            }else{
                //裁剪
                djlImagePreprocessor.enableCrop(rectangle);
                if(config.isCropFace()){
                    subImage = djlImagePreprocessor.process();
                }
            }
            features = featureExtraction(subImage);
            if (subImage != null && subImage.getWrappedImage() instanceof Mat) {
                ((Mat)subImage.getWrappedImage()).release();
            }
        }
        return Objects.isNull(features) ? R.fail(R.Status.Unknown) : R.ok(features);
    }


    @Override
    public Image drawSearchResult(Image image, FaceSearchParams params, String displayField) {
        R<DetectionResponse> detectionResponse = search(image, params);
        Image drawImage = ImageUtils.copy(image);
        BufferedImage bufferedImage = ImageUtils.toBufferedImage(drawImage);
        BufferedImageUtils.drawFaceSearchResult(bufferedImage, detectionResponse.getData(), displayField);
        return SmartImageFactory.getInstance().fromBufferedImage(bufferedImage);
    }

    @Override
    public void close() {
        if (fromFactory) {
            FaceRecModelFactory.removeFromCache(config.getModelEnum());
        }
        try {
            if (predictorPool != null) {
                predictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }

        try {
            if(Objects.nonNull(vectorDBClient)){
                vectorDBClient.close();
            }
        } catch (Exception e) {
            log.warn("关闭 vectorDBClient 失败", e);
        }

    }

    @Override
    public boolean isLoadFaceCompleted() {
        return isLoadCompleted;
    }


    @Override
    public GenericObjectPool<Predictor<Image, float[]>> getPool() {
        return predictorPool;
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
