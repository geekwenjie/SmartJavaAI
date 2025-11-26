package cn.smartjavaai.objectdetection.stream;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Mask;
import ai.djl.modality.cv.output.Rectangle;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.UUID;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.*;
import cn.smartjavaai.common.enums.VideoSourceType;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.model.DetectorModel;
import cn.smartjavaai.vision.utils.DetectorUtils;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.opencv.core.Mat;

import java.util.*;
import java.util.concurrent.*;

/**
 * 视频流目标检测器
 * @author dwj
 */
@Slf4j
public class StreamDetector implements AutoCloseable{

    static {
        OpenCV.loadLocally();
    }


    private DetectorModel detectorModel;
    private String streamUrl;
    //专门抓帧的线程
    private ExecutorService grabberExecutor;
    //专门处理帧的线程
    private ExecutorService processorExecutor;
    //回调线程池
    ExecutorService callbackExecutor;
    private int frameDetectionInterval = 1;
    private long repeatGap = 5; // 秒
    private volatile boolean isRunning;
    private FrameGrabber grabber;
    private StreamDetectionListener listener;
    private OpenCVFrameConverter.ToOrgOpenCvCoreMat converterToMat;
    private VideoSourceType sourceType = VideoSourceType.STREAM; // 默认流
    private int cameraIndex = 0; // 默认第一个摄像头

    private Map<String, Long> lastDetectTime = new ConcurrentHashMap<>();
    private BlockingQueue<Frame> frameQueue = new LinkedBlockingQueue<>(100);

    private GenericObjectPool<Predictor<Image, DetectedObjects>> predictorPool;

    private Predictor<Image, DetectedObjects> predictor;

    boolean grabberFinished = false; // 标记结束

    //空帧数量
    private int nullFrameCount = 0;


    // 连续多少次空帧认为断联
    private static final int MAX_NULL_FRAMES = 10;



    public static Builder builder() { return new Builder(); }

    private StreamDetector(Builder builder) {
        this.detectorModel = builder.detectorModel;
        this.streamUrl = builder.streamUrl;
        this.frameDetectionInterval = builder.frameDetectionInterval;
        this.listener = builder.listener;
        this.sourceType = builder.sourceType;
        this.cameraIndex = builder.cameraIndex;
        this.repeatGap = builder.repeatGap;
        this.converterToMat = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();
    }

    private void initializeGrabber() throws FrameGrabber.Exception {
        if (sourceType == VideoSourceType.CAMERA) {
            // 使用用户传入的摄像头索引
            grabber = new OpenCVFrameGrabber(cameraIndex);
        } else {
            grabber = new FFmpegFrameGrabber(streamUrl);
            if (sourceType == VideoSourceType.STREAM) {
                grabber.setOption("rtsp_transport", "tcp");
                grabber.setOption("buffer_size", "1024000");
                grabber.setOption("stimeout", "2000000");  // 超时：单位微秒，这里是2秒
                grabber.setOption("rw_timeout", "2000000"); // 读超时
                grabber.setOption("max_delay", "5000000");
                grabber.setOption("timeout", "2000000");    // 总超时
            }
        }
        //日志级别
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
        grabber.start();
        if(sourceType == VideoSourceType.FILE){
            // 总帧数
            int totalFrames = grabber.getLengthInFrames();
            log.info("视频帧数：{}", totalFrames);
        }
    }

