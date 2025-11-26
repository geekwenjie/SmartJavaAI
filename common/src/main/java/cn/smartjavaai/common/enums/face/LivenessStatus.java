package cn.smartjavaai.common.enums.face;

/**
 * 活体检测结果
 * @author dwj
 * @date 2025/4/29
 */
public enum LivenessStatus {

    LIVE(0, "活体"),
    NON_LIVE(1, "非活体"),
    UNKNOWN(2, "未知"),
    DETECTING(3, "正在检测");

    private final int code;
    private final String description;

    LivenessStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LivenessStatus fromCode(int code) {
        for (LivenessStatus status : LivenessStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return UNKNOWN; // 默认返回未知
    }

}
