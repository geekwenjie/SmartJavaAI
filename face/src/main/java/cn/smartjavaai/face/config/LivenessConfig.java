package cn.smartjavaai.face.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.LivenessConstant;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 活体检测模型配置
 * @author dwj
 */
@Data
public class LivenessConfig extends ModelConfig {

    /**
     * 活体检测模型枚举
     */
    private LivenessModelEnum modelEnum = LivenessModelEnum.SEETA_FACE6_MODEL;

    /**
     * 模型路径
     */
    private String modelPath;


    /**
     * 人脸检测模型
     */
    private FaceDetModel detectModel;



    /**
     * 视频检测帧数
     */
    private int frameCount = LivenessConstant.DEFAULT_FRAME_COUNT;

    /**
     * 视频检测最大帧数
     */
    private int maxVideoDetectFrames = LivenessConstant.DEFAULT_MAX_VIDEO_DETECT_FRAMES;

    /**
     * 真人阈值
     */
    private Float realityThreshold;

    public LivenessConfig() {
    }

    public LivenessConfig(LivenessModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public LivenessConfig(LivenessModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }

    public LivenessConfig(String modelPath) {
        this.modelPath = modelPath;
    }

}
