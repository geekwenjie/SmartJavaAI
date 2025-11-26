package cn.smartjavaai.ocr.enums;

/**
 * 车牌识别模型枚举
 * @author dwj
 */
public enum PlateRecModelEnum {

    PLATE_REC_CRNN;


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static PlateRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (PlateRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
