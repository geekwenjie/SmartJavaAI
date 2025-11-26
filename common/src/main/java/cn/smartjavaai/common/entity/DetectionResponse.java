package cn.smartjavaai.common.entity;

import ai.djl.modality.cv.Image;
import lombok.Data;

import java.util.List;

/**
 * 检测结果
 * @author dwj
 */
@Data
public class DetectionResponse {

    private List<DetectionInfo> detectionInfoList;

    private transient Image drawnImage;


    public DetectionResponse() {
    }

    public DetectionResponse(List<DetectionInfo> detectionInfoList) {
        this.detectionInfoList = detectionInfoList;
    }
}
