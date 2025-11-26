package cn.smartjavaai.common.enums.face;

/**
 * 性别枚举
 * @author dwj
 * @date 2025/5/6
 */
public enum GenderType {

    MALE(0, "男"),
    FEMALE(1, "女"),

    UNKNOWN(2, "未知");

    private final int code;
    private final String description;

    GenderType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static GenderType fromCode(int code) {
        for (GenderType status : GenderType.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return UNKNOWN; // 默认返回未知
    }

}
