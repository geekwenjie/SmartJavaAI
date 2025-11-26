package cn.smartjavaai.face.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸检测模型配置
 * @author dwj
 */
@Data
public class FaceDetConfig extends ModelConfig {

    /**
     * 人脸检测模型枚举
     */
    private FaceDetModelEnum modelEnum;

    /**
     * 置信度阈值
     */
    private double confidenceThreshold;


    /**
     * 非极大抑制阈值 作用：消除重叠检测框，保留最优结果
     */
    private double nmsThresh = FaceDetectConstant.NMS_THRESHOLD;

    /**
     * 模型路径
     */
    private String modelPath;


    public FaceDetConfig() {
    }

    public FaceDetConfig(FaceDetModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public FaceDetConfig(FaceDetModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }

}
