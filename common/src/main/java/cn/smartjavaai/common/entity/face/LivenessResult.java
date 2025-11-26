package cn.smartjavaai.common.entity.face;

import cn.smartjavaai.common.enums.face.LivenessStatus;
import lombok.Data;

/**
 * 活体检测结果
 * @author dwj
 * @date 2025/6/27
 */
@Data
public class LivenessResult {

    private LivenessStatus status;

    private float score;

    public LivenessResult() {
    }

    public LivenessResult(LivenessStatus status, float score) {
        this.status = status;
        this.score = score;
    }

    public LivenessResult(LivenessStatus status) {
        this.status = status;
    }
}
