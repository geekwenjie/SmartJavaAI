package smartai.examples.ocr.idcard;

import ai.djl.modality.cv.Image;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.IdCardBackInfo;
import cn.smartjavaai.ocr.entity.IdCardFrontInfo;
import cn.smartjavaai.ocr.entity.IdCardInfo;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.idcard.DefaultIdCardRecognizer;
import cn.smartjavaai.ocr.idcard.IdCardPreprocessListener;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 身份证识别 demo
 * 使用说明：
 * 1、先下载 OCR 模型
 * 2、把下面的模型路径改成你自己的本地路径
 * 3、把身份证图片路径改成你自己的图片路径
 * 4、优先运行 recognizeFront() / recognizeBack() 查看结构化结果
 *
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * 开发文档：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class IdCardRecDemo {

    // 设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    // 下载模型后，请替换成你自己的模型路径
    private static final String DET_MODEL_PATH =
            "/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx";
    private static final String REC_MODEL_PATH =
            "/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx";
    private static final String DIRECTION_MODEL_PATH =
            "/Users/wenjie/Documents/develop/model/ocr/PP-LCNet_x0_25_textline_ori_infer/PP-LCNet_x0_25_textline_ori_infer.onnx";

    // 这里改成你自己的身份证图片路径
    private static final String FRONT_IMAGE_PATH = "src/main/resources/idcard/idcard_front1.png";
    private static final String BACK_IMAGE_PATH = "src/main/resources/idcard/idcard_back1.png";

    @BeforeClass
    public static void beforeAll() throws IOException {
        // 修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取文本检测模型
     * @return
     */
    public OcrCommonDetModel getDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        // 文本检测模型，切换模型需要同时修改 modelEnum 及 modelPath
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V4_SERVER_DET_MODEL);
        // 下载模型并替换本地路径
        config.setDetModelPath(DET_MODEL_PATH);
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
    }

    /**
     * 获取方向检测模型
     * @return
     */
    public OcrDirectionModel getDirectionModel() {
        DirectionModelConfig directionModelConfig = new DirectionModelConfig();
        // 行文本方向检测模型，切换模型需要同时修改 modelEnum 及 modelPath
        directionModelConfig.setModelEnum(DirectionModelEnum.PP_LCNET_X0_25);
        // 下载模型并替换本地路径
        directionModelConfig.setModelPath(DIRECTION_MODEL_PATH);
        directionModelConfig.setTextDetModel(getDetectionModel());
        directionModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
    }

    /**
     * 获取 OCR 识别模型
     * @return
     */
    public OcrCommonRecModel getRecModel() {
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        // 文本识别模型，切换模型需要同时修改 modelEnum 及 modelPath
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL);
        // 下载模型并替换本地路径
        recModelConfig.setRecModelPath(REC_MODEL_PATH);
        recModelConfig.setTextDetModel(getDetectionModel());
        recModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }

    /**
     * 创建身份证识别器
     * @return
     */
    public DefaultIdCardRecognizer getIdCardRecognizer() {
        return new DefaultIdCardRecognizer()
                .setRecModel(getRecModel())
                // 身份证识别建议按行返回，方向矫正交给预处理逻辑处理
                .setRecOptions(new OcrRecOptions(false, true))
                .setDirectionModel(getDirectionModel())
                .setEnablePreprocess(true);
    }

    /**
     * 身份证正面识别
     * 可识别：姓名、性别、民族、出生日期、住址、身份证号
     */
    @Test
    public void recognizeFront() {
        try {
            DefaultIdCardRecognizer recognizer = getIdCardRecognizer();
            Image image = SmartImageFactory.getInstance().fromFile(FRONT_IMAGE_PATH);
            IdCardFrontInfo result = recognizer.recognizeFront(image);
            log.info("身份证正面识别结果：{}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("身份证正面识别失败", e);
        }
    }

    /**
     * 身份证反面识别
     * 可识别：签发机关、有效期开始时间、有效期结束时间
     */
    @Test
    public void recognizeBack() {
        try {
            DefaultIdCardRecognizer recognizer = getIdCardRecognizer();
            Image image = SmartImageFactory.getInstance().fromFile(BACK_IMAGE_PATH);
            IdCardBackInfo result = recognizer.recognizeBack(image);
            log.info("身份证反面识别结果：{}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("身份证反面识别失败", e);
        }
    }

    /**
     * 同时识别身份证正反面
     */
    @Test
    public void recognizeBoth() {
        try {
            DefaultIdCardRecognizer recognizer = getIdCardRecognizer();
            Image frontImage = SmartImageFactory.getInstance().fromFile(FRONT_IMAGE_PATH);
            Image backImage = SmartImageFactory.getInstance().fromFile(BACK_IMAGE_PATH);
            IdCardInfo result = recognizer.recognizeBoth(frontImage, backImage);
            log.info("身份证正反面识别结果：{}", JsonUtils.toJson(result));
        } catch (Exception e) {
            log.error("身份证正反面识别失败", e);
        }
    }

    /**
     * 身份证识别并输出调试图片
     * 适合排查“为什么没识别出来”，不适合生产环境使用
     */
    @Test
    public void recognizeFrontWithDebugImages() {
        try {
            Path debugDir = Paths.get("output/idcard_debug");
            Files.createDirectories(debugDir);
            String filePrefix = "idcard_front";
            DefaultIdCardRecognizer recognizer = getIdCardRecognizer()
//                    .setPreprocessListener(createDebugListener(debugDir, "idcard_front"));
                    .setPreprocessListener(new IdCardPreprocessListener() {
                @Override
                public void onAfterDirection(Image image) {
                    saveDebugImage(image, debugDir.resolve(filePrefix + "_step1_direction.png"));
                }

                @Override
                public void onAfterRecognize(Image image, OcrInfo ocrInfo) {
                    saveDebugImage(image, debugDir.resolve(filePrefix + "_step2_ocr_boxes.png"));
                }
            });

            Image image = SmartImageFactory.getInstance().fromFile(FRONT_IMAGE_PATH);
            IdCardFrontInfo result = recognizer.recognizeFront(image);
            log.info("身份证正面识别结果：{}", JsonUtils.toJson(result));
            log.info("调试图片已输出到：{}", debugDir.toAbsolutePath());
        } catch (Exception e) {
            log.error("身份证调试识别失败", e);
        }
    }

    private void saveDebugImage(Image image, Path outputPath) {
        try {
            ImageUtils.save(image, outputPath, "png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