    public void startDetection() {
        if (isRunning){
            throw new RuntimeException("当前正在运行中");
        }
        grabberFinished = false;
        //获取模型Predictor
        predictorPool = detectorModel.getPool();
        try {
            predictor = predictorPool.borrowObject();
        } catch (Exception e) {
            throw new DetectionException(e);
        }

        // 初始化抓帧线程池
        if (grabberExecutor == null || grabberExecutor.isShutdown())
            grabberExecutor = Executors.newFixedThreadPool(2);
        // 初始化帧处理线程池
        if (processorExecutor == null || processorExecutor.isShutdown())
            processorExecutor = Executors.newFixedThreadPool(2);
        if (callbackExecutor == null || callbackExecutor.isShutdown())
            callbackExecutor = Executors.newFixedThreadPool(4);
        try {
            initializeGrabber();
        } catch (FrameGrabber.Exception e) {
            throw new DetectionException("视频流检测启动失败", e);
        }
        isRunning = true;
        log.debug("视频流处理已启动");
        // 初始化抓帧线程
        grabberExecutor.submit(() -> {
            try {
                processFrames();
            } catch (Exception e) {
                log.error("视频流处理异常", e);
            } finally {
                //标识抓取已结束
                grabberFinished = true;
            }
        });
        // 初始化队列处理线程：解决回调比较耗时，导致线程池爆满
        startFrameProcessor();
    }

    /**
     * 负责抓取视频帧到队列
     */
    private void processFrames() {
        long frameCount = 0;
        while (!grabberFinished && isRunning) {
            try {
                Frame frame = grabber.grabFrame();
                //空帧
                if (frame == null) {
                    if(sourceType == VideoSourceType.FILE){
                        log.debug("视频检测结束");
                        grabberFinished = true;
                        break;
                    }else{
                        log.debug("未检测到视频帧");
                        nullFrameCount++;
                        if (nullFrameCount > MAX_NULL_FRAMES) {
                            log.warn("检测到视频断开，已超过最大空帧次数");
                            if(isRunning){
                                stopDetection();
                            }
                            if (listener != null) {
                                listener.onStreamDisconnected();
                            }
                            break;
                        }
                        continue;
                    }
                }else{
                    nullFrameCount = 0; // 只要拿到正常帧就清零
                    //非视频帧
                    if(frame.type != Frame.Type.VIDEO){
                        continue;
                    }
                }
                frameCount++;
                if (frameCount % frameDetectionInterval != 0) continue;
                Frame currentFrame = frame.clone();
                frameQueue.offer(currentFrame);
//                log.debug("正在抓取第{}帧，当前帧数：{}", frameCount, frameQueue.size());
            } catch (Exception e) {
                log.error("抓取视频帧异常", e);
            }
        }
        log.debug("抓取帧线程退出");
    }

    /**
     * 负责处理视频帧
     */
    private void startFrameProcessor() {
        processorExecutor.submit(() -> {
            log.debug("帧处理线程已启动");
            while ((!grabberFinished || !frameQueue.isEmpty()) && isRunning) {
                try {
                    Frame frame = frameQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (frame != null) {
                        processFrame(frame);
                    }
                } catch (InterruptedException e) {
                    log.debug("帧处理线程被中断，准备退出");
                    break;
                } catch (Exception e) {
                    log.error("帧处理异常", e);
                }
            }
            if(isRunning){
                stopDetection();
            }
            if (listener != null) {
                listener.onStreamEnded();
            }
            log.debug("帧处理线程退出");
        });

    }

