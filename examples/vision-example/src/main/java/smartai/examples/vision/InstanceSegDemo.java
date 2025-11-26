package smartai.examples.vision;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.action.model.ActionRecModel;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.instanceseg.config.InstanceSegModelConfig;
import cn.smartjavaai.instanceseg.enums.InstanceSegModelEnum;
import cn.smartjavaai.instanceseg.model.InstanceSegModel;
import cn.smartjavaai.instanceseg.model.InstanceSegModelFactory;
import cn.smartjavaai.objectdetection.model.person.PersonDetModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 实例分割 Demo
 * 模型下载地址：https://pan.baidu.com/s/12nRRY9JFNDwLeg63jfBerA?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class InstanceSegDemo {



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
     * 获取实例分割模型
     * 注意事项：
     * 1、更多模型请查看文档：http://doc.smartjavaai.cn
     */
    public InstanceSegModel getModel(){
        InstanceSegModelConfig config = new InstanceSegModelConfig();
        //实例分割模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(InstanceSegModelEnum.SEG_YOLO11N_ONNX);
        //模型所在路径，synset.txt也需要放在同目录下
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/instance/yolo11n-seg-onnx/yolo11n-seg.onnx");
        // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person","car"));
        //指定返回检测数量
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        return InstanceSegModelFactory.getInstance().getModel(config);
    }



    /**
     * 实例分割
     */
    @Test
    public void instanceSegmentation(){
        try {
            InstanceSegModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/dog_bike_car.jpg"));
            R<DetectionResponse> result = detectorModel.detect(image);
            if(result.isSuccess()){
                log.info("实例分割结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("实例分割失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实例分割并绘制检测结果
     */
    @Test
    public void instanceSegmentationAndDraw(){
        try {
            InstanceSegModel detectorModel = getModel();
            R<DetectionResponse> result = detectorModel.detectAndDraw("src/main/resources/dog_bike_car.jpg","output/dog_bike_car_detected.png");
            if(result.isSuccess()){
                log.info("实例分割结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("实例分割失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实例分割并绘制检测结果
     */
    @Test
    public void instanceSegmentationAndDraw2(){
        try {
            InstanceSegModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/dog_bike_car.jpg"));
            //可以根据后续业务场景使用detectedImage
            R<DetectionResponse> result = detectorModel.detectAndDraw(image);
            if(result.isSuccess()){
                log.info("实例分割结果：{}", JSONObject.toJSONString(result.getData()));
                //保存图片
                ImageUtils.save(result.getData().getDrawnImage(), "dog_bike_car_detected2.png", "output");
            }else{
                log.info("实例分割失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
