package cn.smartjavaai.common.entity.face;

import lombok.Data;

/**
 * 姿态检测结果（单位：度）
 * pitch：上下（俯仰角），正值抬头，负值低头
 * yaw：左右（偏航角），正值右偏，负值左偏
 * roll：倾斜（翻滚角），正值右倾，负值左倾
 */
@Data
public class HeadPose {

    /** 俯仰角：头上下抬（-90°~+90°） */
    private Float pitch;

    /** 偏航角：头左右转（-90°~+90°） */
    private Float yaw;

    /** 翻滚角：头部倾斜（-90°~+90°） */
    private Float roll;

    public HeadPose() {
    }

    public HeadPose(Float pitch, Float yaw, Float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

}
