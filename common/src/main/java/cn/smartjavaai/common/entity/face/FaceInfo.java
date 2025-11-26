package cn.smartjavaai.common.entity.face;

import cn.smartjavaai.common.entity.Point;
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
    private LivenessResult livenessStatus;

    /**
     * 人脸查询结果
     */
    private List<FaceSearchResult> faceSearchResults;

    /**
     * 人脸特征
     */
    private float[] feature;

    /**
     * 表情检测结果
     */
    private ExpressionResult expressionResult;

    public FaceInfo() {
    }

    public FaceInfo(List<Point> keyPoints) {
        this.keyPoints = keyPoints;
    }

    public FaceInfo(List<Point> keyPoints, FaceAttribute faceAttribute, LivenessResult livenessStatus) {
        this.keyPoints = keyPoints;
        this.faceAttribute = faceAttribute;
        this.livenessStatus = livenessStatus;
    }

    public FaceInfo(FaceAttribute faceAttribute, LivenessResult livenessStatus) {
        this.faceAttribute = faceAttribute;
        this.livenessStatus = livenessStatus;
    }


}
