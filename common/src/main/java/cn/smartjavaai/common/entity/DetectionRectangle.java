package cn.smartjavaai.common.entity;

import lombok.Data;

/**
 * 检测结果-矩形区域
 * @author dwj
 */
@Data
public class DetectionRectangle {

    public int x;
    public int y;
    public int width;
    public int height;

    public DetectionRectangle() {
    }

    public DetectionRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

}
