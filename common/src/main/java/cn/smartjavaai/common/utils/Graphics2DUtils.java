package cn.smartjavaai.common.utils;

import cn.smartjavaai.common.entity.Point;

import java.awt.*;
import java.util.List;

/**
 * @author dwj
 */
public class Graphics2DUtils {


    /**
     * 绘制文本
     * @param g
     * @param text
     * @param x
     * @param y
     * @param stroke
     * @param padding
     */
    public static void drawText(Graphics2D g, String text, int x, int y, int stroke, int padding) {
        FontMetrics metrics = g.getFontMetrics();
        x += stroke / 2;
        y += stroke / 2;
        int width = metrics.stringWidth(text) + padding * 2 - stroke / 2;
        int height = metrics.getHeight() + metrics.getDescent();
        int ascent = metrics.getAscent();
        y = Math.max(0, y - height);
        java.awt.Rectangle background = new java.awt.Rectangle(x, y, width, height);
        g.fill(background);
        g.setPaint(Color.WHITE);
        g.drawString(text, x + padding, y + ascent);
    }

    /**
     * 绘制人脸关键点
     * @param g
     * @param keyPoints
     */
    public static void drawLandmarks(Graphics2D g, List<cn.smartjavaai.common.entity.Point> keyPoints) {
        g.setColor(new Color(246, 96, 0));
        BasicStroke bStroke = new BasicStroke(4.0F, 0, 0);
        g.setStroke(bStroke);
        for (Point point : keyPoints){
            g.drawRect((int)point.getX(), (int)point.getY(), 2, 2);
        }
    }


    public static void drawMultilineTextWithBackground(Graphics2D g, List<String> lines, int x, int y) {
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int maxWidth = lines.stream().mapToInt(fm::stringWidth).max().orElse(0);

        int padding = 4;
        int boxWidth = maxWidth + padding * 2;
        int boxHeight = lineHeight * lines.size() + padding * 2;

        // 背景矩形
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(x, y, boxWidth, boxHeight, 8, 8);

        // 绘制每一行文字
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), x + padding, y + padding + (i + 1) * lineHeight - 4);
        }
    }

}
