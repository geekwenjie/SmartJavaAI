package cn.smartjavaai.face.entity;

import cn.smartjavaai.face.enums.QualityGrade;
import lombok.Data;

/**
 * 质量评估结果
 * @author dwj
 * @date 2025/6/23
 */
@Data
public class FaceQualityResult {

    /**
     * 评估得分
     */
    private float score;

    private QualityGrade grade;

    public FaceQualityResult() {
    }

    public FaceQualityResult(float score, QualityGrade grade) {
        this.score = score;
        this.grade = grade;
    }

}
