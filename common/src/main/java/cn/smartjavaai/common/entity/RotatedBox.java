package cn.smartjavaai.common.entity;

import lombok.Data;

/**
 * 旋转框
 * @author dwj
 */
@Data
public class RotatedBox {

    /**
     * 左上角
     */
    private Point topLeft;

    /**
     * 右上角
     */
    private Point topRight;

    /**
     * 右下角
     */
    private Point bottomRight;

    /**
     * 左下角
     */
    private Point bottomLeft;

    public RotatedBox(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public RotatedBox() {
    }
}
