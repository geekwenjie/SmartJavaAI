package smartai.examples.vision;

import ai.djl.modality.cv.Image;

import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.zeroshot.config.ZeroDetConfig;
import cn.smartjavaai.zeroshot.enums.ZeroDetModelEnum;
import cn.smartjavaai.zeroshot.model.ZeroDetModel;
import cn.smartjavaai.zeroshot.model.ZeroDetModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 零样本目标检测
 * @author dwj
 */
@Slf4j
public class ZeroShotObjectDetectionDemo {


    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取零样本目标检测模型
     */
    public ZeroDetModel getModel(){
        ZeroDetConfig config = new ZeroDetConfig();
        //零样本目标检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(ZeroDetModelEnum.OWLV2_BASE_PATCH16);
        //模型所在路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/zero/owlv2-base-patch16");
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        return ZeroDetModelFactory.getInstance().getModel(config);
    }



    /**
     * 零样本目标检测
     * 特性：
     * 1、零样本检测能力：无需针对特定类别进行训练，可直接通过文本查询检测新类别物体
     * 2、开放词汇识别：能够识别训练时未见过的类别名称，突破传统检测模型的类别限制
     * 3、多查询支持：支持同时使用多个文本查询进行目标检测，提高检测效率
     */
    @Test
    public void zeroDetection(){
        try {
            ZeroDetModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/zero/000000039769.jpg"));
            //输入图片以及条件
            R<DetectionResponse> result = detectorModel.detect(image, new String[]{"cat","remote control"});
            if(result.isSuccess()){
                log.info("零样本目标检测结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("零样本目标检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 零样本目标检测并绘制检测结果
     * 特性：
     * 1、零样本检测能力：无需针对特定类别进行训练，可直接通过文本查询检测新类别物体
     * 2、开放词汇识别：能够识别训练时未见过的类别名称，突破传统检测模型的类别限制
     * 3、多查询支持：支持同时使用多个文本查询进行目标检测，提高检测效率
     */
    @Test
    public void zeroDetectionAndDraw() {
        try {
            ZeroDetModel detectorModel = getModel();
            String[] candidates = new String[]{"cat","remote control"};
            //保存绘制后图片以及返回检测结果
            R<DetectionResponse> result = detectorModel.detectAndDraw(candidates, "src/main/resources/zero/000000039769.jpg","output/cat_detected.png");
            if(result.isSuccess()){
                log.info("零样本目标检测结果：{}", JSONObject.toJSONString(result.getData()));
            }else{
                log.info("零样本目标检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 零样本目标检测并绘制检测结果
     * 特性：
     * 1、零样本检测能力：无需针对特定类别进行训练，可直接通过文本查询检测新类别物体
     * 2、开放词汇识别：能够识别训练时未见过的类别名称，突破传统检测模型的类别限制
     * 3、多查询支持：支持同时使用多个文本查询进行目标检测，提高检测效率
     */
    @Test
    public void zeroDetectionAndDraw2(){
        try {
            ZeroDetModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/zero/000000039769.jpg"));
            String[] candidates = new String[]{"cat","remote control"};
            R<DetectionResponse> result = detectorModel.detectAndDraw(image, candidates);
            if(result.isSuccess()){
                log.info("零样本目标检测结果：{}", JSONObject.toJSONString(result.getData()));
                //保存图片
                ImageUtils.save(result.getData().getDrawnImage(), "output/cat_detected.png");
            }else{
                log.info("零样本目标检测失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
