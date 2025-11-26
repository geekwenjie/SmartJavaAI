package cn.smartjavaai.instanceseg.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.instanceseg.enums.InstanceSegModelEnum;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import lombok.Data;

import java.util.List;

/**
 * 实例分割模型参数配置
 *
 * @author dwj
 */
@Data
public class InstanceSegModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private InstanceSegModelEnum modelEnum;


    /**
     * 模型路径
     */
    private String modelPath;


    /**
     * 允许的分类列表
     */
    private List<String> allowedClasses;

    /**
     * 置信度阈值
     */
    private float threshold = 0.25f;


    public InstanceSegModelConfig() {
    }

    public InstanceSegModelConfig(InstanceSegModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public InstanceSegModelConfig(InstanceSegModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
