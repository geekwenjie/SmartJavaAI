package cn.smartjavaai.zeroshot.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.zeroshot.enums.ZeroDetModelEnum;
import lombok.Data;

import java.util.List;

/**
 * 零样本目标检测模型参数配置
 *
 * @author dwj
 */
@Data
public class ZeroDetConfig extends ModelConfig {

    /**
     * 模型
     */
    private ZeroDetModelEnum modelEnum;


    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 置信度阈值
     */
    private float threshold = 0.3f;


    public ZeroDetConfig() {
    }

    public ZeroDetConfig(ZeroDetModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public ZeroDetConfig(ZeroDetModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
