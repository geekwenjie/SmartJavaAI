package cn.smartjavaai.objectdetection.stream;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionInfo;

import java.util.List;

/**
 * 视频流目标检测监听器
 * @author dwj
 */
public interface StreamDetectionListener {



    /**
     * 当检测到目标时回调
     * @param detectionInfoList 目标信息列表
     * @param image 检测到的图片
     */
    void onObjectDetected(List<DetectionInfo> detectionInfoList, Image image);

    /**
     * 当视频文件读取完毕时回调
     */
    void onStreamEnded();

    /**
     * 当视频流断开连接时回调
     */
    void onStreamDisconnected();
}
