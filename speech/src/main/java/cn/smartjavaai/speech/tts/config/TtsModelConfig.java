package cn.smartjavaai.speech.tts.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.speech.tts.enums.TtsModelEnum;
import lombok.Data;

import java.nio.file.Path;

/**
 * @author dwj
 * @date 2025/10/14
 */
@Data
public class TtsModelConfig extends ModelConfig {

    private TtsModelEnum modelEnum;

    private String modelPath;

    private String modelName;

    /**
     * 依赖库目录
     */
    private Path libPath;

}
