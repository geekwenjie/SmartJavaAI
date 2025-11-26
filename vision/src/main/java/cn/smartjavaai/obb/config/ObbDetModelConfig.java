package cn.smartjavaai.obb.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.obb.enums.ObbDetModelEnum;
import lombok.Data;

import java.util.List;

/**
 * 旋转框模型参数配置
 *
 * @author dwj
 */
@Data
public class ObbDetModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private ObbDetModelEnum modelEnum;


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

    /**
     * 按置信度分数排序后，最多保留的检测框数量
     */
    private int topK;


    public ObbDetModelConfig() {
    }

    public ObbDetModelConfig(ObbDetModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public ObbDetModelConfig(ObbDetModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
