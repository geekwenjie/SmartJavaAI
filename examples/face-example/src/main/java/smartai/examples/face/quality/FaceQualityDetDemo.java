package smartai.examples.face.quality;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.config.FaceAttributeConfig;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.QualityConfig;
import cn.smartjavaai.face.entity.FaceQualityResult;
import cn.smartjavaai.face.entity.FaceQualitySummary;
import cn.smartjavaai.face.enums.FaceAttributeModelEnum;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.QualityModelEnum;
import cn.smartjavaai.face.factory.FaceAttributeModelFactory;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceQualityModelFactory;
import cn.smartjavaai.face.model.attribute.FaceAttributeModel;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.quality.FaceQualityModel;
import cn.smartjavaai.face.utils.FaceUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 人脸质量评估 demo
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class FaceQualityDetDemo {

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
     * 获取质量评估模型
     * @return
     */
    public FaceQualityModel getFaceQualityModel() {
        QualityConfig config = new QualityConfig();
        config.setModelEnum(QualityModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/DengWenJie/Downloads/sf3.0_models/sf3.0_models");
        config.setDevice(device);
        return FaceQualityModelFactory.getInstance().getModel(config);
    }


    /**
     * 获取人脸检测模型
     * @return
     */
    public FaceDetModel getFaceDetModel() {
        //需替换为实际模型存储路径
        String modelPath = "C:/Users/DengWenJie/Downloads/sf3.0_models/sf3.0_models";
        FaceDetConfig faceDetectModelConfig = new FaceDetConfig();
        faceDetectModelConfig.setModelEnum(FaceDetModelEnum.SEETA_FACE6_MODEL);
        faceDetectModelConfig.setModelPath(modelPath);
        faceDetectModelConfig.setDevice(device);
        return FaceDetModelFactory.getInstance().getModel(faceDetectModelConfig);
    }


    /**
     * 人脸亮度评估
     */
    @Test
    public void evaluateBrightness(){
        try {
            FaceQualityModel faceQualityModel = getFaceQualityModel();
            FaceDetModel faceDetModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //人脸检测
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        R<FaceQualityResult> faceQualityResultR = faceQualityModel.evaluateBrightness(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        if(faceQualityResultR.isSuccess()){
                            log.info("人脸亮度评估结果：{}", JSONObject.toJSONString(faceQualityResultR.getData()));
                        }else{
                            log.info("人脸亮度评估失败：{}", faceQualityResultR.getMessage());
                        }
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸完整度评估
     */
    @Test
    public void evaluateCompleteness(){
        try {
            FaceQualityModel faceQualityModel = getFaceQualityModel();
            FaceDetModel faceDetModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //人脸检测
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        R<FaceQualityResult> faceQualityResultR = faceQualityModel.evaluateCompleteness(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        if(faceQualityResultR.isSuccess()){
                            log.info("人脸完整度评估结果：{}", JSONObject.toJSONString(faceQualityResultR.getData()));
                        }else{
                            log.info("人脸完整度评估失败：{}", faceQualityResultR.getMessage());
                        }
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸清晰度评估
     */
    @Test
    public void evaluateClarity(){
        try {
            FaceQualityModel faceQualityModel = getFaceQualityModel();
            FaceDetModel faceDetModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        R<FaceQualityResult> faceQualityResultR = faceQualityModel.evaluateClarity(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        if(faceQualityResultR.isSuccess()){
                            log.info("人脸清晰度评估结果：{}", JSONObject.toJSONString(faceQualityResultR.getData()));
                        }else{
                            log.info("人脸清晰度评估失败：{}", faceQualityResultR.getMessage());
                        }
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸姿态评估
     */
    @Test
    public void evaluatePose(){
        try {
            FaceQualityModel faceQualityModel = getFaceQualityModel();
            FaceDetModel faceDetModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //人脸检测
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        R<FaceQualityResult> faceQualityResultR = faceQualityModel.evaluatePose(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        if(faceQualityResultR.isSuccess()){
                            log.info("人脸姿态评估结果：{}", JSONObject.toJSONString(faceQualityResultR.getData()));
                        }else{
                            log.info("人脸姿态评估失败：{}", faceQualityResultR.getMessage());
                        }
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 人脸分辨率评估
     */
    @Test
    public void evaluateResolution(){
        try {
            FaceQualityModel faceQualityModel = getFaceQualityModel();
            FaceDetModel faceDetModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //人脸检测
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        R<FaceQualityResult> faceQualityResultR = faceQualityModel.evaluateResolution(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        if(faceQualityResultR.isSuccess()){
                            log.info("人脸分辨率评估结果：{}", JSONObject.toJSONString(faceQualityResultR.getData()));
                        }else{
                            log.info("人脸分辨率评估失败：{}", faceQualityResultR.getMessage());
                        }
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 评估所有
     */
    @Test
    public void evaluateAll(){
        try {
            FaceQualityModel faceQualityModel = getFaceQualityModel();
            FaceDetModel faceDetModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            //人脸检测
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        R<FaceQualitySummary> faceQualityResultR = faceQualityModel.evaluateAll(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        if(faceQualityResultR.isSuccess()){
                            log.info("人脸评估结果：{}", JSONObject.toJSONString(faceQualityResultR.getData()));
                        }else{
                            log.info("人脸评估失败：{}", faceQualityResultR.getMessage());
                        }
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
