package cn.smartjavaai.face.entity;

import lombok.Data;

import java.util.Map;

/**
 * 人脸质量检测汇总结果
 * @author dwj
 * @date 2025/6/27
 */
@Data
public class FaceQualitySummary {

    private FaceQualityResult brightness;      // 亮度
    private FaceQualityResult clarity;         // 清晰度
    private FaceQualityResult completeness;    // 完整度
    private FaceQualityResult pose;            // 姿态
    private FaceQualityResult resolution;      // 分辨率

    private Map<String, Object> extraResults; // 额外检测结果

}
