package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.PlateDetModelEnum;
import lombok.Data;

/**
 * 车牌检测模型配置
 * @author dwj
 */
@Data
public class PlateDetModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private PlateDetModelEnum modelEnum;

    /**
     * 检测模型路径
     */
    private String modelPath;

    /**
     * 置信度阈值
     */
    private float confidenceThreshold;

    /**
     * iou阈值
     */
    private float iouThreshold;

    /**
     * 检测结果数量
     */
    private int topK;


}
