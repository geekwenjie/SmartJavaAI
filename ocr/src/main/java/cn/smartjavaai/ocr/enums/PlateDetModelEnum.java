package cn.smartjavaai.ocr.enums;

/**
 * 车牌检测模型枚举
 * @author dwj
 */
public enum PlateDetModelEnum {

    YOLOV5,

    YOLOV7;


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static PlateDetModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (PlateDetModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
