package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import lombok.Data;

/**
 * OCR检测模型配置
 * @author dwj
 * @date 2025/4/22
 */
@Data
public class OcrDetModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private CommonDetModelEnum modelEnum;

    /**
     * 检测模型路径
     */
    private String detModelPath;


}
