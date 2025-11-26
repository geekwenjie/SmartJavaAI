package cn.smartjavaai.common.enums.face;

/**
 * 眼睛状态
 * @author dwj
 * @date 2025/5/7
 */
public enum EyeStatus {

    OPEN(0, "睁眼"),

    CLOSED(1, "闭眼"),

    NON_EYE_REGION(2, "非眼部区域"),
    UNKNOWN(3, "未知状态");

    private final int code;
    private final String description;

    EyeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EyeStatus fromCode(int code) {
        for (EyeStatus status : EyeStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return UNKNOWN; // 默认返回未知
    }

}
