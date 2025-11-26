package cn.smartjavaai.ocr.enums;

/**
 * OCR表格结构模型枚举
 * @author dwj
 */
public enum TableStructureModelEnum {

    SLANET,
    //SLANEXT_WIRED,
    SLANET_PLUS;




    /**
     * 根据名称获取枚举 (忽略大小写和下划线变体)
     */
    public static TableStructureModelEnum fromName(String name) {
        String formatted = name.trim().toUpperCase().replaceAll("[-_]", "");
        for (TableStructureModelEnum model : values()) {
            if (model.name().replaceAll("_", "").equals(formatted)) {
                return model;
            }
        }
        throw new IllegalArgumentException("未知模型名称: " + name);
    }


}
