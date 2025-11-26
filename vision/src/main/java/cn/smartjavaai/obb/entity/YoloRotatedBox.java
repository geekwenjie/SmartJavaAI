package cn.smartjavaai.obb.entity;

import cn.smartjavaai.common.entity.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * 旋转框
 * @author dwj
 */
public class YoloRotatedBox {
    public float cx, cy, w, h, angle;
    public float score;

    public String className;

    public YoloRotatedBox(float cx, float cy, float w, float h, float angle, String className, float score) {
        this.cx = cx;
        this.cy = cy;
        this.w = w;
        this.h = h;
        this.angle = angle;
        this.className = className;
        this.score = score;
    }

    public static double probiou(YoloRotatedBox b1, YoloRotatedBox b2, double eps) {
        double[] c1 = covarianceMatrix(b1.w, b1.h, b1.angle);
        double[] c2 = covarianceMatrix(b2.w, b2.h, b2.angle);

        double a1 = c1[0], b1v = c1[1], c1v = c1[2];
        double a2 = c2[0], b2v = c2[1], c2v = c2[2];

        double x1 = b1.cx, y1 = b1.cy;
        double x2 = b2.cx, y2 = b2.cy;

        double t1 = ((a1 + a2) * Math.pow(y1 - y2, 2) + (b1v + b2v) * Math.pow(x1 - x2, 2))
                / ((a1 + a2) * (b1v + b2v) - Math.pow(c1v + c2v, 2) + eps);
        double t2 = ((c1v + c2v) * (x2 - x1) * (y1 - y2))
                / ((a1 + a2) * (b1v + b2v) - Math.pow(c1v + c2v, 2) + eps);
        double t3 = Math.log(((a1 + a2) * (b1v + b2v) - Math.pow(c1v + c2v, 2))
                / (4 * Math.sqrt(a1 * b1v - Math.pow(c1v, 2)) * Math.sqrt(a2 * b2v - Math.pow(c2v, 2)) + eps) + eps);

        // 2. probiou 内部 clamp 严格对应 Python
        double bd = 0.25 * t1 + 0.5 * t2 + 0.5 * t3;
        bd = Math.max(Math.min(bd, 100.0), eps);
        double hd = Math.sqrt(1.0 - Math.exp(-bd) + eps);
        return 1 - hd;
    }

    private static double[] covarianceMatrix(double w, double h, double r) {
        double a = Math.pow(w, 2) / 12.0;
        double b = Math.pow(h, 2) / 12.0;
        double cos = Math.cos(r);
        double sin = Math.sin(r);

        double aVal = a * cos * cos + b * sin * sin;
        double bVal = a * sin * sin + b * cos * cos;
        double cVal = (a - b) * sin * cos;
        return new double[]{aVal, bVal, cVal};
    }


    /**
     * 转换为4点坐标
     * @return
     */
    public List<Point> toPoints() {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // vec1 = [w/2*cos, w/2*sin]
        double vec1x = w / 2.0 * cos;
        double vec1y = w / 2.0 * sin;

        // vec2 = [-h/2*sin, h/2*cos]
        double vec2x = -h / 2.0 * sin;
        double vec2y = h / 2.0 * cos;

        List<Point> points = new ArrayList<>(4);

        // pt1 = ctr + vec1 + vec2
        points.add(new Point((int) Math.round(cx + vec1x + vec2x), (int) Math.round(cy + vec1y + vec2y)));

        // pt2 = ctr + vec1 - vec2
        points.add(new Point((int) Math.round(cx + vec1x - vec2x), (int) Math.round(cy + vec1y - vec2y)));

        // pt3 = ctr - vec1 - vec2
        points.add(new Point((int) Math.round(cx - vec1x - vec2x), (int) Math.round(cy - vec1y - vec2y)));

        // pt4 = ctr - vec1 + vec2
        points.add(new Point((int) Math.round(cx - vec1x + vec2x), (int) Math.round(cy - vec1y + vec2y)));

        return points;
    }

}
