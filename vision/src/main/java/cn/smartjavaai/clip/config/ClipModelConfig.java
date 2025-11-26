package cn.smartjavaai.clip.config;

import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.clip.enums.ClipModelEnum;
import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.Data;

import java.util.List;

/**
 * CLIP模型参数配置
 *
 * @author dwj
 */
@Data
public class ClipModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private ClipModelEnum modelEnum;


    /**
     * 模型路径
     */
    private String modelPath;



    public ClipModelConfig() {
    }

    public ClipModelConfig(ClipModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public ClipModelConfig(ClipModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
