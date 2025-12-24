package smartai.examples.face;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.utils.FaceUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @author dwj
 * @date 2025/12/23
 */
@Slf4j
public class FaceDemo {

    public static String MODEL_PATH = "";

    /**
     * 获取人脸检测模型
     * 注意事项：极速模型，识别准确度低，速度快
     * @return
     */
    public static FaceDetModel getFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.YOLOV5_FACE_320);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath(MODEL_PATH + "/face_model/yolo-face/yolov5face-n-0.5-320x320.onnx");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }


    public static void main(String[] args) {
        try {
            MODEL_PATH = System.getenv("SMART_MODEL_PATH");
            log.info("MODEL_PATH:" + MODEL_PATH);
            Config.setCachePath("/app/smartjavaai_cache");

            FaceDetModel faceModel = getFaceDetModel();
            InputStream is = FaceDemo.class
                    .getClassLoader()
                    .getResourceAsStream("iu_1.jpg");
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromInputStream(is);
            R<DetectionResponse> detectedResult = faceModel.detect(image);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
