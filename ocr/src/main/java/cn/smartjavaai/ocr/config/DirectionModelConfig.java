package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import lombok.Data;

/**
 * 文本方向分类模型配置
 * @author dwj
 * @date 2025/4/22
 */
@Data
public class DirectionModelConfig extends ModelConfig {

    /**
     * 模型
     */
    private DirectionModelEnum modelEnum;

    /**
     * 检测模型路径
     */
    private String modelPath;

    /**
     * 文本检测模型
     */
    private OcrCommonDetModel textDetModel;



}
