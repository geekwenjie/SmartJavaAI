package cn.smartjavaai.semseg.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.semseg.enums.SemSegModelEnum;
import lombok.Data;

import java.util.List;

/**
 * 语义分割模型参数配置
 *
 * @author dwj
 */
@Data
public class SemSegModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private SemSegModelEnum modelEnum;


    /**
     * 模型路径
     */
    private String modelPath;


    /**
     * 允许的分类列表
     */
    private List<String> allowedClasses;


    public SemSegModelConfig() {
    }

    public SemSegModelConfig(SemSegModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public SemSegModelConfig(SemSegModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
