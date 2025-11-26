package cn.smartjavaai.face.config;

import cn.smartjavaai.common.config.ModelConfig;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.constant.FaceDetectConstant;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.vector.config.VectorDBConfig;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸检测识别模型配置
 * @author dwj
 */
@Data
public class FaceRecConfig extends ModelConfig {

    /**
     * 人脸模型枚举
     */
    private FaceRecModelEnum modelEnum;

    /**
     * 模型路径
     */
    private String modelPath;


    /**
     * 向量数据库配置
     */
    private VectorDBConfig vectorDBConfig;

    /**
     * 是否自动加载人脸到内存
     */
    private boolean isAutoLoadFace = true;


    /**
     * 是否裁剪人脸
     */
    private boolean cropFace = true;

    /**
     * 是否对齐人脸
     */
    private boolean align = false;

    /**
     * 人脸检测模型
     */
    private FaceDetModel detectModel;


    public FaceRecConfig() {
    }

    public FaceRecConfig(FaceRecModelEnum modelEnum) {
        this.modelEnum = modelEnum;
    }

    public FaceRecConfig(FaceRecModelEnum modelEnum, String modelPath) {
        this.modelEnum = modelEnum;
        this.modelPath = modelPath;
    }


}
