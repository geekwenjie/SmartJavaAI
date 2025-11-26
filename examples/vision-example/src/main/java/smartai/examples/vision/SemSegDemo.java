package smartai.examples.vision;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.CategoryMask;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.semseg.config.SemSegModelConfig;
import cn.smartjavaai.semseg.enums.SemSegModelEnum;
import cn.smartjavaai.semseg.model.SemSegModel;
import cn.smartjavaai.semseg.model.SemSegModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 语义分割 Demo 通过网盘分享的文件：语义分割（semantic_segmentation）
 * 模型下载地址：https://pan.baidu.com/s/18gs9E5h_d9imPmNLHuDo9A?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class SemSegDemo {



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
     * 获取语义分割模型
     * 注意事项：
     * 1、更多模型请查看文档：http://doc.smartjavaai.cn
     */
    public SemSegModel getModel(){
        SemSegModelConfig config = new SemSegModelConfig();
        //语义分割模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(SemSegModelEnum.DEEPLABV3);
        //模型所在路径，synset.txt也需要放在同目录下
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/semseg/deeplabv3/deeplabv3.pt");
        // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person","car"));
        config.setDevice(device);
        return SemSegModelFactory.getInstance().getModel(config);
    }



    /**
     * 语义分割
     */
    @Test
    public void semSeg(){
        try {
            SemSegModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/dog_bike_car.jpg"));
            R<CategoryMask> result = detectorModel.detect(image);
            if(result.isSuccess()){
                log.info("语义分割结果：{}", result.getData());
            }else{
                log.info("语义分割失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语义分割并绘制检测结果
     */
    @Test
    public void semSegAndDraw(){
        try {
            SemSegModel detectorModel = getModel();
            R<CategoryMask> result = detectorModel.detectAndDraw("src/main/resources/dog_bike_car.jpg","output/dog_bike_car_semseg.png");
            if(result.isSuccess()){
                log.info("语义分割结果：{}", result.getData());
            }else{
                log.info("语义分割失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 语义分割并绘制检测结果
     */
    @Test
    public void semSegAndDraw2(){
        try {
            SemSegModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/dog_bike_car.jpg"));
            //可以根据后续业务场景使用detectedImage
            Image dretectedImage = detectorModel.detectAndDraw(image);
            //保存
            ImageUtils.save(dretectedImage, "dog_bike_car_detected2.png", "output");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
