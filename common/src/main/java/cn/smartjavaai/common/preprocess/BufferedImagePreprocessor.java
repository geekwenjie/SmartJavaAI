package cn.smartjavaai.common.preprocess;

import cn.smartjavaai.common.entity.DetectionRectangle;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片预处理
 * @author dwj
 * @date 2025/6/27
 */
public class BufferedImagePreprocessor implements ImagePreprocessor<BufferedImage>{

    private BufferedImage image;
    private DetectionRectangle rect;
    private float extendRatio = 1;
    private int targetSize = 128;
    private int centerCropSize = 80;

    private Color paddingColor = new Color(127, 127, 127); // 默认灰色
    private boolean enableSquarePadding = true;
    private boolean enableScaling = true;
    private boolean enableCenterCrop = false;



    public BufferedImagePreprocessor(BufferedImage image, DetectionRectangle rect) {
        this.image = image;
        this.rect = rect;
    }

    public BufferedImagePreprocessor setExtendRatio(float ratio) {
        this.extendRatio = ratio;
        return this;
    }

    public BufferedImagePreprocessor setTargetSize(int size) {
        this.targetSize = size;
        return this;
    }

    public BufferedImagePreprocessor setCenterCropSize(int size) {
        this.centerCropSize = size;
        return this;
    }

    public BufferedImagePreprocessor enableSquarePadding(boolean enable) {
        this.enableSquarePadding = enable;
        return this;
    }

    public BufferedImagePreprocessor enableScaling(boolean enable) {
        this.enableScaling = enable;
        return this;
    }

    public BufferedImagePreprocessor enableCenterCrop(boolean enable) {
        this.enableCenterCrop = enable;
        return this;
    }

    public BufferedImagePreprocessor setPaddingColor(Color color) {
        this.paddingColor = color;
        return this;
    }


    public BufferedImage process() {
        // Step 1: 基于检测框扩展
        BufferedImage cropped = cropAndExtend();

        // Step 2: 补正方形 + 背景填充
        BufferedImage squared = enableSquarePadding ? squarePadding(cropped) : cropped;

        // Step 3: 缩放
        BufferedImage scaled = enableScaling ? scaleToTarget(squared) : squared;

        // Step 4: CenterCrop
        BufferedImage finalResult = enableCenterCrop ? centerCrop(scaled) : scaled;

        return finalResult;
    }

    /**
     * 检测框扩展及裁剪
     * @return
     */
    private BufferedImage cropAndExtend() {
        int x = rect.x;
        int y = rect.y;
        int width = rect.width;
        int height = rect.height;

        int extendX = Math.round(width * extendRatio);
        int extendY = Math.round(height * extendRatio);

        // 计算扩展后的边界 (确保不超出图像范围)
        int left = Math.max(0, x - extendX);
        int right = Math.min(image.getWidth(), x + width + extendX);
        int top = Math.max(0, y - extendY);
        int bottom = Math.min(image.getHeight(), y + height + extendY);

        // 动态计算最大可用扩展区域
        int origRoiWidth = right - left;
        int origRoiHeight = bottom - top;
        int longSide = Math.max(origRoiWidth, origRoiHeight);

        // 计算可扩展空间（不超出原图边界）
        int extendLeft = Math.min(left, (longSide - origRoiWidth) / 2);
        int extendRight = Math.min(image.getWidth() - right, (longSide - origRoiWidth + 1) / 2);
        int extendTop = Math.min(top, (longSide - origRoiHeight) / 2);
        int extendBottom = Math.min(image.getHeight() - bottom, (longSide - origRoiHeight + 1) / 2);

        // 计算实际扩展后的区域
        int expandedLeft = left - extendLeft;
        int expandedRight = right + extendRight;
        int expandedTop = top - extendTop;
        int expandedBottom = bottom + extendBottom;

        int expandedWidth = expandedRight - expandedLeft;
        int expandedHeight = expandedBottom - expandedTop;

        return image.getSubimage(expandedLeft, expandedTop, expandedWidth, expandedHeight);
    }

    /**
     * 填充正方形
     * @param src
     * @return
     */
    private BufferedImage squarePadding(BufferedImage src) {
        int longSide = Math.max(src.getWidth(), src.getHeight());
        BufferedImage squared = new BufferedImage(longSide, longSide, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = squared.createGraphics();
        g.setColor(paddingColor);
        g.fillRect(0, 0, longSide, longSide);
        int xOffset = (longSide - src.getWidth()) / 2;
        int yOffset = (longSide - src.getHeight()) / 2;
        g.drawImage(src, xOffset, yOffset, null);
        g.dispose();
        return squared;
    }

    private BufferedImage scaleToTarget(BufferedImage src) {
        Image scaled = src.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        BufferedImage result = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = result.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return result;
    }

    private BufferedImage centerCrop(BufferedImage src) {
        int startX = (src.getWidth() - centerCropSize) / 2;
        int startY = (src.getHeight() - centerCropSize) / 2;
        return src.getSubimage(startX, startY, centerCropSize, centerCropSize);
    }

}
