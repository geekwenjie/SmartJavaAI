package cn.smartjavaai.pose.config;

import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.pose.enums.PoseModelEnum;
import lombok.Data;

import java.util.List;

/**
 * 姿态估计模型参数配置
 *
 * @author dwj
 */
@Data
public class PoseModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private PoseModelEnum modelEnum;



    /**
     * 模型路径
     */
    private String modelPath;


    /**
     * 置信度阈值
     */
    private float threshold;



    public PoseModelConfig() {
    }

    public PoseModelConfig(PoseModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public PoseModelConfig(PoseModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
