package cn.smartjavaai.cls.config;

import cn.smartjavaai.action.enums.ActionRecModelEnum;
import cn.smartjavaai.cls.enums.ClsModelEnum;
import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.Data;

import java.util.List;

/**
 * 分类模型参数配置
 *
 * @author dwj
 */
@Data
public class ClsModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private ClsModelEnum modelEnum;



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

    /**
     * 检测结果数量
     */
    private int topK;



    public ClsModelConfig() {
    }

    public ClsModelConfig(ClsModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public ClsModelConfig(ClsModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