    private void processFrame(Frame frame) {
        Mat mat = null;
        try {
            mat = converterToMat.convert(frame);
            if (mat == null) return;

            Image image = SmartImageFactory.getInstance().fromMat(mat);
            DetectedObjects detectedObjects = predictor.predict(image);
//            log.info("内部检测结果：{}", detectedObjects.toString());
            DetectionResponse detectionResponse = DetectorUtils.convertToDetectionResponse(detectedObjects, image);
            if(Objects.isNull(detectionResponse)){
                return;
            }
            List<DetectionInfo> filtered = filterRepeatedObjects(detectionResponse);
            if (!filtered.isEmpty() && listener != null) {
                Image copyImage = image.duplicate();
                callbackExecutor.submit(() -> listener.onObjectDetected(filtered, copyImage));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("单帧处理异常", e);
        }  finally {
            if (mat != null) mat.release();
        }
    }

    private List<DetectionInfo> filterRepeatedObjects(DetectionResponse response) {
        List<DetectionInfo> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (DetectionInfo info : response.getDetectionInfoList()) {
            String name = info.getObjectDetInfo().getClassName();
            Long last = lastDetectTime.get(name);
            if (last == null || (now - last) > repeatGap * 1000) {
                lastDetectTime.put(name, now);
                result.add(info);
            }
        }
        return result;
    }


    /**
     * 开始检测下一个视频文件
     */
    public void startNextVideo(String videoPath) {
        if(!grabberFinished){
            throw new DetectionException("当前视频未检测结束，请先关闭当前检测，再切换下一个视频");
        }
        if(sourceType != VideoSourceType.FILE){
            throw new DetectionException("sourceType不是文件");
        }
        this.streamUrl = videoPath;
        this.grabberFinished = false;
        startDetection();
    }

    public void stopDetection() {
        log.debug("停止检测中...");
        isRunning = false;
        grabberFinished = true;
        if (predictor != null && predictorPool != null) {
            try {
                predictorPool.returnObject(predictor); //归还
                predictor = null;
                predictorPool = null;
            } catch (Exception e) {
                log.warn("归还Predictor失败", e);
                try {
                    predictor.close(); // 归还失败才销毁
                } catch (Exception ex) {
                    log.error("关闭Predictor失败", ex);
                }
            }
        }
        if (grabber != null) {
            try {
                grabber.stop(); grabber.release();
                grabber = null;
            }catch (FrameGrabber.Exception e) {
                log.error("释放Grabber失败", e);
            }
        }
        log.debug("停止检测完毕");
    }


    @Override
    public void close() {
//        if(isRunning){
//            System.out.println("--isRunning：" + isRunning);
//            stopDetection();
//        }
        if (grabberExecutor != null){
            grabberExecutor.shutdownNow();
        }
        if (processorExecutor != null) {
            processorExecutor.shutdownNow();
        }
        if (callbackExecutor != null) {
            callbackExecutor.shutdownNow();
        }
    }

    public static class Builder {
        private DetectorModel detectorModel;
        private String streamUrl;
        private int frameDetectionInterval = 1;
        private StreamDetectionListener listener;
        private VideoSourceType sourceType = VideoSourceType.STREAM; // 默认流
        private int cameraIndex = 0; // 默认第一个摄像头

        private long repeatGap = 5;//同物体重复检测间隔

        public Builder detectorModel(DetectorModel m) { this.detectorModel = m; return this; }
        public Builder streamUrl(String url) { this.streamUrl = url; return this; }
        public Builder listener(StreamDetectionListener listener) { this.listener = listener; return this; }
        public Builder repeatGap(long repeatGap) {
            this.repeatGap = repeatGap;
            return this;
        }
        public Builder sourceType(VideoSourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }
        public Builder cameraIndex(int cameraIndex) {
            this.cameraIndex = cameraIndex;
            return this;
        }
        public Builder frameDetectionInterval(int interval) {
            if (interval < 1) throw new IllegalArgumentException("frameDetectionInterval >= 1");
            this.frameDetectionInterval = interval;
            return this;
        }

        public StreamDetector build() {
            if (detectorModel == null) {
                throw new DetectionException("detectorModel 不能为空");
            }

            if (sourceType == null) {
                throw new DetectionException("sourceType 不能为空");
            }

            // 根据 sourceType 校验不同的参数
            switch (sourceType) {
                case STREAM:
                case FILE:
                    if (StringUtils.isBlank(streamUrl)) {
                        throw new DetectionException("streamUrl 不能为空");
                    }
                    break;
                case CAMERA:
                    if (cameraIndex < 0) {
                        throw new DetectionException("cameraIndex 必须 >= 0");
                    }
                    break;
                default:
                    throw new DetectionException("不支持的视频源类型: " + sourceType);
            }

            return new StreamDetector(this);
        }

    }

}
