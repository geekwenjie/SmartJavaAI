package cn.smartjavaai.face.model.facerec;

import ai.djl.engine.Engine;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDManager;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceResult;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.preprocess.DJLImageFacePreprocessor;
import cn.smartjavaai.face.utils.FaceUtils;
import cn.smartjavaai.face.utils.Seetaface6Utils;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.core.VectorDBClient;
import cn.smartjavaai.face.vector.core.VectorDBFactory;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.face.vector.entity.FaceVector;
import cn.smartjavaai.face.vector.exception.VectorDBException;
import com.seeta.pool.*;
import com.seeta.sdk.*;
import cn.smartjavaai.face.seetaface.NativeLoader;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * SeetaFace6 人脸模型
 * @author dwj
 */
@SuppressWarnings("AliMissingOverrideAnnotation")
@Slf4j
public class SeetaFace6FaceRecModel implements FaceRecModel{

    /**
     * 特征维度
     */
    private static final int DIMENSION = 1024;


    private FaceRecConfig config;

    private FaceDetectorPool faceDetectorPool;
    private FaceRecognizerPool faceRecognizerPool;
    private FaceLandmarkerPool faceLandmarkerPool;

    private FaceDatabasePool faceDatabasePool;

    private VectorDBClient vectorDBClient = null;

    /**
     * 是否加载人脸库完毕
     */
    private static volatile boolean isLoadCompleted = false;

    /**
     * 是否归一化相似度
     */
    public static final boolean NORMALIZE_SIMILARITY = false;



    @Override
    public void loadModel(FaceRecConfig config) {
        this.config = config;
        if(StringUtils.isBlank(config.getModelPath())){
            throw new FaceException("modelPath is null");
        }
        //加载依赖库
        NativeLoader.loadNativeLibraries(config.getDevice());
        log.debug("Loading seetaFace6 library successfully.");
        String[] faceDetectorModelPath = {config.getModelPath() + File.separator + "face_detector.csta"};
        String[] faceRecognizerModelPath = {config.getModelPath() + File.separator + "face_recognizer.csta"};
        //轻量模型
        if(config.getModelEnum() == FaceRecModelEnum.SEETA_FACE6_LIGHT_MODEL){
            faceRecognizerModelPath = new String[] { config.getModelPath() + File.separator + "face_recognizer_light.csta" };
        }
        String[] faceLandmarkerModelPath = {config.getModelPath() + File.separator + "face_landmarker_pts5.csta"};
        SeetaDevice device = SeetaDevice.SEETA_DEVICE_AUTO;
        int gpuId = config.getGpuId();
        if(Objects.nonNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? SeetaDevice.SEETA_DEVICE_CPU : SeetaDevice.SEETA_DEVICE_GPU;
        }
        try {
            SeetaModelSetting faceDetectorPoolSetting = new SeetaModelSetting(gpuId, faceDetectorModelPath, device);
            SeetaConfSetting faceDetectorPoolConfSetting = new SeetaConfSetting(faceDetectorPoolSetting);

            SeetaModelSetting faceRecognizerPoolSetting = new SeetaModelSetting(gpuId, faceRecognizerModelPath, device);
            SeetaConfSetting faceRecognizerPoolConfSetting = new SeetaConfSetting(faceRecognizerPoolSetting);

            SeetaModelSetting faceLandmarkerPoolSetting = new SeetaModelSetting(gpuId, faceLandmarkerModelPath, device);
            SeetaConfSetting faceLandmarkerPoolConfSetting = new SeetaConfSetting(faceLandmarkerPoolSetting);

            SeetaModelSetting faceDatabasePoolSetting = new SeetaModelSetting(gpuId, faceRecognizerModelPath, device);
            SeetaConfSetting faceDatabasePoolConfSetting = new SeetaConfSetting(faceDatabasePoolSetting);

            this.faceDetectorPool = new FaceDetectorPool(faceDetectorPoolConfSetting);
            this.faceRecognizerPool = new FaceRecognizerPool(faceRecognizerPoolConfSetting);
            this.faceLandmarkerPool = new FaceLandmarkerPool(faceLandmarkerPoolConfSetting);
            this.faceDatabasePool = new FaceDatabasePool(faceDatabasePoolConfSetting);

            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            faceDetectorPool.setMaxTotal(predictorPoolSize);
            faceRecognizerPool.setMaxTotal(predictorPoolSize);
            faceLandmarkerPool.setMaxTotal(predictorPoolSize);
            faceDatabasePool.setMaxTotal(predictorPoolSize);
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);


            //初始化人脸库
            if(config.getVectorDBConfig() != null && config.getVectorDBConfig().getType() != null){
                if(config.getVectorDBConfig() instanceof MilvusConfig){
                    MilvusConfig milvusConfig = ((MilvusConfig) config.getVectorDBConfig());
                    //设置向量维度
                    milvusConfig.setDimension(DIMENSION);
                    //相似度计算方式 为空，设置默认值
                    if(Objects.isNull(milvusConfig.getMetricType())){
                        //seetaface6 默认使用余弦相似度
                        milvusConfig.setMetricType(MetricType.COSINE);
                    }

                }else if (config.getVectorDBConfig() instanceof SQLiteConfig){
                    SQLiteConfig  sqliteConfig = (SQLiteConfig) config.getVectorDBConfig();
                    if(Objects.isNull(sqliteConfig.getSimilarityType())){
                        //seetaface6 默认使用余弦相似度
                        sqliteConfig.setSimilarityType(SimilarityType.COSINE);
                    }
                }
                //创建向量数据库
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
        } catch (FileNotFoundException e) {
            throw new FaceException(e);
        }
    }

