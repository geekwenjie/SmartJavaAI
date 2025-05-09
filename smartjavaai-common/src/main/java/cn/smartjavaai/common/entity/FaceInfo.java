package cn.smartjavaai.common.entity;

import cn.smartjavaai.common.enums.LivenessStatus;
import lombok.Data;

import java.util.List;

/**
 * 人脸信息
 * @author dwj
 * @date 2025/5/7
 */
@Data
public class FaceInfo {

    /**
     * 人脸关键点
     */
    private List<Point> keyPoints;

    /**
     * 人脸属性
     */
    private FaceAttribute faceAttribute;

    /**
     * 活体检测结果
     */
    private LivenessStatus livenessStatus;

    public FaceInfo() {
    }

    public FaceInfo(List<Point> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public FaceInfo(List<Point> keyPoints, FaceAttribute faceAttribute, LivenessStatus livenessStatus) {
        this.keyPoints = keyPoints;
        this.faceAttribute = faceAttribute;
        this.livenessStatus = livenessStatus;
    }

    public FaceInfo(FaceAttribute faceAttribute, LivenessStatus livenessStatus) {
        this.faceAttribute = faceAttribute;
        this.livenessStatus = livenessStatus;
    }
}
