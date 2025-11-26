package cn.smartjavaai.objectdetection.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import cn.smartjavaai.objectdetection.enums.PersonDetectorModelEnum;
import lombok.Data;

import java.util.List;

/**
 * 行人检测模型参数配置
 *
 * @author dwj
 * @date 2025/4/4
 */
@Data
public class PersonDetModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private PersonDetectorModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private float threshold;


    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 按置信度分数排序后，最多保留的检测框数量
     */
    private int topK;


    public PersonDetModelConfig() {
    }

    public PersonDetModelConfig(PersonDetectorModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public PersonDetModelConfig(PersonDetectorModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
