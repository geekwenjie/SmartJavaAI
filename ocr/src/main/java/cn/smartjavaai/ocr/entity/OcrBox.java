package cn.smartjavaai.ocr.entity;

import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.Point;
import lombok.Data;

/**
 * OCR 检测框
 * @author dwj
 * @date 2025/5/20
 */
@Data
public class OcrBox {

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

    public OcrBox(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public OcrBox() {
    }

    public float[] toFloatArray() {
        return new float[]{
                (float)topLeft.getX(), (float)topLeft.getY(),
                (float)topRight.getX(), (float)topRight.getY(),
                (float)bottomRight.getX(), (float)bottomRight.getY(),
                (float)bottomLeft.getX(), (float)bottomLeft.getY()
        };
    }

    /**
     * 转换为 DetectionRectangle，使用最小外包矩形
     */
    public DetectionRectangle toDetectionRectangle() {
        float[] pts = toFloatArray();
        float minX = Math.min(Math.min(pts[0], pts[2]), Math.min(pts[4], pts[6]));
        float minY = Math.min(Math.min(pts[1], pts[3]), Math.min(pts[5], pts[7]));
        float maxX = Math.max(Math.max(pts[0], pts[2]), Math.max(pts[4], pts[6]));
        float maxY = Math.max(Math.max(pts[1], pts[3]), Math.max(pts[5], pts[7]));
        DetectionRectangle rect = new DetectionRectangle();
        rect.setX((int) minX);
        rect.setY((int) minY);
        rect.setWidth((int) (maxX - minX));
        rect.setHeight((int) (maxY - minY));
        return rect;
    }


}
