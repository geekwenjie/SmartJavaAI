package smartai.examples.ocr.plate;

import ai.djl.modality.cv.Image;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.config.PlateRecModelConfig;
import cn.smartjavaai.ocr.entity.PlateInfo;
import cn.smartjavaai.ocr.enums.PlateDetModelEnum;
import cn.smartjavaai.ocr.enums.PlateRecModelEnum;
import cn.smartjavaai.ocr.factory.PlateModelFactory;
import cn.smartjavaai.ocr.model.plate.PlateDetModel;
import cn.smartjavaai.ocr.model.plate.PlateRecModel;
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
 * 车牌识别demo
 * 模型下载地址：https://pan.baidu.com/s/1YEP56UqYcL-Op80M6JAreA?pwd=1234 提取码: 1234
 * 开发文档：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class PlateRecDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取车牌检测模型
     * @return
     */
    public PlateDetModel getPlateDetModel() {
        PlateDetModelConfig config = new PlateDetModelConfig();
        //车牌检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(PlateDetModelEnum.YOLOV5);
        //下载模型并替换本地路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/plate/yolov5_plate_detect.onnx");
        config.setDevice(device);
        return PlateModelFactory.getInstance().getDetModel(config);
    }

    /**
     * 获取车牌识别模型
     * @return
     */
    public PlateRecModel getPlateRecModel() {
        PlateRecModelConfig recModelConfig = new PlateRecModelConfig();
        //车牌识别模型，切换模型需要同时修改modelEnum及modelPath
        recModelConfig.setModelEnum(PlateRecModelEnum.PLATE_REC_CRNN);
        //下载模型并替换本地路径
        recModelConfig.setModelPath("/Users/wenjie/Documents/develop/model/plate/plate_rec_color.onnx");
        //指定车牌检测模型
        recModelConfig.setPlateDetModel(getPlateDetModel());
        recModelConfig.setDevice(device);
        return PlateModelFactory.getInstance().getRecModel(recModelConfig);
    }

    /**
     * 车牌识别
     */
    @Test
    public void testDetect() throws IOException {
        PlateRecModel plateRecModel = getPlateRecModel();
        //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
        Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/plate/Quicker_20220930_180856.png");
        //识别车号
        R<List<PlateInfo>> result = plateRecModel.recognize(image);
        if(result.isSuccess()){
            log.info("车牌识别结果：{}", JsonUtils.toJson(result.getData()));
        }else{
            log.error("车牌识别失败：{}", result.getMessage());
        }
    }

    /**
     * 车牌识别及绘制结果
     */
    @Test
    public void recognizeAndDraw() {
        PlateRecModel plateRecModel = getPlateRecModel();
        //识别车号并绘制结果
        R<Void> result = plateRecModel.recognizeAndDraw("src/main/resources/plate/single_green.jpg", "output/plate_recognized2.jpg");
        if(result.isSuccess()){
            log.info("车牌识别成功");
        }else{
            log.error("车牌识别失败：{}", result.getMessage());
        }

    }

    /**
     * 车牌识别及绘制结果
     */
    @Test
    public void recognizeAndDraw2() {
        try {
            PlateRecModel plateRecModel = getPlateRecModel();
            String imagePath = "src/main/resources/plate/Quicker_20220930_180856.png";
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(imagePath);
            //可以根据后续业务场景使用detectedImage
            R<Image> detectedImage = plateRecModel.recognizeAndDraw(image);
            if(detectedImage.isSuccess()){
                log.info("车牌识别成功");
                ImageUtils.save(detectedImage.getData(), "output/plate_recognized3.jpg");
            }else{
                log.error("车牌识别失败：{}", detectedImage.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
