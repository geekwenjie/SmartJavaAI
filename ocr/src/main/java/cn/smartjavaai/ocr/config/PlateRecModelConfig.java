package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.ocr.enums.PlateDetModelEnum;
import cn.smartjavaai.ocr.enums.PlateRecModelEnum;
import cn.smartjavaai.ocr.model.plate.PlateDetModel;
import lombok.Data;

/**
 * 车牌识别模型配置
 * @author dwj
 */
@Data
public class PlateRecModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private PlateRecModelEnum modelEnum;

    /**
     * 检测模型路径
     */
    private String modelPath;

    /**
     * 车牌检测模型
     */
    private PlateDetModel plateDetModel;



}
