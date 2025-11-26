package smartai.examples.vision;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.instanceseg.config.InstanceSegModelConfig;
import cn.smartjavaai.instanceseg.enums.InstanceSegModelEnum;
import cn.smartjavaai.instanceseg.model.InstanceSegModelFactory;
import cn.smartjavaai.obb.config.ObbDetModelConfig;
import cn.smartjavaai.obb.enums.ObbDetModelEnum;
import cn.smartjavaai.obb.model.ObbDetModel;
import cn.smartjavaai.obb.model.ObbDetModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * obb旋转框检测demo
 * 模型下载地址：https://pan.baidu.com/s/1-tC0u-aha3tnMQwy8FKy1Q?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class ObbDetDemo {

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
     * 获取旋转框检测模型
     * 注意事项：
     * 1、更多模型请查看文档：http://doc.smartjavaai.cn
     * 2、模型可检测物体请查看：模型同目录文件synset.txt
     */
    public ObbDetModel getModel(){
        ObbDetModelConfig config = new ObbDetModelConfig();
        //旋转框检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(ObbDetModelEnum.YOLOV11);
        //模型所在路径，synset.txt也需要放在同目录下
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/obb/yolo11n-obb.onnx");
        // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("plane","ship"));
        //指定返回检测数量
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        return ObbDetModelFactory.getInstance().getModel(config);
    }



    /**
     * 旋转框检测
     */
    @Test
    public void obbDet(){
        try {
            ObbDetModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/obb/boats.jpg"));
            R<DetectionResponse> result = detectorModel.detect(image);
            if(result.isSuccess()){
                log.info("旋转框检测结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("旋转框检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 旋转框检测并绘制检测结果
     */
    @Test
    public void obbDetAndDraw(){
        try {
            ObbDetModel detectorModel = getModel();
            R<DetectionResponse> result = detectorModel.detectAndDraw("src/main/resources/obb/boats.jpg","output/boats_detected.png");
            if(result.isSuccess()){
                log.info("旋转框检测结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("旋转框检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 旋转框检测并绘制检测结果
     */
    @Test
    public void obbDetAndDraw2(){
        try {
            ObbDetModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/obb/boats.jpg"));
            //可以根据后续业务场景使用detectedImage
            R<DetectionResponse> result = detectorModel.detectAndDraw(image);
            if(result.isSuccess()){
                log.info("旋转框检测结果：{}", JSONObject.toJSONString(result.getData()));
                //保存图片
                ImageUtils.save(result.getData().getDrawnImage(), "output/boats_obb_detected2.png");
            }else{
                log.info("旋转框检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
