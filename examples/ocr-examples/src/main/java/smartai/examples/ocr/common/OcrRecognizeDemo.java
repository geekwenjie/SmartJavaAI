package smartai.examples.ocr.common;

import ai.djl.modality.cv.Image;
import ai.djl.util.JsonUtils;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OCR 文本识别 示例
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * 开发文档：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class OcrRecognizeDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;


    @BeforeClass
    public static void beforeAll() throws IOException {
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
        //Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    /**
     * 获取通用识别模型（高精确度模型）
     * 注意事项：高精度模型，识别准确度高，速度慢
     * @return
     */
    public OcrCommonRecModel getProRecModel(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定文本识别模型，切换模型需要同时修改modelEnum及modelPath
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_SERVER_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_server_rec_infer/PP-OCRv5_server_rec.onnx");
        recModelConfig.setDevice(device);
        recModelConfig.setTextDetModel(getProDetectionModel());
        recModelConfig.setDirectionModel(getDirectionModel());
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }

    /**
     * 获取通用识别模型（极速模型）
     * 注意事项：极速模型，识别准确度低，速度快
     * @return
     */
    public OcrCommonRecModel getFastRecModel(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定文本识别模型，切换模型需要同时修改modelEnum及modelPath
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_rec_infer/PP-OCRv5_mobile_rec_infer.onnx");
        recModelConfig.setDevice(device);
        recModelConfig.setTextDetModel(getFastDetectionModel());
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }


    /**
     * 获取文本检测模型(极速模型)
     * 注意事项：极速模型，识别准确度低，速度快
     * @return
     */
    public OcrCommonDetModel getFastDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_det_infer/PP-OCRv5_mobile_det_infer.onnx");
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
    }

    /**
     * 获取文本检测模型(高精确度模型)
     * 注意事项：高精度模型，识别准确度高，速度慢
     * @return
     */
    public OcrCommonDetModel getProDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V5_SERVER_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_server_det_infer/PP-OCRv5_server_det.onnx");
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
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
        return OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
    }



    /**
     * 文本识别
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognize(){
        try {
            OcrCommonRecModel recModel = getFastRecModel();
            //不带方向矫正，分行返回文本
            OcrRecOptions options = new OcrRecOptions(false, true);
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/ocr_1.jpg");
            OcrInfo ocrInfo = recModel.recognize(image, options);
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 文本识别（手写字）
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognizeHandWriting(){
        try {
            OcrCommonRecModel recModel = getFastRecModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/handwriting_1.jpg");
            OcrInfo ocrInfo = recModel.recognize(image, new OcrRecOptions());
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本识别（带方向矫正）
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 本方法支持多角度文字识别
     * 流程：文本检测 -> 方向检测 -> 方向矫正 ->  文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognize2(){
        try {
            OcrCommonRecModel recModel = getFastRecModel();
            //带方向矫正，分行返回文本
            OcrRecOptions options = new OcrRecOptions(true, true);
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/ocr_3.jpg");
            OcrInfo ocrInfo = recModel.recognize(image, options);
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 文本识别并绘制结果
     * 支持简体中文、繁体中文、英文、日文四种主要语言，以及手写、竖版、拼音、生僻字
     * 流程：文本检测 -> 文本识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognizeAndDraw(){
        try {
            OcrCommonRecModel recModel = getFastRecModel();
            int fontSize = 18;
            recModel.recognizeAndDraw("src/main/resources/general_ocr_002.png", "output/ocr_4_recognized.jpg", fontSize, new OcrRecOptions());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void recognizeAndDraw2(){
        try {
            OcrCommonRecModel recModel = getFastRecModel();
            int fontSize = 18;
            //创建保存路径
            Path inputImagePath = Paths.get("src/main/resources/general_ocr_002.png");
            Path imageOutputPath = Paths.get("output/ocr_5_recognized.jpg");
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(inputImagePath);
            OcrInfo ocrInfo = recModel.recognizeAndDraw(image, fontSize, new OcrRecOptions());
            log.info("OCR识别结果：{}", JSONObject.toJSONString(ocrInfo));
            //保存绘制结果
            if(ocrInfo != null && ocrInfo.getDrawnImage() != null){
                ImageUtils.save(ocrInfo.getDrawnImage(), imageOutputPath.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 批量识别
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void batchRecognize(){
        try {
            OcrCommonRecModel recModel = getFastRecModel();
            //批量检测要求图片宽高一致
            String folderPath = "/Users/xxx/Downloads/testing33";
            //读取文件夹中所有图片
            List<Image> images = ImageUtils.readImagesFromFolder(folderPath);
            //带方向矫正，分行返回文本
            OcrRecOptions options = new OcrRecOptions(true, true);
            List<OcrInfo> ocrResult = recModel.batchRecognizeDJLImage(images, options);
            for(int i = 0; i < ocrResult.size(); i++){
                log.info("图片" + i + "文本识别结果：{}", JSONObject.toJSONString(ocrResult.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 多线程文本识别
     * 注意事项：
     * 1、模型在外层统一创建，多个线程共享同一个模型实例，避免重复加载模型。
     * 2、每个线程内部单独创建 Image 对象，避免共享图片对象带来的线程安全问题。
     */
    @Test
    public void multiThreadRecognize() {
        ExecutorService executorService = null;
        try {
            final OcrCommonRecModel recModel = getFastRecModel();
            final OcrRecOptions options = new OcrRecOptions(false, true);
            final String imagePath = "src/main/resources/ocr_1.jpg";

            int threadCount = 5;
            int taskCount = 20;
            CountDownLatch countDownLatch = new CountDownLatch(taskCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            List<String> errorMessages = new ArrayList<>();
            executorService = Executors.newFixedThreadPool(threadCount);

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < taskCount; i++) {
                final int taskIndex = i;
                executorService.submit(() -> {
                    try {
                        Image image = SmartImageFactory.getInstance().fromFile(imagePath);
                        OcrInfo ocrInfo = recModel.recognize(image, options);
                        successCount.incrementAndGet();
                        log.info("线程：{}，任务：{}，识别结果：{}",
                                Thread.currentThread().getName(),
                                taskIndex,
                                JSONObject.toJSONString(ocrInfo));
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        String errorMsg = "任务" + taskIndex + "识别失败：" + e.getMessage();
                        synchronized (errorMessages) {
                            errorMessages.add(errorMsg);
                        }
                        log.error(errorMsg, e);
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            countDownLatch.await();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("多线程识别完成，总任务数：{}，成功：{}，失败：{}，耗时：{} ms",
                    taskCount, successCount.get(), failCount.get(), costTime);

            if (!errorMessages.isEmpty()) {
                log.error("失败详情：{}", JsonUtils.toJson(errorMessages));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }




}
