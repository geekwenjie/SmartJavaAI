package cn.smartjavaai.common.entity;

import lombok.Data;

import java.util.List;

/**
 * 定向边界框 检测结果
 * @author dwj
 */
@Data
public class ObbDetInfo {

    /**
     * 类别名称
     */
    private String className;

    /**
     * 检测框坐标
     */
    private RotatedBox rotatedBox;

    public ObbDetInfo() {
    }


    public ObbDetInfo(String className, RotatedBox rotatedBox) {
        this.className = className;
        this.rotatedBox = rotatedBox;
    }
}
