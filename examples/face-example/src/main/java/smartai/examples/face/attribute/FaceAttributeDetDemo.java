package smartai.examples.face.attribute;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceAttributeConfig;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.enums.FaceAttributeModelEnum;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.factory.FaceAttributeModelFactory;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.attribute.FaceAttributeModel;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
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
import java.util.List;

/**
 * 人脸属性检测demo
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class FaceAttributeDetDemo {

    @BeforeClass
    public static void beforeAll() throws IOException {
        //将图片处理的底层引擎切换为 OpenCV
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }


    public FaceAttributeModel getFaceAttributeModel() {
        FaceAttributeConfig config = new FaceAttributeConfig();
        config.setModelEnum(FaceAttributeModelEnum.SEETA_FACE6_MODEL);
        //需替换为实际模型存储路径
        config.setModelPath("C:/Users/DengWenJie/Downloads/sf3.0_models/sf3.0_models");
        return FaceAttributeModelFactory.getInstance().getModel(config);
    }

    public FaceDetModel getFaceDetModel() {
        //需替换为实际模型存储路径
        String modelPath = "C:/Users/DengWenJie/Downloads/sf3.0_models/sf3.0_models";
        FaceDetConfig faceDetectModelConfig = new FaceDetConfig();
        faceDetectModelConfig.setModelEnum(FaceDetModelEnum.SEETA_FACE6_MODEL);
        faceDetectModelConfig.setModelPath(modelPath);
        return FaceDetModelFactory.getInstance().getModel(faceDetectModelConfig);
    }


    /**
     * 人脸属性检测（多人脸）
     */
    @Test
    public void testFaceAttributeDetect(){
        try {
            FaceAttributeModel faceAttributeModel = getFaceAttributeModel();
            ////创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            DetectionResponse detectionResponse = faceAttributeModel.detect(image);
            //绘制并导出人脸属性图片，小人脸仅有人脸框
            BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
            FaceUtils.drawBoxesWithFaceAttribute(bufferedImage, detectionResponse,"C:/Users/Administrator/Downloads/double_person_.png");
            log.info("人脸属性检测结果：{}", JSONObject.toJSONString(detectionResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片人脸属性检测（分数最高人脸）
     */
    @Test
    public void testFaceAttributeDetect2(){
        try {
            FaceAttributeModel faceAttributeModel = getFaceAttributeModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            FaceAttribute faceAttribute = faceAttributeModel.detectTopFace(image);
            log.info("人脸属性检测结果：{}", JSONObject.toJSONString(faceAttribute));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 图片单人脸人脸属性检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testFaceAttributeDetect4(){
        try {
            FaceDetModel faceDetModel = getFaceDetModel();
            FaceAttributeModel faceAttributeModel = getFaceAttributeModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/iu_1.jpg");
            R<DetectionResponse> detectionResponse = faceDetModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    for (DetectionInfo detectionInfo : detectionResponse.getData().getDetectionInfoList()){
                        FaceInfo faceInfo = detectionInfo.getFaceInfo();
                        FaceAttribute faceAttribute = faceAttributeModel.detect(image, detectionInfo.getDetectionRectangle(), faceInfo.getKeyPoints());
                        log.info("人脸属性检测结果：{}", JSONObject.toJSONString(faceAttribute));
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
