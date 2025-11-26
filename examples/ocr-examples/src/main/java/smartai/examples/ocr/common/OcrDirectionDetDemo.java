package smartai.examples.ocr.common;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * OCR 行文本方向检测 示例
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * 开发文档：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class OcrDirectionDetDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取方向检测模型
     * @return
     */
    public OcrDirectionModel getDirectionModel(){
        DirectionModelConfig directionModelConfig = new DirectionModelConfig();
        //指定行文本方向检测模型，切换模型需要同时修改modelEnum及modelPath
        directionModelConfig.setModelEnum(DirectionModelEnum.PP_LCNET_X0_25);
        //指定行文本方向检测模型路径，需要更改为自己的模型路径（下载地址请查看文档）
        directionModelConfig.setModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-LCNet_x0_25_textline_ori_infer/PP-LCNet_x0_25_textline_ori_infer.onnx");
        directionModelConfig.setDevice(device);
        directionModelConfig.setTextDetModel(getDetectionModel());
        return OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
    }

    /**
     * 获取文本检测模型
     * @return
     */
    public OcrCommonDetModel getDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_det_infer/PP-OCRv5_mobile_det_infer.onnx");
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
    }


    /**
     * 文本方向检测
     * 流程：文本检测 -> 方向分类
     * 检测图像中文字的整体方向
     * 支持返回四种可能的方向角度：0°, 90°, 180°, 270°
     * 模型需要放在单独文件夹
     */
    @Test
    public void detect(){
        try {
            OcrDirectionModel directionModel = getDirectionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/ocr_1.jpg");
            List<OcrItem> itemList = directionModel.detect(image);
            log.info("OCR方向检测结果1：{}", JSONObject.toJSONString(itemList));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 文本检测并绘制结果
     * 流程：文本检测 -> 方向分类
     * 检测图像中的文本区域，仅检测文本框位置，不识别文字内容
     * 模型需要放在单独文件夹
     */
    @Test
    public void detectAndDraw(){
        try {
            OcrDirectionModel directionModel = getDirectionModel();
            directionModel.detectAndDraw("src/main/resources/ocr_3.jpg",  "output/ocr_3_detected.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本检测并绘制结果
     * 流程：文本检测 -> 方向分类
     * 检测图像中的文本区域，仅检测文本框位置，不识别文字内容
     * 模型需要放在单独文件夹
     */
    @Test
    public void detectAndDraw2(){
        try {
            OcrDirectionModel directionModel = getDirectionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/ocr_1.jpg");
            Image resultImage = directionModel.detectAndDraw(image);
            ImageUtils.save(resultImage, "output/ocr_1_detected4.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
