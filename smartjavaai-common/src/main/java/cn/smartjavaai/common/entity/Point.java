package cn.smartjavaai.common.entity;

import ai.djl.util.JsonUtils;

import java.io.Serializable;

/**
 * 点
 * @author dwj
 */
public class Point implements Serializable {
    private static final long serialVersionUID = 1L;
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return JsonUtils.GSON_COMPACT.toJson(this);
    }
}
