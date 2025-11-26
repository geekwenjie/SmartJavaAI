package cn.smartjavaai.common.entity;

import cn.smartjavaai.common.entity.face.FaceInfo;
import lombok.Data;

/**
 * 检测结果信息
 * @author dwj
 * @date 2025/5/7
 */
@Data
public class DetectionInfo {

    /**
     * 检测位置信息
     */
    private DetectionRectangle detectionRectangle;

    /**
     * 检测得分
     */
    private float score;

    /**
     * 人脸信息
     */

    private FaceInfo faceInfo;

    /**
     * 目标检测信息
     */
    private ObjectDetInfo objectDetInfo;

    /**
     * 目标分割信息
     */
    private InstanceSegInfo instanceSegInfo;

    /**
     * 旋转框信息
     */
    private ObbDetInfo obbDetInfo;



    public DetectionInfo() {
    }

    public DetectionInfo(DetectionRectangle detectionRectangle) {
        this.detectionRectangle = detectionRectangle;
    }

    public DetectionInfo(DetectionRectangle detectionRectangle, float score) {
        this.detectionRectangle = detectionRectangle;
        this.score = score;
    }

    public DetectionInfo(DetectionRectangle detectionRectangle, float score, FaceInfo faceInfo) {
        this.detectionRectangle = detectionRectangle;
        this.score = score;
        this.faceInfo = faceInfo;
    }


}
