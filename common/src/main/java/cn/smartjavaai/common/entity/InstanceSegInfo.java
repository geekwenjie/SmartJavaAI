package cn.smartjavaai.common.entity;

import lombok.Data;

/**
 * 目标分割信息
 * @author dwj
 */
@Data
public class InstanceSegInfo {

    /**
     * 类别名称
     */
    private String className;

    /**
     * 遮罩
     */
    private float[][] mask;

    public InstanceSegInfo() {
    }

    public InstanceSegInfo(String className, float[][] mask) {
        this.className = className;
        this.mask = mask;
    }
}
