package cn.smartjavaai.ocr.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import lombok.Data;

/**
 * OCR识别模型配置
 * @author dwj
 * @date 2025/4/22
 */
@Data
public class OcrRecModelConfig extends ModelConfig {

    /**
     * 识别模型
     */
    private CommonRecModelEnum recModelEnum;

    /**
     * 识别模型路径
     */
    private String recModelPath;

    /**
     * 文本检测模型
     */
    private OcrCommonDetModel textDetModel;

    /**
     * 文本方向模型
     */
    private OcrDirectionModel directionModel;

}
