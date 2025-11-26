package cn.smartjavaai.obb.entity;

import lombok.Data;

import java.util.List;

/**
 * odd检测结果
 * @author dwj
 */
@Data
public class ObbResult {

    private List<YoloRotatedBox> rotatedBoxeList;


    public ObbResult(List<YoloRotatedBox> rotatedBoxeList) {
        this.rotatedBoxeList = rotatedBoxeList;
    }
}