    @Override
    public float calculSimilar(float[] feature1, float[] feature2) {
        if(Objects.isNull(feature1) || Objects.isNull(feature2)){
            throw new FaceException("特征向量无效");
        }
        FaceRecognizer faceRecognizer = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            return faceRecognizer.CalculateSimilarity(feature1, feature2);
        } catch (Exception e) {
            throw new FaceException(e);
        }finally {
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<Float> featureComparison(String imagePath1, String imagePath2) {
        if(!FileUtils.isFileExists(imagePath1) || !FileUtils.isFileExists(imagePath2)){
            throw new FaceException("图像文件不存在");
        }
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
    public R<Float> featureComparison(BufferedImage image1, BufferedImage image2) {
        if(!BufferedImageUtils.isImageValid(image1) || !BufferedImageUtils.isImageValid(image2)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        R<float[]> feature1 = extractTopFaceFeature(image1);
        if(!feature1.isSuccess()){
            return R.fail(feature1.getCode(), feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(image2);
        if(!feature2.isSuccess()){
            return R.fail(feature2.getCode(), feature2.getMessage());
        }
        return R.ok(calculSimilar(feature1.getData(), feature2.getData()));
    }




    @Override
    public R<Float> featureComparison(byte[] imageData1, byte[] imageData2) {
        if(Objects.isNull(imageData1) || Objects.isNull(imageData2)){
            throw new FaceException("图像无效");
        }
        BufferedImage image1 = null;
        BufferedImage image2 = null;
        try {
            image1 = ImageIO.read(new ByteArrayInputStream(imageData1));
            image2 = ImageIO.read(new ByteArrayInputStream(imageData2));
        } catch (IOException e) {
            throw new FaceException("无效图片", e);
        }
        return featureComparison(image1, image2);
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
    public R<String> register(FaceRegisterInfo faceRegisterInfo, BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        //提取特征向量
        R<float[]> featureResponse = extractTopFaceFeature(image);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        return register(faceRegisterInfo, featureResponse.getData());
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new FaceException(e);
        }
        return register(faceRegisterInfo, bufferedImage);
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
            throw new FaceException("无效的图片输入流", e);
        }
        return register(faceRegisterInfo, image);
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, float[] feature) {
        if(Objects.isNull(feature)){
            return R.fail(R.Status.Unknown.getCode(),  "人脸注册失败：人脸特征为空");
        }
        FaceVector faceVector = new FaceVector();
        if(Objects.nonNull(faceRegisterInfo)){
            faceVector.setId(faceRegisterInfo.getId());
            faceVector.setMetadata(faceRegisterInfo.getMetadata());
        }
        faceVector.setVector(feature);
        return R.ok(vectorDBClient.insert(faceVector));
    }



//    /**
//     * 注册已裁剪后人脸
//     * @param key
//     * @param faceData
//     * @return
//     */
//    private boolean registerCroppedFace(String key, FaceData faceData) {
//        FaceDatabase faceDatabase = null;
//        try {
//            faceDatabase = faceDatabasePool.borrowObject();
//            SeetaImageData cropImageData = new SeetaImageData(faceData.getWidth(), faceData.getHeight(), faceData.getChannel());
//            cropImageData.data = faceData.getImgData();
//            long index = faceDatabase.RegisterByCroppedFace(cropImageData);
//            if (index < 0) {
//                log.debug("register face fail: key={}, index={}", key, index);
//                return false;
//            }
//            int rows = 0;
//            try {
//                rows = new FaceDao(config.getFaceDbPath()).updateIndex(index, faceData);
//            } catch (SQLException | ClassNotFoundException e) {
//                throw new FaceException(e);
//            }
//            return rows > 0;
//        } catch (FaceException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new FaceException(e);
//        }finally {
//            if (faceDatabase != null) {
//                try {
//                    faceDatabasePool.returnObject(faceDatabase); //归还
//                } catch (Exception e) {
//                    log.warn("归还Predictor失败", e);
//                }
//            }
//        }
//    }



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
    public R<DetectionResponse> search(BufferedImage image, FaceSearchParams params) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
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
    public R<DetectionResponse> search(byte[] imageData, FaceSearchParams params) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new FaceException(e);
        }
        return search(bufferedImage, params);
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
    public R<List<FaceSearchResult>> searchByTopFace(BufferedImage sourceImage, FaceSearchParams params) {
        if(!BufferedImageUtils.isImageValid(sourceImage)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        //提取分数最高人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(sourceImage);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? config.getModelEnum().getThreshold() : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        List<FaceSearchResult> searchResults = vectorDBClient.search(featureResponse.getData(), searchParams);
        return R.ok(searchResults);
    }

    @Override
    public R<List<FaceSearchResult>> searchByTopFace(byte[] imageData, FaceSearchParams params) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new FaceException(e);
        }
        return searchByTopFace(bufferedImage, params);
    }

    @Override
    public void removeRegister(String... keys) {
        if(keys == null || keys.length == 0){
            throw new FaceException("keys不允许为空");
        }
        vectorDBClient.deleteBatch(Arrays.asList(keys));
    }

    @Override
    public void clearFace(){
        vectorDBClient.dropCollection(null);
    }

    /**
     * 检查是否存在人脸库
     * @return
     */
//    private boolean checkFaceDb(){
//        if(Objects.nonNull(config) && StringUtils.isNotBlank(config.getFaceDbPath())){
//            File file = new File(config.getFaceDbPath());
//            return file.exists() && file.isFile();
//        }
//        return false;
//    }

    private FaceResult searchFaceDb(long index,float similar) {
        if(index >= 0){
            String key = null;
//            try {
//                key = new FaceDao(config.getFaceDbPath()).findKeyByIndex(index);
//            } catch (SQLException | ClassNotFoundException e) {
//                throw new FaceException("查询人脸库失败", e);
//            }
            return new FaceResult(key, similar);
        }
        return null;
    }


    /**
     * 加载人脸库
     * @throws SQLException
     * @throws ClassNotFoundException
     */
//    private void loadFaceDb() {
//        if(!checkFaceDb()){
//            log.debug("未配置人脸库");
//            return;
//        }
//        //分页查询人脸库
//        int pageNo = 0, pageSize = 100;
//        while (true) {
//            List<FaceData> list = null;
//            try {
//                list = new FaceDao(config.getFaceDbPath()).findFace(pageNo, pageSize);
//            } catch (SQLException | ClassNotFoundException e) {
//                throw new FaceException("查询人脸库失败", e);
//            }
//            if (list == null) {
//                break;
//            }
//            list.forEach(face -> {
//                try {
//                    registerCroppedFace(face.getKey(), face);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            if (list.size() < pageSize) {
//                break;
//            }
//            pageNo++;
//        }
//    }


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
    public R<DetectionResponse> extractFeatures(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            return extractFeatures(ImageIO.read(new ByteArrayInputStream(imageData)));
        } catch (IOException e) {
            throw new FaceException("特征提取异常", e);
        }
    }

    @Override
    public R<DetectionResponse> extractFeatures(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        FaceRecognizer faceRecognizer = null;
        try {
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = BufferedImageUtils.getMatrixBGR(image);
            faceRecognizer = faceRecognizerPool.borrowObject();
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                List<float[]> featureList = new ArrayList<float[]>();
                List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
                faceDetector = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                //检测人脸
                SeetaRect[] seetaResult = faceDetector.Detect(imageData);
                if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(SeetaRect seetaRect : seetaResult){
                    //提取人脸的5点人脸标识
                    SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, pointFS);
                    //提取特征
                    float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
                    //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
                    boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
                    if(!isSuccess){
                        return R.fail(R.Status.Unknown.getCode(), "人脸特征提取失败");
                    }
                    featureList.add(features);
                    seetaPointFSList.add(pointFS);
                }
                return R.ok(FaceUtils.featuresConvertToResponse(seetaResult, seetaPointFSList, featureList));
            }else{
                R<DetectionResponse> detectResponse = config.getDetectModel().detect(image);
                if(!detectResponse.isSuccess()){
                    return detectResponse;
                }
                if(Objects.isNull(detectResponse.getData()) || Objects.isNull(detectResponse.getData().getDetectionInfoList()) || detectResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(DetectionInfo detectionInfo : detectResponse.getData().getDetectionInfoList()){
                    //提取特征
                    float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
                    FaceInfo faceInfo = detectionInfo.getFaceInfo();
                    if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                        return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
                    }
                    SeetaPointF[] pointFS = Seetaface6Utils.convertToSeetaPointF(faceInfo.getKeyPoints());
                    //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
                    boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
                    if(!isSuccess){
                        return R.fail(R.Status.Unknown.getCode(), "人脸特征提取失败");
                    }
                    faceInfo.setFeature(features);
                }
                return detectResponse;
            }
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("人脸特征提取异常", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }


    @Override
    public R<float[]> extractTopFaceFeature(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        float[] features = null;
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = BufferedImageUtils.getMatrixBGR(image);
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        FaceRecognizer faceRecognizer = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            //提取人脸的5点人脸标识
            SeetaPointF[] pointFS = null;
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                faceDetector = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                //检测人脸
                SeetaRect[] seetaResult = faceDetector.Detect(imageData);
                if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaResult[0], pointFS);
            }else{
                R<DetectionResponse> detectResponse = config.getDetectModel().detect(image);
                if(!detectResponse.isSuccess()){
                    return R.fail(detectResponse.getCode(), detectResponse.getMessage());
                }
                if(Objects.isNull(detectResponse.getData()) || Objects.isNull(detectResponse.getData().getDetectionInfoList()) || detectResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                DetectionInfo detectionInfo = detectResponse.getData().getDetectionInfoList().get(0);
                pointFS = Seetaface6Utils.convertToSeetaPointF(detectionInfo.getFaceInfo().getKeyPoints());
            }
            //提取特征
            features = new float[faceRecognizer.GetExtractFeatureSize()];
            //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
            boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
            if(!isSuccess){
                return R.fail(R.Status.Unknown.getCode(),  "人脸特征提取失败");
            }
            return R.ok(features);
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    /**
     * 特征提取
     * @param image
     * @return
     */
    public float[] featureExtraction(BufferedImage image){
        FaceRecognizer faceRecognizer = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = BufferedImageUtils.getMatrixBGR(image);
            //提取特征
            float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
            faceRecognizer.ExtractCroppedFace(imageData, features);
            return features;
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
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
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));
        } catch (IOException e) {
            throw new FaceException(e);
        }
        upsertFace(faceRegisterInfo, bufferedImage);
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
    public R<Float> featureComparison(Image image1, Image image2) {
        R<float[]> feature1 = extractTopFaceFeature(image1);
        if(!feature1.isSuccess()){
            return R.fail(feature1.getCode(), feature1.getMessage());
        }
        R<float[]> feature2 = extractTopFaceFeature(image2);
        if(!feature2.isSuccess()){
            return R.fail(feature2.getCode(), feature2.getMessage());
        }
        return R.ok(calculSimilar(feature1.getData(), feature2.getData()));
    }

    @Override
    public R<String> register(FaceRegisterInfo faceRegisterInfo, Image image) {
        //提取特征向量
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
        //提取分数最高人脸特征
        R<float[]> featureResponse = extractTopFaceFeature(image);
        if(!featureResponse.isSuccess()){
            return R.fail(featureResponse.getCode(), featureResponse.getMessage());
        }
        //设置默认值
        float threshold = Objects.isNull(params.getThreshold()) ? config.getModelEnum().getThreshold() : params.getThreshold();
        int topK = Objects.isNull(params.getTopK()) ? 1 : params.getTopK();
        boolean normalize = Objects.isNull(params.getNormalizeSimilarity()) ? NORMALIZE_SIMILARITY : params.getNormalizeSimilarity();
        FaceSearchParams searchParams = new FaceSearchParams(topK, threshold, normalize);
        List<FaceSearchResult> searchResults = vectorDBClient.search(featureResponse.getData(), searchParams);
        return R.ok(searchResults);
    }

    @Override
    public R<DetectionResponse> extractFeatures(Image image) {
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        FaceRecognizer faceRecognizer = null;
        try {
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            faceRecognizer = faceRecognizerPool.borrowObject();
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                List<float[]> featureList = new ArrayList<float[]>();
                List<SeetaPointF[]> seetaPointFSList = new ArrayList<SeetaPointF[]>();
                faceDetector = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                //检测人脸
                SeetaRect[] seetaResult = faceDetector.Detect(imageData);
                if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(SeetaRect seetaRect : seetaResult){
                    //提取人脸的5点人脸标识
                    SeetaPointF[] pointFS = new SeetaPointF[faceLandmarker.number()];
                    faceLandmarker.mark(imageData, seetaRect, pointFS);
                    //提取特征
                    float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
                    //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
                    boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
                    if(!isSuccess){
                        return R.fail(R.Status.Unknown.getCode(), "人脸特征提取失败");
                    }
                    featureList.add(features);
                    seetaPointFSList.add(pointFS);
                }
                return R.ok(FaceUtils.featuresConvertToResponse(seetaResult, seetaPointFSList, featureList));
            }else{
                R<DetectionResponse> detectResponse = config.getDetectModel().detect(image);
                if(!detectResponse.isSuccess()){
                    return detectResponse;
                }
                if(Objects.isNull(detectResponse.getData()) || Objects.isNull(detectResponse.getData().getDetectionInfoList()) || detectResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                for(DetectionInfo detectionInfo : detectResponse.getData().getDetectionInfoList()){
                    //提取特征
                    float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
                    FaceInfo faceInfo = detectionInfo.getFaceInfo();
                    if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                        return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
                    }
                    SeetaPointF[] pointFS = Seetaface6Utils.convertToSeetaPointF(faceInfo.getKeyPoints());
                    //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
                    boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
                    if(!isSuccess){
                        return R.fail(R.Status.Unknown.getCode(), "人脸特征提取失败");
                    }
                    faceInfo.setFeature(features);
                }
                return detectResponse;
            }
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("人脸特征提取异常", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<float[]> extractFeatures(Image image, DetectionInfo detectionInfo) {
        FaceRecognizer faceRecognizer = null;
        try {
            SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
            imageData.data = ImageUtils.getMatrixBGR(image);
            faceRecognizer = faceRecognizerPool.borrowObject();

            //提取特征
            float[] features = new float[faceRecognizer.GetExtractFeatureSize()];
            FaceInfo faceInfo = detectionInfo.getFaceInfo();
            if(Objects.isNull(faceInfo) || Objects.isNull(faceInfo.getKeyPoints())){
                return R.fail(R.Status.Unknown.getCode(), "未检测到人脸关键点");
            }
            SeetaPointF[] pointFS = Seetaface6Utils.convertToSeetaPointF(faceInfo.getKeyPoints());
            //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
            boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
            if(!isSuccess){
                return R.fail(R.Status.Unknown.getCode(), "人脸特征提取失败");
            }
            return Objects.isNull(features) ? R.fail(R.Status.Unknown) : R.ok(features);
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("人脸特征提取异常", e);
        }finally {
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
    }

    @Override
    public R<float[]> extractTopFaceFeature(Image image) {
        float[] features = null;
        SeetaImageData imageData = new SeetaImageData(image.getWidth(), image.getHeight(), 3);
        imageData.data = ImageUtils.getMatrixBGR(image);
        FaceDetector faceDetector = null;
        FaceLandmarker faceLandmarker = null;
        FaceRecognizer faceRecognizer = null;
        try {
            faceRecognizer = faceRecognizerPool.borrowObject();
            //提取人脸的5点人脸标识
            SeetaPointF[] pointFS = null;
            //默认使用Seetaface6检测模型
            if(Objects.isNull(config.getDetectModel())){
                faceDetector = faceDetectorPool.borrowObject();
                faceLandmarker = faceLandmarkerPool.borrowObject();
                //检测人脸
                SeetaRect[] seetaResult = faceDetector.Detect(imageData);
                if(Objects.isNull(seetaResult) || seetaResult.length == 0){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                pointFS = new SeetaPointF[faceLandmarker.number()];
                faceLandmarker.mark(imageData, seetaResult[0], pointFS);
            }else{
                R<DetectionResponse> detectResponse = config.getDetectModel().detect(image);
                if(!detectResponse.isSuccess()){
                    return R.fail(detectResponse.getCode(), detectResponse.getMessage());
                }
                if(Objects.isNull(detectResponse.getData()) || Objects.isNull(detectResponse.getData().getDetectionInfoList()) || detectResponse.getData().getDetectionInfoList().isEmpty()){
                    return R.fail(R.Status.NO_FACE_DETECTED);
                }
                DetectionInfo detectionInfo = detectResponse.getData().getDetectionInfoList().get(0);
                pointFS = Seetaface6Utils.convertToSeetaPointF(detectionInfo.getFaceInfo().getKeyPoints());
            }
            //提取特征
            features = new float[faceRecognizer.GetExtractFeatureSize()];
            //CropFaceV2 + ExtractCroppedFace 已包含裁剪+人脸对齐
            boolean isSuccess = faceRecognizer.Extract(imageData, pointFS, features);
            if(!isSuccess){
                return R.fail(R.Status.Unknown.getCode(),  "人脸特征提取失败");
            }
            return R.ok(features);
        } catch (FaceException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceException("目标检测错误", e);
        }finally {
            if (faceDetector != null) {
                try {
                    faceDetectorPool.returnObject(faceDetector); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceLandmarker != null) {
                try {
                    faceLandmarkerPool.returnObject(faceLandmarker); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
            if (faceRecognizer != null) {
                try {
                    faceRecognizerPool.returnObject(faceRecognizer); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                }
            }
        }
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
    public void close() throws Exception {
        if (fromFactory) {
            FaceRecModelFactory.removeFromCache(config.getModelEnum());
        }
        if(Objects.nonNull(faceDetectorPool)){
            faceDetectorPool.close();
        }
        if(Objects.nonNull(faceRecognizerPool)){
            faceRecognizerPool.close();
        }
        if(Objects.nonNull(faceLandmarkerPool)){
            faceLandmarkerPool.close();
        }
        if(Objects.nonNull(faceDatabasePool)){
            faceDatabasePool.close();
        }
        if(Objects.nonNull(vectorDBClient)){
            vectorDBClient.close();
        }
    }




    @Override
    public boolean isLoadFaceCompleted() {
        return isLoadCompleted;
    }


    public FaceDetectorPool getFaceDetectorPool() {
        return faceDetectorPool;
    }

    public FaceRecognizerPool getFaceRecognizerPool() {
        return faceRecognizerPool;
    }

    public FaceLandmarkerPool getFaceLandmarkerPool() {
        return faceLandmarkerPool;
    }

    public FaceDatabasePool getFaceDatabasePool() {
        return faceDatabasePool;
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
