package cn.smartjavaai.speech.asr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.speech.asr.enums.AsrModelEnum;
import lombok.Data;

import java.nio.file.Path;

/**
 * Asr模型配置
 * @author dwj
 * @date 2025/7/31
 */
@Data
public class AsrModelConfig extends ModelConfig {

    private AsrModelEnum modelEnum;

    private String modelPath;

    /**
     * 依赖库目录
     */
    private Path libPath;

    private String modelName;
}
