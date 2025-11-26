package smartai.examples.vision;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.Joints;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.pose.config.PoseModelConfig;
import cn.smartjavaai.pose.enums.PoseModelEnum;
import cn.smartjavaai.pose.model.PoseDetModelFactory;
import cn.smartjavaai.pose.model.PoseModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * 姿态估计demo
 * 模型下载地址：https://pan.baidu.com/s/1pPYyl1V2CpcMYCO8CJQHGg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class PoseDetDemo {

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
     * 获取姿态估计模型
     * 注意事项：
     * 1、更多模型请查看文档：http://doc.smartjavaai.cn
     */
    public PoseModel getModel(){
        PoseModelConfig config = new PoseModelConfig();
        //姿态估计模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(PoseModelEnum.YOLO11N_POSE_PT);
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/pose/yolo11n-pose-pt");
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.25f);
        return PoseDetModelFactory.getInstance().getModel(config);
    }



    /**
     * 姿态估计
     */
    @Test
    public void poseDet(){
        try {
            PoseModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/pose/pose_soccer.png"));
            R<Joints[]> result = detectorModel.detect(image);
            if(result.isSuccess()){
                log.info("姿态估计结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("姿态估计失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 姿态估计并绘制检测结果
     */
    @Test
    public void poseDetAndDraw(){
        try {
            PoseModel detectorModel = getModel();
            R<Joints[]> result = detectorModel.detectAndDraw("src/main/resources/pose/pose_soccer.png","output/pose_detected.png");
            if(result.isSuccess()){
                log.info("姿态估计结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("姿态估计失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 姿态估计并绘制检测结果
     */
    @Test
    public void poseDetAndDraw2(){
        try {
            PoseModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/pose/pose_soccer.png"));
            //可以根据后续业务场景使用detectedImage
            Image drawImage = detectorModel.detectAndDraw(image);
            //保存图片
            ImageUtils.save(drawImage, "pose_detected2.png", "output");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
