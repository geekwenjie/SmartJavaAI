package cn.smartjavaai.common.enums.face;

/**
 * 人脸表情枚举
 * @author dwj
 */
public enum FacialExpression {

    ANGRY("angry", "愤怒"),
    DISGUST("disgust", "厌恶"),
    FEAR("fear", "害怕"),
    HAPPY("happy", "高兴"),
    SAD("sad", "伤心"),
    SURPRISE("surprise", "惊讶"),
    NEUTRAL("neutral", "中性");

    private final String label;
    private final String description;

    FacialExpression(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static FacialExpression fromLabel(String label) {
        for (FacialExpression facialExpression : FacialExpression.values()) {
            if (facialExpression.getLabel().equals(label)) {
                return facialExpression;
            }
        }
        throw new IllegalArgumentException("Invalid facial expression label: " + label);
    }

}
