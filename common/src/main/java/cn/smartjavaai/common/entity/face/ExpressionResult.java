package cn.smartjavaai.common.entity.face;

import ai.djl.modality.Classifications;
import cn.smartjavaai.common.enums.face.FacialExpression;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import lombok.Data;

/**
 * 人脸表情识别结果
 * @author dwj
 */
@Data
public class ExpressionResult {

    /**
     * 表情
     */
    private FacialExpression expression;

    /**
     * 分数
     */
    private float score;

    /**
     * 完整结果
     */
    private Classifications classifications;

    public ExpressionResult() {
    }


    public ExpressionResult(FacialExpression expression, float score) {
        this.expression = expression;
        this.score = score;
    }

    public ExpressionResult(FacialExpression expression, float score, Classifications classifications) {
        this.expression = expression;
        this.score = score;
        this.classifications = classifications;
    }
}
