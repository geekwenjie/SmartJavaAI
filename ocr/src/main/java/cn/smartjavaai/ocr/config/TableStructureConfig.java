package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.TableStructureModelEnum;
import lombok.Data;

/**
 * OCR表格结构识别模型配置
 * @author dwj
 */
@Data
public class TableStructureConfig extends ModelConfig {

    /**
     * 模型
     */
    private TableStructureModelEnum modelEnum;

    /**
     * 检测模型路径
     */
    private String modelPath;


}
