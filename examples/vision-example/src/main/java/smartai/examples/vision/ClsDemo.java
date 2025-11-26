package smartai.examples.vision;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.cls.config.ClsModelConfig;
import cn.smartjavaai.cls.enums.ClsModelEnum;
import cn.smartjavaai.cls.model.ClsModel;
import cn.smartjavaai.cls.model.ClsModelFactory;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.instanceseg.config.InstanceSegModelConfig;
import cn.smartjavaai.instanceseg.enums.InstanceSegModelEnum;
import cn.smartjavaai.instanceseg.model.InstanceSegModel;
import cn.smartjavaai.instanceseg.model.InstanceSegModelFactory;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 图像分类模型demo
 * @author dwj
 */
@Slf4j
public class ClsDemo {


    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        //将图片处理的底层引擎切换为 OpenCV
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }


    public ClsModel getModel(){
        ClsModelConfig config = new ClsModelConfig();
        //切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(ClsModelEnum.YOLOV8);
        //模型所在路径，synset.txt也需要放在同目录下
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/cls/yolo11m-cls.onnx");
        // 指定允许的类别
//        config.setAllowedClasses(Arrays.asList("dog","car"));
        //指定返回检测数量
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        return ClsModelFactory.getInstance().getModel(config);
    }

    /**
     * 实例分割
     */
    @Test
    public void detect(){
        try {
            ClsModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/clip/dog.jpg"));
            R<Classifications> result = detectorModel.detect(image);
            if(result.isSuccess()){
                if(CollectionUtils.isNotEmpty(result.getData().getClassNames())){
                    //分数最高分类
                    log.info("分类识别结果：{}", result.getData().best().toString());
                    //按分数排序前5个结果
//                log.info("动作识别结果：{}", result.getData().topK(5).toString());
                }else{
                    log.info("未识别到分类");
                }
            }else{
                log.info("分类识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
