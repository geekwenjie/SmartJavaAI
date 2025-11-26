package cn.smartjavaai.common.entity.face;

import cn.smartjavaai.common.enums.face.EyeStatus;
import cn.smartjavaai.common.enums.face.GenderType;
import lombok.Data;

/**
 * 人脸属性
 * @author dwj
 * @date 2025/5/7
 */
@Data
public class FaceAttribute {

    /**
     * 性别
     */
    private GenderType genderType;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 左眼状态
     */
    private EyeStatus leftEyeStatus;

    /**
     * 右眼状态
     */
    private EyeStatus rightEyeStatus;

    /**
     * 是否带口罩
     */
    private Boolean wearingMask;

    /**
     * 姿态
     */
    private HeadPose headPose;


    public FaceAttribute() {
    }


}
