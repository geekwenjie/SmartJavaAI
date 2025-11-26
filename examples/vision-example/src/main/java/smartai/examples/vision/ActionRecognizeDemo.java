package smartai.examples.vision;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.action.config.ActionRecModelConfig;
import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.action.model.ActionRecModel;
import cn.smartjavaai.action.model.ActionRecModelFactory;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 动作识别Demo
 * 模型下载地址：https://pan.baidu.com/s/17doY4pgZM9EbtSIaoCWWCA?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class ActionRecognizeDemo {

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
     * 获取动作识别模型
     * 注意事项：
     * 1、不同模型支持的动作类别不同，请查看文档：http://doc.smartjavaai.cn
     */
    public ActionRecModel getModel(){
        ActionRecModelConfig config = new ActionRecModelConfig();
        //动作识别模型切换时，需要同时更新 modelEnum 和 modelPath。其中部分 modelEnum 对应多个模型文件，可通过指定 modelPath 来选择具体的模型。
        config.setModelEnum(ActionRecModelEnum.INCEPTIONV3_KINETICS400_ONNX);
        //模型所在路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/action/gluoncv-inceptionv3_kinetics400-695477a5.onnx");
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        //指定允许的类别
//        config.setAllowedClasses(Arrays.asList("dancing_ballet"));
        return ActionRecModelFactory.getInstance().getModel(config);
    }



    /**
     * 动作识别
     * 注意事项：
     * 1、不同模型支持的动作类别不同，请查看文档：http://doc.smartjavaai.cn
     * 2、图片中应该只包含单一动作人物
     * 3、动作识别，只做图片分类，并不做人物定位
     */
    @Test
    public void actionRecognition(){
        try {
            ActionRecModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/action/dance.jpg"));
            R<Classifications> result = detectorModel.detect(image);
            if(result.isSuccess()){
                if(CollectionUtils.isNotEmpty(result.getData().getClassNames())){
                    //分数最高分类
                    log.info("动作识别结果：{}", result.getData().best().toString());
                    //按分数排序前5个结果
//                log.info("动作识别结果：{}", result.getData().topK(5).toString());
                }else{
                    log.info("未识别到动作");
                }
            }else{
                log.info("动作识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
