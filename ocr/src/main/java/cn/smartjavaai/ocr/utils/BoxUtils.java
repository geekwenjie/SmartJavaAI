package cn.smartjavaai.ocr.utils;

import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.ocr.entity.OcrBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author dwj
 * @date 2026/1/11
 */
public class BoxUtils {


    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    /**
     * 寻找指定方向上距离最近的框
     *
     * @param anchor 锚点框，作为搜索的起始参考框
     * @param boxList 候选框列表，在其中搜索目标框
     * @param direction 搜索方向，可选 UP（上）、DOWN（下）、LEFT（左）、RIGHT（右）
     * @return 找到的最近的 OcrBox，如果找不到符合条件的框则返回 null
     * 
     * @apiNote
     * - 该方法会在指定方向上寻找与锚点框在同一行或同一列的最近邻框
     * - 水平方向（LEFT/RIGHT）：要求候选框与锚点框在 Y 轴上有重叠（同一行）
     * - 垂直方向（UP/DOWN）：要求候选框与锚点框在 X 轴上有重叠（同一列）
     * - 使用欧几里得距离计算中心点之间的距离
     */
    public static OcrBox findNearestBox(OcrBox anchor, List<OcrBox> boxList, Direction direction) {
        OcrBox nearest = null;
        double bestScore = Double.MAX_VALUE;
        AxisSystem axisSystem = buildAxisSystem(anchor);

        for (OcrBox target : boxList) {
            if (target == anchor) {
                continue;
            }

            CandidateMetrics metrics = evaluateCandidate(anchor, target, direction, axisSystem);
            if (metrics == null) {
                continue;
            }
            if (metrics.score < bestScore) {
                bestScore = metrics.score;
                nearest = target;
            }
        }

        return nearest;
    }

    /**
     * 寻找指定方向上距离最近的多个框（按距离升序返回）
     *
     * @param anchor    锚点框
     * @param boxList   候选框列表
     * @param direction 搜索方向
     * @param limit     返回的最大数量（<=0 时返回空列表）
     * @return 按距离由近到远排序的 OcrBox 列表
     */
    public static List<OcrBox> findNearestBoxes(OcrBox anchor, List<OcrBox> boxList, Direction direction, int limit) {
        if (anchor == null || boxList == null || boxList.isEmpty() || limit <= 0) {
            return new ArrayList<>();
        }

        AxisSystem axisSystem = buildAxisSystem(anchor);
        List<Neighbor> candidates = new ArrayList<>();

        for (OcrBox target : boxList) {
            if (target == anchor) {
                continue;
            }

            CandidateMetrics metrics = evaluateCandidate(anchor, target, direction, axisSystem);
            if (metrics == null) {
                continue;
            }
            candidates.add(new Neighbor(target, metrics.score));
        }

        candidates.sort(Comparator.comparingDouble(n -> n.distance));

        List<OcrBox> result = new ArrayList<>();
        int size = Math.min(limit, candidates.size());
        for (int i = 0; i < size; i++) {
            result.add(candidates.get(i).box);
        }
        return result;
    }

    /**
     * 内部使用的邻居结构体，存储框和距离
     */
    private static class Neighbor {
        private final OcrBox box;
        private final double distance;

        private Neighbor(OcrBox box, double distance) {
            this.box = box;
            this.distance = distance;
        }
    }

