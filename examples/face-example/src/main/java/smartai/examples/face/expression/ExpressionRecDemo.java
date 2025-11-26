package smartai.examples.face.expression;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.ExpressionResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.FacialExpression;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceExpressionConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.ExpressionModelEnum;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.ExpressionModelFactory;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.model.expression.ExpressionModel;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
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
import java.util.List;

/**
 * 表情识别demo
 * 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class ExpressionRecDemo {

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
     * 获取表情识别模型
     * @return
     */
    public ExpressionModel getExpressionModel(){
        FaceExpressionConfig config = new FaceExpressionConfig();
        config.setModelEnum(ExpressionModelEnum.FrEmotion);
        config.setModelPath("/Users/wenjie/Documents/develop/model/emotion/fr_expression.onnx");
        config.setDevice(device);
        config.setAlign(true);
        config.setDetectModel(getFaceDetModel());
        return ExpressionModelFactory.getInstance().getModel(config);
    }

    /**
     * 表情识别（单人脸）
     * 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）
     */
    @Test
    public void testExpressionDetect() {
        try {
            ExpressionModel model = getExpressionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/emotion/happy.png");
            R<ExpressionResult> result = model.detectTopFace(image);
            if(result.isSuccess()){
                log.info("识别结果：{}", JSONObject.toJSONString(result.getData().getExpression().getDescription()));
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 表情识别（多人脸）
     * 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）
     */
    @Test
    public void testExpressionDetect2() {
        try {
            ExpressionModel model = getExpressionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/emotion/happy.png");
            R<DetectionResponse> result = model.detect(image);
            if(result.isSuccess()){
                //log.info("识别结果：{}", JSONObject.toJSONString(result.getData()));
                for (DetectionInfo detectionInfo : result.getData().getDetectionInfoList()) {
                    log.info("识别结果：{}", JSONObject.toJSONString(detectionInfo.getFaceInfo().getExpressionResult().getExpression().getDescription()));
                }
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 表情识别(基于人脸检测检测框-多人)
     * 流程：人脸检测 -》表情识别
     * 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）
     */
    @Test
    public void testExpressionDetect3() {
        try {
            FaceDetModel faceDetModel = getFaceDetModel();
            ExpressionModel model = getExpressionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/emotion/happy.png");
            R<DetectionResponse> detResult = faceDetModel.detect(image);
            if(detResult.isSuccess()){
                R<List<ExpressionResult>> result = model.detect(image, detResult.getData());
                if(result.isSuccess()){
                    result.getData().forEach(expressionResult -> {
                        log.info("识别结果：{}", JSONObject.toJSONString(expressionResult.getExpression().getDescription()));
                    });
                }else{
                    log.info("识别失败：{}", result.getMessage());
                }
            }else{
                log.info("人脸检测失败：{}", detResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 表情识别(基于人脸检测检测框-单人)
     * 流程：人脸检测 -》表情识别
     * 支持识别7种表情：neutral（中性）、happy（高兴）、sad（悲伤）、surprise（惊讶）、fear（恐惧）、disgust（厌恶）、anger（愤怒）
     */
    @Test
    public void testExpressionDetect4() {
        try {
            FaceDetModel faceDetModel = getFaceDetModel();
            ExpressionModel model = getExpressionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/emotion/happy.png");
            R<DetectionResponse> detResult = faceDetModel.detect(image);
            if(detResult.isSuccess()){
                for (DetectionInfo detectionInfo : detResult.getData().getDetectionInfoList()) {
                    R<ExpressionResult> result = model.detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
                    if(result.isSuccess()){
                        log.info("识别结果：{}", JSONObject.toJSONString(result.getData().getExpression().getDescription()));
                    }else{
                        log.info("识别失败：{}", result.getMessage());
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detResult.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图片活体检测并绘制结果
     */
    @Test
    public void testExpressionDetectAndDraw(){
        try {
            ExpressionModel model = getExpressionModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/emotion/surprise.png");
            R<DetectionResponse> result = model.detect(image);
            if(result.isSuccess()){
                //log.info("识别结果：{}", JSONObject.toJSONString(result.getData()));
                for (DetectionInfo detectionInfo : result.getData().getDetectionInfoList()) {
                    log.info("识别结果：{}", JSONObject.toJSONString(detectionInfo.getFaceInfo().getExpressionResult().getExpression().getDescription()));
                    ImageUtils.drawRectAndText(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getExpressionResult().getExpression().getDescription());
                }
                ImageUtils.save(image, "output/detect.jpg");
            }else{
                log.info("识别失败：{}", result.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 摄像头表情识别
     * 注意事项：如果视频比较卡，可以使用轻量的人脸检测模型
     */
//    @Test
    public void testExpressionDetectCamera(){
        try {
            ExpressionModel expressionModel = getExpressionModel();
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
                R<DetectionResponse> detectedResult = expressionModel.detect(img);
                if(!detectedResult.isSuccess()){
                    log.debug("识别失败：{}", detectedResult.getMessage());
                    continue;
                }
                for(DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()){
                    DetectionRectangle detectionRectangle = detectionInfo.getDetectionRectangle();
                    String text = detectionInfo.getFaceInfo().getExpressionResult().getExpression().getLabel() + ":" + detectionInfo.getFaceInfo().getExpressionResult().getScore();
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
