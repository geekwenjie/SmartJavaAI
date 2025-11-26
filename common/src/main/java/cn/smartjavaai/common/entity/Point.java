package cn.smartjavaai.common.entity;

import ai.djl.util.JsonUtils;

import java.io.Serializable;

/**
 * 点
 * @author dwj
 */
public class Point implements Serializable {
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return JsonUtils.GSON_COMPACT.toJson(this);
    }

    public org.opencv.core.Point toCvPoint() {
        return new org.opencv.core.Point(x, y);
    }


}
