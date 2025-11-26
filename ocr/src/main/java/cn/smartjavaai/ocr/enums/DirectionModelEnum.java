package cn.smartjavaai.ocr.enums;

/**
 * OCR文本方向分类模型枚举
 * @author dwj
 * @date 2025/4/4
 */
public enum DirectionModelEnum {

    CH_PPOCR_MOBILE_V2_CLS,

    PP_LCNET_X0_25,

    PP_LCNET_X1_0;



    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static DirectionModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (DirectionModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
