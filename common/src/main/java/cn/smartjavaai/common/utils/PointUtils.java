package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.output.Point;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author dwj
 */
public class PointUtils {

    /**
     * 对 4 个关键点进行排序，顺序为：
     * 左上、右上、右下、左下
     */
    public static List<Point> orderPoints(List<Point> points) {
        if (points == null || points.size() != 4) {
            throw new IllegalArgumentException("必须提供 4 个点");
        }

        // 按 X 坐标升序排列
        points.sort(Comparator.comparingDouble(Point::getX));

        List<Point> left = points.subList(0, 2);
        List<Point> right = points.subList(2, 4);

        // 左侧两点按 Y 排序：上为 tl，下为 bl
        Point tl = left.get(0).getY() < left.get(1).getY() ? left.get(0) : left.get(1);
        Point bl = left.get(0).getY() >= left.get(1).getY() ? left.get(0) : left.get(1);

        // 右侧两点按 Y 排序：上为 tr，下为 br
        Point tr = right.get(0).getY() < right.get(1).getY() ? right.get(0) : right.get(1);
        Point br = right.get(0).getY() >= right.get(1).getY() ? right.get(0) : right.get(1);

        return Arrays.asList(tl, tr, br, bl);
    }

    /**
     * 欧式距离计算
     *
     * @param point1
     * @param point2
     * @return
     */
    public static float distance(float[] point1, float[] point2) {
        float disX = point1[0] - point2[0];
        float disY = point1[1] - point2[1];
        float dis = (float) Math.sqrt(disX * disX + disY * disY);
        return dis;
    }

    /**
     * 欧式距离计算
     *
     * @param point1
     * @param point2
     * @return
     */
    public static float distance(Point point1, Point point2) {
        double disX = point1.getX() - point2.getX();
        double disY = point1.getY() - point2.getY();
        float dis = (float) Math.sqrt(disX * disX + disY * disY);
        return dis;
    }



}
