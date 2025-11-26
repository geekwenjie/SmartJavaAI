package cn.smartjavaai.action.config;

import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import lombok.Data;

import java.util.List;

/**
 * 人类动作识别模型参数配置
 *
 * @author dwj
 */
@Data
public class ActionRecModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private ActionRecModelEnum modelEnum;



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
    private float threshold = 0.3f;



    public ActionRecModelConfig() {
    }

    public ActionRecModelConfig(ActionRecModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public ActionRecModelConfig(ActionRecModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