    private static CandidateMetrics evaluateCandidate(OcrBox anchor, OcrBox target, Direction direction, AxisSystem axisSystem) {
        double mainAxisX = isHorizontalDirection(direction) ? axisSystem.horizontalAxisX : axisSystem.verticalAxisX;
        double mainAxisY = isHorizontalDirection(direction) ? axisSystem.horizontalAxisY : axisSystem.verticalAxisY;
        double crossAxisX = isHorizontalDirection(direction) ? axisSystem.verticalAxisX : axisSystem.horizontalAxisX;
        double crossAxisY = isHorizontalDirection(direction) ? axisSystem.verticalAxisY : axisSystem.horizontalAxisY;

        Projection anchorMain = projectBox(anchor, mainAxisX, mainAxisY);
        Projection anchorCross = projectBox(anchor, crossAxisX, crossAxisY);
        Projection targetMain = projectBox(target, mainAxisX, mainAxisY);
        Projection targetCross = projectBox(target, crossAxisX, crossAxisY);
        Point anchorCenter = getCenter(anchor);
        Point targetCenter = getCenter(target);
        double centerMainDelta = projectPointDelta(anchorCenter, targetCenter, mainAxisX, mainAxisY);

        double mainGap;
        switch (direction) {
            case RIGHT:
            case DOWN:
                mainGap = targetMain.min - anchorMain.max;
                if (centerMainDelta <= 0) {
                    return null;
                }
                break;
            case LEFT:
            case UP:
                mainGap = anchorMain.min - targetMain.max;
                if (centerMainDelta >= 0) {
                    return null;
                }
                break;
            default:
                return null;
        }

        double anchorMainSize = Math.max(1.0, anchorMain.max - anchorMain.min);
        double targetMainSize = Math.max(1.0, targetMain.max - targetMain.min);
        double allowedBacktrack = Math.min(anchorMainSize, targetMainSize) * 0.35;
        if (mainGap < -allowedBacktrack) {
            return null;
        }

        double overlap = Math.max(0.0, Math.min(anchorCross.max, targetCross.max) - Math.max(anchorCross.min, targetCross.min));
        double minCrossSize = Math.max(1.0, Math.min(anchorCross.max - anchorCross.min, targetCross.max - targetCross.min));
        double overlapRatio = overlap / minCrossSize;

        double anchorCrossCenter = (anchorCross.min + anchorCross.max) / 2.0;
        double targetCrossCenter = (targetCross.min + targetCross.max) / 2.0;
        double crossCenterDistance = Math.abs(anchorCrossCenter - targetCrossCenter);
        double crossTolerance = Math.max(anchorCross.max - anchorCross.min, targetCross.max - targetCross.min) * 0.6;

        if (overlapRatio < 0.2 && crossCenterDistance > crossTolerance) {
            return null;
        }

        double score = Math.max(0.0, mainGap) * 10.0 + crossCenterDistance + Math.abs(centerMainDelta) * 0.01;
        if (overlapRatio < 0.2) {
            score += (0.2 - overlapRatio) * 100.0;
        }
        return new CandidateMetrics(score);
    }

    private static boolean isHorizontalDirection(Direction direction) {
        return direction == Direction.LEFT || direction == Direction.RIGHT;
    }

    private static Point getCenter(OcrBox box) {
        float cx = (float) (box.getTopLeft().getX() + box.getTopRight().getX() + box.getBottomRight().getX() + box.getBottomLeft().getX()) / 4;
        float cy = (float) (box.getTopLeft().getY() + box.getTopRight().getY() + box.getBottomRight().getY() + box.getBottomLeft().getY()) / 4;
        return new Point(cx, cy);
    }

    private static AxisSystem buildAxisSystem(OcrBox anchor) {
        Point topLeft = anchor.getTopLeft();
        Point topRight = anchor.getTopRight();
        Point bottomLeft = anchor.getBottomLeft();

        double horizontalX = topRight.getX() - topLeft.getX();
        double horizontalY = topRight.getY() - topLeft.getY();
        double verticalX = bottomLeft.getX() - topLeft.getX();
        double verticalY = bottomLeft.getY() - topLeft.getY();

        double horizontalNorm = Math.hypot(horizontalX, horizontalY);
        double verticalNorm = Math.hypot(verticalX, verticalY);

        if (horizontalNorm < 1e-6) {
            horizontalX = 1.0;
            horizontalY = 0.0;
            horizontalNorm = 1.0;
        }
        if (verticalNorm < 1e-6) {
            verticalX = 0.0;
            verticalY = 1.0;
            verticalNorm = 1.0;
        }

        return new AxisSystem(
                horizontalX / horizontalNorm,
                horizontalY / horizontalNorm,
                verticalX / verticalNorm,
                verticalY / verticalNorm
        );
    }

    private static Projection projectBox(OcrBox box, double axisX, double axisY) {
        double[] values = new double[]{
                dot(box.getTopLeft(), axisX, axisY),
                dot(box.getTopRight(), axisX, axisY),
                dot(box.getBottomRight(), axisX, axisY),
                dot(box.getBottomLeft(), axisX, axisY)
        };
        double min = values[0];
        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            min = Math.min(min, values[i]);
            max = Math.max(max, values[i]);
        }
        return new Projection(min, max);
    }

    private static double dot(Point point, double axisX, double axisY) {
        return point.getX() * axisX + point.getY() * axisY;
    }

    private static double projectPointDelta(Point from, Point to, double axisX, double axisY) {
        return (to.getX() - from.getX()) * axisX + (to.getY() - from.getY()) * axisY;
    }

    private static double min(double a, double b, double c, double d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    private static double max(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    private static class AxisSystem {
        private final double horizontalAxisX;
        private final double horizontalAxisY;
        private final double verticalAxisX;
        private final double verticalAxisY;

        private AxisSystem(double horizontalAxisX, double horizontalAxisY, double verticalAxisX, double verticalAxisY) {
            this.horizontalAxisX = horizontalAxisX;
            this.horizontalAxisY = horizontalAxisY;
            this.verticalAxisX = verticalAxisX;
            this.verticalAxisY = verticalAxisY;
        }
    }

    private static class Projection {
        private final double min;
        private final double max;

        private Projection(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    private static class CandidateMetrics {
        private final double score;

        private CandidateMetrics(double score) {
            this.score = score;
        }
    }


}
