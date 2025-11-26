package cn.smartjavaai.translation.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;

import cn.smartjavaai.translation.enums.TranslationModeEnum;
import lombok.Data;

/**
 * 机器翻译模型配置
 * @author lwx
 * @date 2025/6/05
 */
@Data
public class TranslationModelConfig extends ModelConfig {
    /**
     * 翻译模型
     */
    private TranslationModeEnum modelEnum;

    /**
     * 翻译模型路径
     */
    private String modelPath;




}
