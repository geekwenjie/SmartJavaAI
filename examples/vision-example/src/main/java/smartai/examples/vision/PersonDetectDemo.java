package smartai.examples.vision;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.objectdetection.config.PersonDetModelConfig;
import cn.smartjavaai.objectdetection.enums.PersonDetectorModelEnum;
import cn.smartjavaai.objectdetection.model.person.PersonDetModel;
import cn.smartjavaai.objectdetection.model.person.PersonDetModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * 行人检测案例
 * 模型下载地址：https://pan.baidu.com/s/1EWfExw7pYjKEH5uR5wf3Rw?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class PersonDetectDemo {

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
     * 获取行人检测模型
     */
    public PersonDetModel getModel(){
        PersonDetModelConfig config = new PersonDetModelConfig();
        //行人检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(PersonDetectorModelEnum.YOLOV8_PERSON);
        //模型所在路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/person/yolov8n-person.onnx");
        //指定返回检测数量
        config.setTopK(100);
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        return PersonDetModelFactory.getInstance().getModel(config);
    }



    /**
     * 行人检测
     */
    @Test
    public void objectDetection(){
        try {
            PersonDetModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/person/person.png"));
            R<DetectionResponse> result = detectorModel.detect(image);
            if(result.isSuccess()){
                log.info("行人检测结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("行人检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 行人检测并绘制检测结果
     */
    @Test
    public void objectDetectionAndDraw(){
        try {
            PersonDetModel detectorModel = getModel();
            //保存绘制后图片以及返回检测结果
            R<DetectionResponse> result = detectorModel.detectAndDraw("src/main/resources/person/person.png","output/person_detected.png");
            if(result.isSuccess()){
                log.info("行人检测结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("行人检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 行人检测并绘制检测结果
     */
    @Test
    public void objectDetectionAndDraw2(){
        try {
            PersonDetModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/person/person.png"));
            //可以根据后续业务场景使用detectedImage
            R<DetectionResponse> result = detectorModel.detectAndDraw(image);
            if(result.isSuccess()){
                log.info("行人检测结果：{}", JSONObject.toJSONString(result.getData()));
                //保存图片
                ImageUtils.save(result.getData().getDrawnImage(), "person_result.png", "output");
            }else{
                log.info("行人检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
