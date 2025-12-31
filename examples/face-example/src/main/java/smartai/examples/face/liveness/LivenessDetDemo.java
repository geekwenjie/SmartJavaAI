package smartai.examples.face.liveness;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import cn.hutool.core.lang.UUID;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.ExpressionResult;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.exception.FaceException;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.LivenessModelFactory;
import cn.smartjavaai.face.model.expression.ExpressionModel;
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
import java.util.List;

/**
 * 静态活体检测demo
 * 模型下载地址：https://pan.baidu.com/s/10l22x5fRz_gwLr8EAHa1Jg?pwd=1234 提取码: 1234
 * 文档地址：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class LivenessDetDemo {

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
     * 获取活体检测模型
     * @return
     */
    public LivenessDetModel getLivenessDetModel(){
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.IIC_FL_MODEL);
        config.setDevice(device);
        //需替换为实际模型存储路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/anti/IIC_Fl.onnx");
        //人脸活体阈值,可选,默认0.8，超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(LivenessConstant.DEFAULT_REALITY_THRESHOLD);
        /*视频检测帧数，可选，默认10，输出帧数超过这个number之后，就可以输出识别结果。
        这个数量相当于多帧识别结果融合的融合的帧数。当输入的帧数超过设定帧数的时候，会采用滑动窗口的方式，返回融合的最近输入的帧融合的识别结果。
        一般来说，在10以内，帧数越多，结果越稳定，相对性能越好，但是得到结果的延时越高。*/
        config.setFrameCount(LivenessConstant.DEFAULT_FRAME_COUNT);
        //视频最大检测帧数
        config.setMaxVideoDetectFrames(LivenessConstant.DEFAULT_MAX_VIDEO_DETECT_FRAMES);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        return LivenessModelFactory.getInstance().getModel(config);
    }

    /**
     * 获取活体检测模型(小视科技模型)
     * 备注：小视科技活体检测是两个模型融合结果
     * @return
     */
    public LivenessDetModel getMiniVisionLivenessDetModel(){
        LivenessConfig config = new LivenessConfig();
        config.setModelEnum(LivenessModelEnum.MINI_VISION_MODEL);
        config.setDevice(device);
        //模型1路径：需替换为实际模型存储路径
        config.setModelPath("/Users/wenjie/Documents/develop/model/live/2.7_80x80_MiniFASNetV2.onnx");
        //SE模型路径：需替换为实际模型存储路径
        config.putCustomParam("seModelPath", "/Users/wenjie/Documents/develop/model/live/4_0_0_80x80_MiniFASNetV1SE.onnx");
        //人脸活体阈值,可选,超过阈值则认为是真人，低于阈值是非活体
        config.setRealityThreshold(0.5f);
        /*视频检测帧数，可选，默认10，输出帧数超过这个number之后，就可以输出识别结果。
        这个数量相当于多帧识别结果融合的融合的帧数。当输入的帧数超过设定帧数的时候，会采用滑动窗口的方式，返回融合的最近输入的帧融合的识别结果。
        一般来说，在10以内，帧数越多，结果越稳定，相对性能越好，但是得到结果的延时越高。*/
        config.setFrameCount(LivenessConstant.DEFAULT_FRAME_COUNT);
        //视频最大检测帧数
        config.setMaxVideoDetectFrames(LivenessConstant.DEFAULT_MAX_VIDEO_DETECT_FRAMES);
        //指定人脸检测模型
        config.setDetectModel(getFaceDetModel());
        return LivenessModelFactory.getInstance().getModel(config);
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
     * 图片活体检测（多人脸）
     */
    @Test
    public void testLivenessDetect(){
        try {
            LivenessDetModel livenessDetModel = getLivenessDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/liveness/1.jpg");
            R<DetectionResponse> response = livenessDetModel.detect(image);
            if(response.isSuccess()){
                for (DetectionInfo detectionInfo : response.getData().getDetectionInfoList()){
                    log.info("活体检测结果：{}", JSONObject.toJSONString(detectionInfo));
                }
            }else{
                log.info("活体检测失败：{}", response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图片活体检测并绘制结果
     */
    @Test
    public void testLivenessDetectAndDraw(){
        try {
            LivenessDetModel livenessDetModel = getLivenessDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/liveness/1.jpg");
            R<DetectionResponse> response = livenessDetModel.detect(image);
            if(response.isSuccess()){
                for (DetectionInfo detectionInfo : response.getData().getDetectionInfoList()){
                    log.info("活体检测结果：{}", JSONObject.toJSONString(detectionInfo.getFaceInfo().getLivenessStatus().getStatus().getDescription()));
                    ImageUtils.drawRectAndText(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getLivenessStatus().getStatus().toString());
                    ImageUtils.save(image, "output/detect.jpg");
                }
            }else{
                log.info("活体检测失败：{}", response.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图片活体检测（分数最高人脸）
     */
    @Test
    public void testLivenessDetect2(){
        try {
            LivenessDetModel livenessDetModel = getLivenessDetModel();
            //指定文件夹路径
            File dir = new File("src/main/resources/liveness");
            File[] files = dir.listFiles();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            SmartImageFactory imageFactory = SmartImageFactory.getInstance();
            for (File file : files) {
                R<LivenessResult> response = livenessDetModel.detectTopFace(imageFactory.fromFile(file));
                if(response.isSuccess()){
                    log.info("{}活体检测结果：{},分数：{}", file.getName(), response.getData().getStatus().getDescription(), response.getData().getScore());
                }else{
                    log.info("{}活体检测失败：{}", file.getName(), response.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 图片多人脸活体检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testLivenessDetect3(){
        try {
            FaceDetModel faceDetectModel = getFaceDetModel();
            LivenessDetModel livenessDetModel = getLivenessDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/liveness/1.jpg");
            //人脸检测
            R<DetectionResponse> detectionResponse = faceDetectModel.detect(image);
            if(detectionResponse.isSuccess()){
                log.info("人脸检测结果：{}", JSONObject.toJSONString(detectionResponse.getData()));
                //检测到人脸
                if(detectionResponse.getData() != null && detectionResponse.getData().getDetectionInfoList() != null && detectionResponse.getData().getDetectionInfoList().size() > 0){
                    R<List<LivenessResult>> livenessResult = livenessDetModel.detect(image, detectionResponse.getData());
                    if(livenessResult.isSuccess()){
                        log.info("活体检测结果：{}", JSONObject.toJSONString(livenessResult.getData()));
                    }else{
                        log.error("活体检测失败：{}", livenessResult.getMessage());
                    }
                }else{
                    log.info("未检测到人脸");
                }
            }else{
                log.error("人脸检测失败：{}", detectionResponse.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图片单人脸活体检测（基于已检测出的人脸区域和关键点）
     */
    @Test
    public void testLivenessDetect4(){
        try {
            FaceDetModel faceDetModel = getFaceDetModel();
            LivenessDetModel livenessDetModel = getMiniVisionLivenessDetModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile("src/main/resources/liveness/1.jpg");
            R<DetectionResponse> detResult = faceDetModel.detect(image);
            if(detResult.isSuccess()){
                for (DetectionInfo detectionInfo : detResult.getData().getDetectionInfoList()) {
                    //seetaface6 需要有5点人脸关键点
                    //R<LivenessResult> result = livenessDetModel.detect(image, detectionInfo.getDetectionRectangle(), detectionInfo.getFaceInfo().getKeyPoints());
                    R<LivenessResult> result = livenessDetModel.detect(image, detectionInfo.getDetectionRectangle());
                    if(result.isSuccess()){
                        log.info("识别结果：{}", JSONObject.toJSONString(result.getData()));
                    }else{
                        log.info("识别失败：{}", result.getMessage());
                    }
                }
            }else{
                log.info("人脸检测失败：{}", detResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频活体检测
     */
    @Test
    public void testLivenessDetectVideo(){
        try {
            LivenessDetModel livenessDetModel = getLivenessDetModel();
            //视频路径
            R<LivenessResult> livenessStatus = livenessDetModel.detectVideo("/Users/wenjie/Documents/idea_workplace/SmartJavaAI-Demo/src/main/resources/girl.mp4");
            if (livenessStatus.isSuccess()){
                log.info("识别结果：{}", JSONObject.toJSONString(livenessStatus.getData()));
            }else{
                log.info("识别失败：{}", livenessStatus.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 摄像头活体检测
     * 注意事项：如果视频比较卡，可以使用轻量的人脸检测模型
     */
//    @Test
    public void testLivenessDetectCamera(){
        try {
            LivenessDetModel livenessDetModel = getLivenessDetModel();
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
                R<DetectionResponse> detectedResult = livenessDetModel.detect(img);
                if(!detectedResult.isSuccess()){
                    log.debug("识别失败：{}", detectedResult.getMessage());
                    continue;
                }
                for(DetectionInfo detectionInfo : detectedResult.getData().getDetectionInfoList()){
                    DetectionRectangle detectionRectangle = detectionInfo.getDetectionRectangle();
                    Color color = detectionInfo.getFaceInfo().getLivenessStatus().getStatus() == LivenessStatus.LIVE ? Color.GREEN : Color.RED;
                    String text = detectionInfo.getFaceInfo().getLivenessStatus().getStatus().getDescription() + ":" + detectionInfo.getFaceInfo().getLivenessStatus().getScore();
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
