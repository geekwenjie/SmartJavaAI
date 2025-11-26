package smartai.examples.vision;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.util.JsonUtils;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.UUID;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.VideoSourceType;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.model.DetectorModel;
import cn.smartjavaai.objectdetection.model.ObjectDetectionModelFactory;
import cn.smartjavaai.objectdetection.stream.StreamDetectionListener;
import cn.smartjavaai.objectdetection.stream.StreamDetector;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 目标检测模型demo
 * 模型下载地址：https://pan.baidu.com/s/10aTOLBlR6EG-sq6g0OkAWg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class ObjectDetectionDemo {


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
     * 获取目标检测模型
     * 注意事项：
     * 1、更多模型请查看文档：http://doc.smartjavaai.cn/objectdetect.html
     */
    public DetectorModel getModel(){
        DetectorModelConfig config = new DetectorModelConfig();
        //目标检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(DetectorModelEnum.YOLOV12_OFFICIAL_ONNX);
        //模型所在路径，synset.txt也需要放在同目录下
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/object/yolov12/yolov12n.onnx");
        // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person","car"));
        //指定返回检测数量
        config.setTopK(100);
        config.setDevice(device);
        //置信度阈值
        config.setThreshold(0.5f);
        return ObjectDetectionModelFactory.getInstance().getModel(config);
    }



    /**
     * 目标检测
     */
    @Test
    public void objectDetection(){
        try {
            DetectorModel detectorModel = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/object_detection.jpg");
            DetectionResponse detectionResponse = detectorModel.detect(image);
            log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 目标检测并绘制检测结果
     */
    @Test
    public void objectDetectionAndDraw(){
        try {
            DetectorModel detectorModel = getModel();
            detectorModel.detectAndDraw("src/main/resources/object_detection.jpg","output/object_detection_detected.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 目标检测并绘制检测结果,返回BufferedImage
     */
    @Test
    public void objectDetectionAndDraw2(){
        try {
            DetectorModel detectorModel = getModel();
            String imagePath = "src/main/resources/object_detection.jpg";
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(imagePath);
            //可以根据后续业务场景使用detectedImage
            DetectionResponse detectionResponse = detectorModel.detectAndDraw(image);
            log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
            if(detectionResponse != null && detectionResponse.getDrawnImage() != null){
                ImageUtils.save(detectionResponse.getDrawnImage(), "output/object_detection_detected2.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 使用自己训练的模型检测
     */
    @Test
    public void objectDetectionWithCustomModel(){
        try {
            DetectorModelConfig config = new DetectorModelConfig();
            //目标检测模型，切换模型需要同时修改modelEnum及modelPath
            config.setModelEnum(DetectorModelEnum.YOLOV12_CUSTOM_ONNX);
            //模型所在路径，synset.txt也需要放在同目录下(分类文件，具体请看文档：http://doc.smartjavaai.cn/objectdetect.html#%E4%BD%BF%E7%94%A8%E8%87%AA%E5%B7%B1%E8%AE%AD%E7%BB%83%E7%9A%84%E6%A8%A1%E5%9E%8B%E6%A3%80%E6%B5%8B)
            config.setModelPath("/Users/xxx/Documents/develop/fire_model/best.onnx");
            //模型训练时图片宽度
            config.putCustomParam("width", 640);//resize 宽
            //模型训练时图片高度
            config.putCustomParam("height", 640);// resize 高
            config.putCustomParam("nmsThreshold", 0.5f);
            // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person"));
            //指定返回检测数量
            config.setTopK(100);
            config.setDevice(device);
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
            DetectionResponse detect = detectorModel.detect("src/main/resources/dog_bike_car.jpg");
            log.info("目标检测结果：{}", JSONObject.toJSONString(detect));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * tensorflow2目标检测
     * 注意事项：
     * 1、百度网盘只提供部分模型，更多tensorflow模型可以前往官网下载：https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/tf2_detection_zoo.md
     */
    @Test
    public void objectDetection3(){
        try {
            DetectorModelConfig config = new DetectorModelConfig();
            //指定模型枚举，可以通过modelPath指定不同tensorflow模型
            config.setModelEnum(DetectorModelEnum.TENSORFLOW2_OFFICIAL);
            //模型路径，需解压模型压缩包，可以通过modelPath指定不同tensorflow模型
            config.setModelPath("/Users/wenjie/Documents/develop/model/tensorflow/ssd_mobilenet_v2_320x320_coco17_tpu-8");
//            config.putCustomParam("synsetUrl", "https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/data/mscoco_label_map.pbtxt");
//            config.putCustomParam("synsetPath", "/Users/wenjie/Downloads/mscoco_label_map.pbtxt.txt");
            //分类文件，需下载放入模型路径下
            config.putCustomParam("synsetFileName", "mscoco.pbtxt");
            // 指定允许的类别
//            config.setAllowedClasses(Arrays.asList("person"));
            //指定返回检测数量
            config.setTopK(100);
            config.setDevice(device);
            DetectorModel detectorModel = ObjectDetectionModelFactory.getInstance().getModel(config);
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/dog_bike_car.jpg");
            DetectionResponse detectionResponse = detectorModel.detect(image);
            log.info("目标检测结果：{}", JSONObject.toJSONString(detectionResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 视频流目标检测
     */
    @Test
    public void testStream(){
        StreamDetector detector = new StreamDetector.Builder()
                //视频源类型：支持视频流、本地摄像头、视频文件
                .sourceType(VideoSourceType.STREAM)
                //视频流地址，支持rtsp、rtmp、http等常见视频流
                .streamUrl("rtsp://username:password@ip:port/Streaming/Channels/101")
                //每隔多少帧检测一次（需要根据模型检测速度决定）
                .frameDetectionInterval(10)
                //目标检测模型
                .detectorModel(getModel())
                //回调函数：检测到指定目标时触发（getModel中可指定模型检测的物体）
                .listener(new StreamDetectionListener() {

                    /**
                     * 建议把耗时操作放到新线程里执行
                     * @param detectionInfoList 目标信息列表
                     * @param image 检测到的图片
                     */
                    @Override
                    public void onObjectDetected(List<DetectionInfo> detectionInfoList, Image image) {
                        log.info("时间：" + LocalDateTimeUtil.now().toString());
                        log.info("检测结果：{}", JsonUtils.toJson(detectionInfoList));
                        //绘制检测结果
                        ImageUtils.drawRectAndText(image, detectionInfoList);
                        //保存图片
                        ImageUtils.save(image, "test"+ UUID.fastUUID().toString() +".png","/Users/wenjie/Downloads");
                        if (image != null){
                            ImageUtils.releaseOpenCVMat(image);
                        }
                    }

                    @Override
                    public void onStreamEnded() {
                        log.info("视频流检测结束");
                    }

                    @Override
                    public void onStreamDisconnected() {
                        log.info("视频流断开连接");
                    }
                }).build();
        detector.startDetection();
        //阻塞主线程
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(); // 一直阻塞，直到被 countDown
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 本地摄像头目标检测
     */
    @Test
    public void testLocalCamera(){
        StreamDetector detector = new StreamDetector.Builder()
                //视频源类型：支持视频流、本地摄像头、视频文件
                .sourceType(VideoSourceType.CAMERA)
                //摄像头序号
                .cameraIndex(0)
                //每隔多少帧检测一次（需要根据模型检测速度决定）
                .frameDetectionInterval(5)
                //目标检测模型
                .detectorModel(getModel())
                //回调函数：检测到指定目标时触发（getModel中可指定模型检测的物体）
                .listener(new StreamDetectionListener() {
                    @Override
                    public void onObjectDetected(List<DetectionInfo> detectionInfoList, Image image) {
                        log.info("时间：" + LocalDateTimeUtil.now().toString());
                        log.info("检测结果：{}", JsonUtils.toJson(detectionInfoList));
                        //绘制检测结果
                        ImageUtils.drawRectAndText(image, detectionInfoList);
                        //保存图片
                        ImageUtils.save(image, "test"+ UUID.fastUUID().toString() +".png","/Users/wenjie/Downloads");
                        if (image != null){
                            ImageUtils.releaseOpenCVMat(image);
                        }
                    }

                    @Override
                    public void onStreamEnded() {
                        log.info("视频流检测结束");
                    }

                    @Override
                    public void onStreamDisconnected() {
                        log.info("视频流断开连接");
                    }
                }).build();
        detector.startDetection();
        //阻塞主线程
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(); // 一直阻塞，直到被 countDown
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 视频文件目标检测
     */
    @Test
    public void testVideoFile(){
        StreamDetector detector = new StreamDetector.Builder()
                //视频源类型：支持视频流、本地摄像头、视频文件
                .sourceType(VideoSourceType.FILE)
                //摄像头序号
                .streamUrl("girl.mp4")
                //每隔多少帧检测一次（需要根据模型检测速度决定）
                .frameDetectionInterval(5)
                //目标检测模型
                .detectorModel(getModel())
                //同物体重复检测时间间隔，单位s
                .repeatGap(5)
                //回调函数：检测到指定目标时触发（getModel中可指定模型检测的物体）
                .listener(new StreamDetectionListener() {
                    @Override
                    public void onObjectDetected(List<DetectionInfo> detectionInfoList, Image image) {
                        log.info("时间：" + LocalDateTimeUtil.now().toString());
                        log.info("检测结果：{}", JsonUtils.toJson(detectionInfoList));
                        //绘制检测结果
                        ImageUtils.drawRectAndText(image, detectionInfoList);
                        //保存图片
                        ImageUtils.save(image, "test"+ UUID.fastUUID().toString() +".png","/Users/wenjie/Downloads");
                        if (image != null){
                            ImageUtils.releaseOpenCVMat(image);
                        }
                    }

                    @Override
                    public void onStreamEnded() {
                        log.info("视频流检测结束");
                    }

                    @Override
                    public void onStreamDisconnected() {
                        log.info("视频流断开连接");
                    }
                }).build();
        detector.startDetection();
        //阻塞主线程
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(); // 一直阻塞，直到被 countDown
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 摄像头目标检测并实时预览
     * 注意事项：如果视频比较卡，可以使用更轻量的检测模型
     */
    @Test
    public void testDetectCamera(){
        try {
            DetectorModel detectorModel = getModel();
            OpenCV.loadShared();
            VideoCapture capture = new VideoCapture(0);
            if (!capture.isOpened()) {
                System.out.println("No camera detected");
                return;
            }

            double ratio =
                    capture.get(Videoio.CAP_PROP_FRAME_WIDTH)
                            / capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int height = (int) (screenSize.height * 0.65f);
            int width = (int) (height * ratio);
            if (width > screenSize.width) {
                width = screenSize.width;
            }

            Mat image = new Mat();
            boolean captured = false;
            for (int i = 0; i < 10; ++i) {
                captured = capture.read(image);
                if (captured) {
                    break;
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignore) {
                    // ignore
                }
            }
            if (!captured) {
                JOptionPane.showConfirmDialog(null, "Failed to capture image from WebCam.");
            }
            ViewerFrame frame = new ViewerFrame(width, height);
            SmartImageFactory factory = SmartImageFactory.getInstance();
            Size size = new Size(width, height);

            while (capture.isOpened()) {
                if (!capture.read(image)) {
                    break;
                }
                Mat resizeImage = new Mat();
                Imgproc.resize(image, resizeImage, size);
                Image img = factory.fromMat(resizeImage);
                DetectionResponse detectedResult = detectorModel.detect(img);
                if (Objects.isNull(detectedResult) || Objects.isNull(detectedResult.getDetectionInfoList()) || detectedResult.getDetectionInfoList().size() == 0){
                    log.debug("未检测到物体");
                    continue;
                }
                for(DetectionInfo detectionInfo : detectedResult.getDetectionInfoList()){
                    DetectionRectangle detectionRectangle = detectionInfo.getDetectionRectangle();
                    String text = detectionInfo.getObjectDetInfo().getClassName();
                    ImageUtils.drawRectAndText(img, detectionRectangle, text);
                }
                frame.showImage(ImageUtils.toBufferedImage(img));
            }
            capture.release();
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
