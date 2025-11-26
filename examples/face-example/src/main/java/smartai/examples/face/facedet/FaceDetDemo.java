package smartai.examples.face.facedet;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.util.JsonUtils;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
import cn.smartjavaai.face.utils.FaceUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import smartai.examples.face.ViewerFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 人脸检测模型demo
 * 模型下载地址：https://pan.baidu.com/s/1d2YlJ2YOdGn3Y-AegyAhmQ?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class FaceDetDemo {


    public static String imgPath = "src/main/resources/iu_1.jpg";

    @BeforeClass
    public static void beforeAll() throws IOException {
        //将图片处理的底层引擎切换为 OpenCV
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }


    /**
     * 获取人脸检测模型（均衡模型）
     * 均衡模型：兼顾速度和精度
     * 注意事项：SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
     * @return
     */
    public FaceDetModel getFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.MTCNN);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/mtcnn");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }


    /**
     * 获取人脸检测模型（高精度模型）
     * 注意事项：高精度模型，识别准确度高，速度慢
     * @return
     */
    public FaceDetModel getProFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.RETINA_FACE);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/face_model/retinaface.pt");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取人脸检测模型（极速模型）
     * 注意事项：极速模型，识别准确度低，速度快
     * @return
     */
    public FaceDetModel getFastFaceDetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //人脸检测模型，SmartJavaAI提供了多种模型选择(更多模型，请查看文档)，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(FaceDetModelEnum.YOLOV5_FACE_320);
        //下载模型并替换本地路径，下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
        config.setModelPath("/Users/wenjie/Documents/develop/model/face_model/yolo-face/yolov5face-n-0.5-320x320.onnx");
        //只返回相似度大于该值的人脸,需要根据实际情况调整，分值越大越严格容易漏检，分值越小越宽松容易误识别
        config.setConfidenceThreshold(0.5f);
        //用于去除重复的人脸框，当两个框的重叠度超过该值时，只保留一个
        config.setNmsThresh(FaceDetectConstant.NMS_THRESHOLD);
        return FaceDetModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取Seetaface6 人脸检测模型
     * 注意：不支持macos
     * @return
     */
    public FaceDetModel getSeetaface6DetModel(){
        FaceDetConfig config = new FaceDetConfig();
        //指定模型
        config.setModelEnum(FaceDetModelEnum.SEETA_FACE6_MODEL);
        //指定模型路径：请根据实际情况替换为本地模型文件的绝对路径（下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234）
        config.setModelPath("C:/Users/Administrator/Downloads/sf3.0_models/sf3.0_models");
        config.setConfidenceThreshold(0.9);
        return FaceDetModelFactory.getInstance().getModel(config);
    }


    /**
     * 人脸检测
     * 注意事项：
     * 1、此用例使用均衡模型，可以切换高精度模型或极速模型
     */
    @Test
    public void testFaceDetect(){
        try {
            FaceDetModel faceModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(imgPath);
            R<DetectionResponse> detectedResult = faceModel.detect(image);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
                //裁剪人脸保存
                for (DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()) {
                    Image faceImage = FaceUtils.cropFace(image, detectionInfo.getDetectionRectangle());
                    ImageUtils.save(faceImage, "output/face_" + detectionInfo.getDetectionRectangle().getX() + "_" + detectionInfo.getDetectionRectangle().getY() + ".jpg");
                }
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 人脸检测并绘制人脸框
     */
    @Test
    public void testFaceDetectAndDraw(){
        try {
            FaceDetModel faceModel = getFaceDetModel();
            R<DetectionResponse> detectedResult = faceModel.detectAndDraw("src/main/resources/largest_selfie.jpg","output/largest_selfie_detected.png");
            if(detectedResult.isSuccess()){
                log.info("人脸检测成功：{}", JsonUtils.toJson(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 人脸检测并绘制人脸框,返回BufferedImage
     *
     */
    @Test
    public void testFaceDetectAndDraw2(){
        try {
            FaceDetModel faceModel = getFaceDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(imgPath);
            R<DetectionResponse> detectionResponseR = faceModel.detectAndDraw(image);
            if(detectionResponseR.isSuccess()){
                log.info("人脸检测成功：{}", JsonUtils.toJson(detectionResponseR.getData()));
                ImageUtils.save(detectionResponseR.getData().getDrawnImage(), "output/iu_1_detect.png");
            }else{
                log.info("人脸检测失败：{}", detectionResponseR.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * 人脸检测(Seetaface6)
     * 注意事项：不支持macos
     */
    @Test
    public void testFaceDetectSeetaface6(){
        try {
            FaceDetModel faceModel = getSeetaface6DetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(imgPath);
            R<DetectionResponse> detectedResult = faceModel.detect(image);
            if(detectedResult.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectedResult.getData()));
            }else{
                log.info("人脸检测失败：{}", detectedResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 摄像头人脸检测
     * 注意事项：实时检测，需要使用极速模型
     */
    @Test
    public void testDetectCamera(){
        try {
            FaceDetModel faceModel = getFastFaceDetModel();
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
                R<DetectionResponse> detectedResult = faceModel.detect(img);
                if(!detectedResult.isSuccess()){
                    log.debug("识别失败：{}", detectedResult.getMessage());
                    continue;
                }
                for(DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()){
                    DetectionRectangle detectionRectangle = detectionInfo.getDetectionRectangle();
                    String text = null;
                    if(detectionInfo.getScore() > 0){
                        text = detectionInfo.getScore() + "";
                    }
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
