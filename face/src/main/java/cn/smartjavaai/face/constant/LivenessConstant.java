package cn.smartjavaai.face.constant;

import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import lombok.Data;

/**
 * 活体检测常量
 * @author dwj
 */
public class LivenessConstant {

    /**
     * 默认人脸清晰度阈值
     */
    public static final float DEFAULT_FACE_CLARITY_THRESHOLD = 0.3F;

    /**
     * 默认活体阈值
     */
    public static final float DEFAULT_REALITY_THRESHOLD = 0.8F;

    /**
     * 视频默认检测帧数
     */
    public static final int DEFAULT_FRAME_COUNT = 10;

    /**
     * 视频默认最大检测帧数
     */
    public static final int DEFAULT_MAX_VIDEO_DETECT_FRAMES = Integer.MAX_VALUE;
}
