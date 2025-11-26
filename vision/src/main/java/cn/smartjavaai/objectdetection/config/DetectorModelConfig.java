package cn.smartjavaai.objectdetection.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.objectdetection.enums.DetectorModelEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目标检测模型参数配置
 *
 * @author dwj
 * @date 2025/4/4
 */
@Data
public class DetectorModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private DetectorModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private float threshold = DetectorConstant.DEFAULT_THRESHOLD;


    /**
     * 模型路径
     */
    private String modelPath;

    /**
     * 候选框数量：默认为8400. 应设置0到8400之间的整数
     * 用于性能优化的关键参数，它通过限制模型后处理阶段需要处理的候选框（bounding boxes）数量来提高推理速度
     * 建议不低于1000
     */
    private int maxBox;

    /**
     * 允许的分类列表
     */
    private List<String> allowedClasses;

    /**
     * 检测结果数量
     */
    private int topK;


    public DetectorModelConfig() {
    }

    public DetectorModelConfig(DetectorModelEnum modelEnum, DeviceEnum device) {
        this.modelEnum = modelEnum;
        setDevice(device);
    }

    public DetectorModelConfig(DetectorModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }


}
