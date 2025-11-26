package smartai.examples.face.facerec;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.IdStrategy;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.entity.FaceVector;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * 人脸识别模型demo
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class FaceRecDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        //将图片处理的底层引擎切换为 OpenCV
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }


    /**
     * 获取人脸检测模型（均衡模型）
     * 均衡模型：兼顾速度和精度
     * 注意事项：SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
     * @return
     */
    public FaceDetModel getFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.MTCNN);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/mtcnn");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸检测模型（高精度模型）
     * 注意事项：
     * 1、高精度模型，识别准确度高，速度慢
     * 2、具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
     * @return
     */
    public FaceDetModel getProFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/face_model/retinaface.pt");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸检测模型（极速模型）
     * 注意事项：
     * 1、极速模型，识别准确度低，速度快
     * 2、具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
     * @return
     */
    public FaceDetModel getFastFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.YOLOV5_FACE_320);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/face_model/yolo-face/yolov5face-n-0.5-320x320.onnx");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }


    /**
     * 获取人脸识别模型（高精度，速度慢）
     * 追求准确度可以使用
     * 也可以使用其他模型，具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
     * @return
     */
    public FaceRecModel getFaceRecModel(){
        FaceRecConfig config = new FaceRecConfig();
        //高精度模型，速度慢
        config.setModelEnum(FaceRecModelEnum.INSIGHT_FACE_IRSE50_MODEL);
        //模型路径，请下载模型并替换为本地路径：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/recognition/InsightFace/model_ir_se50.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        config.setDevice(device);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型（高速模型，精度一般）
     * 追求速度可以使用
     * 也可以使用其他模型，具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
     * @return
     */
    public FaceRecModel getHighSpeedFaceRecModel(){
        FaceRecConfig config = new FaceRecConfig();
        //模型枚举
        config.setModelEnum(FaceRecModelEnum.INSIGHT_FACE_MOBILE_FACENET_MODEL);
        //模型路径，请下载模型并替换为本地路径：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/recognition/InsightFace/model_mobilefacenet.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(false);
        config.setDevice(device);
        //指定人脸检测模型
        config.setDetectModel(getFastFaceDetModel());
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型(带向量数据库配置)
     * @return
     */
    public FaceRecModel getFaceRecModelWithDbConfig(){
        FaceRecConfig config = new FaceRecConfig();
        //高精度模型，速度慢,追求速度请更换高速模型，具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
        config.setModelEnum(FaceRecModelEnum.INSIGHT_FACE_IRSE50_MODEL);
        //模型路径，请下载模型并替换为本地路径：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/recognition/InsightFace/model_ir_se50.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        //指定人脸检测模型，可切换人脸检测模型（极速：getFastFaceDetModel，高精度：getProFaceDetModel），具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
        config.setDetectModel(getFaceDetModel());
        config.setDevice(device);

        //初始化向量数据库：Milvus数据库配置
        MilvusConfig vectorDBConfig = new MilvusConfig();
        vectorDBConfig.setHost("127.0.0.1");
        vectorDBConfig.setPort(19530);
//        vectorDBConfig.setUsername("root");
//        vectorDBConfig.setPassword("Milvus");
//        vectorDBConfig.setCollectionName("face6");
        //ID策略：自动生成
        vectorDBConfig.setIdStrategy(IdStrategy.AUTO);
        //索引类型:内积 (Inner Product) 不建议修改
        //vectorDBConfig.setMetricType(MetricType.IP);
        config.setVectorDBConfig(vectorDBConfig);
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸识别模型(带SQLite数据库配置)
     * @return
     */
    public FaceRecModel getFaceRecModelWithSQLiteConfig(){
        FaceRecConfig config = new FaceRecConfig();
        //高精度模型，速度慢, 追求速度请更换高速模型，具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
        config.setModelEnum(FaceRecModelEnum.INSIGHT_FACE_IRSE50_MODEL);
        //模型路径，请下载模型并替换为本地路径：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/recognition/InsightFace/model_ir_se50.pt");
        //裁剪人脸：如果图片已经是裁剪过的，则请将此参数设置为false
        config.setCropFace(true);
        //开启人脸对齐：适用于人脸不正的场景，开启将提升人脸特征准确度，关闭可以提升性能
        config.setAlign(true);
        //指定人脸检测模型，可切换人脸检测模型（极速：getFastFaceDetModel，高精度：getProFaceDetModel），具体其他模型参数可以查看文档：http://doc.smartjavaai.cn/face.html
        config.setDetectModel(getFaceDetModel());
        config.setDevice(device);

        //初始化SQLite数据库
        SQLiteConfig vectorDBConfig = new SQLiteConfig();
        vectorDBConfig.setSimilarityType(SimilarityType.IP);
        config.setVectorDBConfig(vectorDBConfig);
        return FaceRecModelFactory.getInstance().getModel(config);
    }

    /**
     * 提取人脸特征(多人脸场景)
     * 自动裁剪人脸（处理耗时略有增加）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     */
    @Test
    public void testExtractFeatures(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型: getHighSpeedFaceRecModel
            FaceRecModel faceRecModel = getFaceRecModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //提取图片中所有人脸特征
            R<DetectionResponse> faceResult  = faceRecModel.extractFeatures(image);
            if(faceResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(faceResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", faceResult.getMessage());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸比对1：1（基于图像直接比对）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 比对两张图片中提取的人脸特征。（接口内自动完成）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void featureComparison(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型: getHighSpeedFaceRecModel
            FaceRecModel faceRecModel = getFaceRecModel();
            //基于图像直接比对人脸特征
            R<Float> similarResult = faceRecModel.featureComparison("src/main/resources/iu_1.jpg","src/main/resources/iu_2.jpg");
            if(similarResult.isSuccess()){
                //相似度阈值不同模型不同，具体参看文档
                log.info("人脸比对相似度：{}", JSONObject.toJSONString(similarResult.getData()));
                //不同模型的相似度标准不同。当前阈值仅适用于 insight_face 模型，切换模型时请相应调整阈值，详情请参考文档。
                if(similarResult.getData() >= 0.62f){
                    log.info("识别为同一人");
                }else{
                    log.info("识别为不同人");
                }
            }else{
                log.info("人脸比对失败：{}", similarResult.getMessage());
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸比对1：1（基于图像直接比对）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 比对两张图片中提取的人脸特征。（接口内自动完成）
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void featureComparison3(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型: getHighSpeedFaceRecModel
            FaceRecModel faceRecModel = getFaceRecModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image1 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            Image image2 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_2.jpg");
            //基于图像直接比对人脸特征
            R<Float> similarResult = faceRecModel.featureComparison(image1, image2);
            if(similarResult.isSuccess()){
                //相似度阈值不同模型不同，具体参看文档
                log.info("人脸比对相似度：{}", JSONObject.toJSONString(similarResult.getData()));
                //不同模型的相似度标准不同。当前阈值仅适用于 insight_face 模型，切换模型时请相应调整阈值，详情请参考文档。
                if(similarResult.getData() >= 0.62f){
                    log.info("识别为同一人");
                }else{
                    log.info("识别为不同人");
                }
            }else{
                log.info("人脸比对失败：{}", similarResult.getMessage());
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸比对1：1（基于特征值比对）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 比对两张图片中提取的人脸特征。
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void featureComparison2(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型: getHighSpeedFaceRecModel
            FaceRecModel faceRecModel = getFaceRecModel();
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image1 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            R<float[]> featureResult1 = faceRecModel.extractTopFaceFeature(image1);
            if(featureResult1.isSuccess()){
                log.info("图片1人脸特征提取成功：{}", JSONObject.toJSONString(featureResult1.getData()));
            }else{
                log.info("图片1人脸特征提取失败：{}", featureResult1.getMessage());
                return;
            }
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            Image image2 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_2.jpg");
            R<float[]> featureResult2 = faceRecModel.extractTopFaceFeature(image2);
            if(featureResult2.isSuccess()){
                log.info("图片2人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("图片2人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            //计算相似度
            float similar = faceRecModel.calculSimilar(featureResult1.getData(), featureResult2.getData());
            log.info("相似度：{}", similar);
            //不同模型的相似度标准不同。当前阈值仅适用于 insight_face 模型，切换模型时请相应调整阈值，详情请参考文档。
            if(similar >= 0.62f){
                log.info("识别为同一人");
            }else{
                log.info("识别为不同人");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 人脸注册 + 人脸更新 + 人脸查询 + 人脸删除（使用向量数据库Milvus）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 注册人脸
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void searchFace(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型
            FaceRecModel faceRecModel = getFaceRecModelWithDbConfig();
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            R<float[]> featureResult = faceRecModel.extractTopFaceFeature(image);
            if(featureResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult.getMessage());
                return;
            }
            //人脸注册信息
            FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "iu");
            metadataJson.put("age", "25");
            //faceRegisterInfo.setId("001");
            faceRegisterInfo.setMetadata(metadataJson.toJSONString());
            //人脸注册，返回人脸库ID
            R<String> registerResult = faceRecModel.register(faceRegisterInfo, featureResult.getData());
            if(registerResult.isSuccess()){
                log.info("注册成功：ID-{}", registerResult.getData());
            }else{
                log.info("注册失败：{}", registerResult.getMessage());
            }
            log.info("====================人脸更新==========================");
            //更新人脸 只支持自定义ID：vectorDBConfig.setIdStrategy(IdStrategy.CUSTOM);
//            FaceRegisterInfo updateInfo = new FaceRegisterInfo();
//            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
//            JSONObject metadataJsonUpdate = new JSONObject();
//            metadataJsonUpdate.put("name", "iu_update");
//            metadataJsonUpdate.put("age", "25");
//            updateInfo.setMetadata(metadataJsonUpdate.toJSONString());
//            //更新必须设置ID,只有
//            updateInfo.setId(registerResult.getData());
//            Image image2 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_2.jpg");
//            faceRecModel.upsertFace(updateInfo, image2);
//            log.info("更新人脸成功");
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            Image image3 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_3.jpg");
            R<float[]> featureResult2 = faceRecModel.extractTopFaceFeature(image3);
            if(featureResult2.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
//            faceSearchParams.setThreshold(0.62f);
            List<FaceSearchResult> faceSearchResults = faceRecModel.search(featureResult2.getData(), faceSearchParams);
//                R<DetectionResponse> faceSearchResults = faceModel.search("src/main/resources/face/iu_3.jpg", faceSearchParams);
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            log.info("====================人脸删除==========================");
            faceRecModel.removeRegister(registerResult.getData());
            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸注册 + 人脸更新 + 人脸查询 + 人脸删除（使用轻量数据库SQLite）
     * 流程：从输入图像中裁剪分数最高的人脸 → 提取其人脸特征 → 注册人脸
     * 注意事项：
     * 1、首次调用接口，可能会较慢。只要不关闭程序，后续调用会明显加快。若每次重启程序，则每次首次调用都将重新加载，仍会较慢。
     * 2、若人脸朝向不正，可开启人脸对齐以提升特征提取准确度。（方法参考自定义配置人脸特征提取）
     * @throws Exception
     */
    @Test
    public void searchFace2(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型
            FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig();
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            R<float[]> featureResult = faceRecModel.extractTopFaceFeature(image);
            if(featureResult.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult.getMessage());
                return;
            }
            //人脸注册信息
            FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "iu");
            metadataJson.put("age", "25");
            faceRegisterInfo.setMetadata(metadataJson.toJSONString());
            //可自定义 ID，若未设置则自动生成。
            //faceRegisterInfo.setId("00001");
            //人脸注册，返回人脸库ID
            R<String> registerResult = faceRecModel.register(faceRegisterInfo, featureResult.getData());
            if(registerResult.isSuccess()){
                log.info("注册成功：ID-{}", registerResult.getData());
            }else{
                log.info("注册失败：{}", registerResult.getMessage());
            }
            log.info("====================人脸更新==========================");
            FaceRegisterInfo updateInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJsonUpdate = new JSONObject();
            metadataJsonUpdate.put("name", "iu_update");
            metadataJsonUpdate.put("age", "25");
            updateInfo.setMetadata(metadataJsonUpdate.toJSONString());
            //更新必须设置ID,只有
            updateInfo.setId(registerResult.getData());
            Image image2 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_2.jpg");
            faceRecModel.upsertFace(updateInfo, image2);
            log.info("更新人脸成功");
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            Image image3 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_3.jpg");
            R<float[]> featureResult2 = faceRecModel.extractTopFaceFeature(image3);
            if(featureResult2.isSuccess()){
                log.info("人脸特征提取成功：{}", JSONObject.toJSONString(featureResult2.getData()));
            }else{
                log.info("人脸特征提取失败：{}", featureResult2.getMessage());
                return;
            }
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
            //faceSearchParams.setThreshold(0.62f);
            List<FaceSearchResult> faceSearchResults = faceRecModel.search(featureResult2.getData(), faceSearchParams);
            log.info("人脸查询结果：{}", JSONArray.toJSONString(faceSearchResults));
            log.info("====================人脸删除==========================");
            faceRecModel.removeRegister(registerResult.getData());
            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人脸查询及绘制
     *
     * @throws Exception
     */
    @Test
    public void searchFace3(){
        try {
            //高精度模型，速度慢, 追求速度请更换高速模型
            FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig();
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            log.info("====================人脸注册==========================");
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //人脸注册信息
            FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
            //设置人脸注册的自定义元数据，本例中使用 JSON 格式存储用户信息
            JSONObject metadataJson = new JSONObject();
            metadataJson.put("name", "iu");
            metadataJson.put("age", "25");
            faceRegisterInfo.setMetadata(metadataJson.toJSONString());
            //可自定义 ID，若未设置则自动生成。
            //faceRegisterInfo.setId("00001");
            //人脸注册，返回人脸库ID
            R<String> registerResult = faceRecModel.register(faceRegisterInfo, image);
            if(registerResult.isSuccess()){
                log.info("注册成功：ID-{}", registerResult.getData());
            }else{
                log.info("注册失败：{}", registerResult.getMessage());
            }
            log.info("====================人脸查询==========================");
            //特征提取（提取分数最高人脸特征）,适用于单人脸场景
            Image image3 = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_3.jpg");
            FaceSearchParams faceSearchParams = new FaceSearchParams();
            faceSearchParams.setTopK(1);
            //faceSearchParams.setThreshold(0.62f);
            //图片中只会显示Metadata信息中name的字段
            Image drawSearchResult = faceRecModel.drawSearchResult(image3, faceSearchParams, "name");
            ImageUtils.save(drawSearchResult, "output/search_result.jpg");
            log.info("====================人脸删除==========================");
            faceRecModel.removeRegister(registerResult.getData());
            log.info("人脸删除成功");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 获取人脸信息
     */
    @Test
    public void getFaceInfo(){
        //使用ID获取人脸信息
        try {
            FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig();
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            //ID需改为你需要查询的ID
            R<FaceVector> faceInfoResult = faceRecModel.getFaceInfoById("9c4c316d53a74b1184195c1714c250c4");
            if(faceInfoResult.isSuccess()){
                log.info("人脸信息：{}", JSONObject.toJSONString(faceInfoResult.getData()));
            }else{
                log.info("获取人脸信息失败：{}", faceInfoResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取人脸信息
     */
    @Test
    public void listFaces(){
        //使用ID获取人脸信息
        try {
            FaceRecModel faceRecModel = getFaceRecModelWithSQLiteConfig();
            //等待加载人脸库结束
            while (!faceRecModel.isLoadFaceCompleted()){
                Thread.sleep(100);
            }
            //ID需改为你需要查询的ID
            R<List<FaceVector>> faceInfoResult = faceRecModel.listFaces(1, 10);
            if(faceInfoResult.isSuccess()){
                log.info("人脸信息：{}", JSONObject.toJSONString(faceInfoResult.getData()));
            }else{
                log.info("获取人脸信息失败：{}", faceInfoResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







}
