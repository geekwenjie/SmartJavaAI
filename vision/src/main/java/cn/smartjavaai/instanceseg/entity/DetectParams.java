package cn.smartjavaai.instanceseg.entity;

import lombok.Data;

/**
 * 检测参数
 * @author dwj
 */
@Data
public class DetectParams {

    /**
     * 置信度阈值
     */
    private float threshold = 0.3f;

}
