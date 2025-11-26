package cn.smartjavaai.ocr.enums;

/**
 * 文本方向
 * @author dwj
 * @date 2025/5/23
 */
public enum AngleEnum {

    ANGLE_0("0"),
    ANGLE_90("90"),
    ANGLE_180("180"),
    ANGLE_270("270");

    private final String value;

    AngleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AngleEnum fromValue(String value) {
        for (AngleEnum angle : values()) {
            if (angle.value.equals(value)) {
                return angle;
            }
        }
        throw new IllegalArgumentException("Invalid angle value: " + value);
    }

    @Override
    public String toString() {
        return value + "°";
    }
}
