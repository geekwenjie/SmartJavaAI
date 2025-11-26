package cn.smartjavaai.common.entity;

import lombok.Data;

import java.util.List;

/**
 * 多边形
 * @author dwj
 */
@Data
public class PolygonLabel {

    private List<Point> points;
    private String text;

    public PolygonLabel(List<Point> points, String text) {
        this.points = points;
        this.text = text;
    }

    public PolygonLabel() {
    }

    public PolygonLabel(List<Point> points) {
        this.points = points;
    }
}
