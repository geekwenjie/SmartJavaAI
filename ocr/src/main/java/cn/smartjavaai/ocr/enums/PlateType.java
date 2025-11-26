package cn.smartjavaai.ocr.enums;

/**
 * @author dwj
 */
public enum PlateType {

    SINGLE("single", "单层"),
    DOUBLE("double", "双层"),
    UNKNOWN("unknown", "未知");

    private final String className;
    private final String description;

    PlateType(String className, String description) {
        this.className = className;
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public String getDescription() {
        return description;
    }




    /**
     * 根据value获取对应的PlateType
     * @param className
     * @return PlateType
     */
    public static PlateType fromClassName(String className) {
        for (PlateType type : values()) {
            if (type.className.equals(className)) {
                return type;
            }
        }
        return null;
    }

}
