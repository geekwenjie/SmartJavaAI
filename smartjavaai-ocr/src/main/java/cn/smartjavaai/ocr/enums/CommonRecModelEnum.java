package cn.smartjavaai.ocr.enums;

/**
 * OCR识别模型枚举
 * @author dwj
 */
public enum CommonRecModelEnum {

    PP_OCR_V5_SERVER_REC_MODEL,

    PP_OCR_V5_MOBILE_REC_MODEL,

    PP_OCR_V4_SERVER_REC_MODEL,

    PP_OCR_V4_MOBILE_REC_MODEL;


    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static CommonRecModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (CommonRecModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
