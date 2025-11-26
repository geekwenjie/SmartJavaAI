package cn.smartjavaai.common.preprocess;

import cn.smartjavaai.common.entity.DetectionRectangle;
import org.opencv.core.Mat;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * @author dwj
 */
public class OpenCVPreprocessor implements ImagePreprocessor<Mat> {

    private Mat image;
    private DetectionRectangle rect;
    private float extendRatio = 1;
    private int targetSize = 128;
    private int centerCropSize = 80;

    private Scalar paddingColor = new Scalar(127, 127, 127); // 默认灰色
    private boolean enableSquarePadding = true;
    private boolean enableScaling = true;
    private boolean enableCenterCrop = false;

    public OpenCVPreprocessor(Mat image, DetectionRectangle rect) {
        this.image = image;
        this.rect = rect;
    }

    @Override
    public OpenCVPreprocessor setExtendRatio(float ratio) {
        this.extendRatio = ratio;
        return this;
    }

    @Override
    public OpenCVPreprocessor setTargetSize(int size) {
        this.targetSize = size;
        return this;
    }

    @Override
    public OpenCVPreprocessor setCenterCropSize(int size) {
        this.centerCropSize = size;
        return this;
    }

    @Override
    public OpenCVPreprocessor enableSquarePadding(boolean enable) {
        this.enableSquarePadding = enable;
        return this;
    }

    @Override
    public OpenCVPreprocessor enableScaling(boolean enable) {
        this.enableScaling = enable;
        return this;
    }

    @Override
    public OpenCVPreprocessor enableCenterCrop(boolean enable) {
        this.enableCenterCrop = enable;
        return this;
    }

    @Override
    public OpenCVPreprocessor setPaddingColor(java.awt.Color color) {
        this.paddingColor = new Scalar(color.getBlue(), color.getGreen(), color.getRed());
        return this;
    }

    @Override
    public Mat process() {
        // Step 1: 裁剪 + 扩展
        Mat cropped = cropAndExtend();

        // Step 2: 填充正方形
        Mat squared = enableSquarePadding ? squarePadding(cropped) : cropped;

        // Step 3: 缩放
        Mat scaled = enableScaling ? scaleToTarget(squared) : squared;

        // Step 4: CenterCrop
        Mat finalResult = enableCenterCrop ? centerCrop(scaled) : scaled;

        return finalResult;
    }

    /**
     * 检测框扩展及裁剪
     */
    private Mat cropAndExtend() {
        int x = rect.x;
        int y = rect.y;
        int width = rect.width;
        int height = rect.height;

        int extendX = Math.round(width * extendRatio);
        int extendY = Math.round(height * extendRatio);

        int left = Math.max(0, x - extendX);
        int right = Math.min(image.width(), x + width + extendX);
        int top = Math.max(0, y - extendY);
        int bottom = Math.min(image.height(), y + height + extendY);

        int origRoiWidth = right - left;
        int origRoiHeight = bottom - top;
        int longSide = Math.max(origRoiWidth, origRoiHeight);

        // 计算可扩展空间（不超出原图边界）
        int extendLeft = Math.min(left, (longSide - origRoiWidth) / 2);
        int extendRight = Math.min(image.width() - right, (longSide - origRoiWidth + 1) / 2);
        int extendTop = Math.min(top, (longSide - origRoiHeight) / 2);
        int extendBottom = Math.min(image.height() - bottom, (longSide - origRoiHeight + 1) / 2);

        int expandedLeft = left - extendLeft;
        int expandedRight = right + extendRight;
        int expandedTop = top - extendTop;
        int expandedBottom = bottom + extendBottom;

        Rect roi = new Rect(expandedLeft, expandedTop, expandedRight - expandedLeft, expandedBottom - expandedTop);
        return new Mat(image, roi).clone(); // clone 避免与原图共享内存
    }

    /**
     * 填充为正方形
     */
    private Mat squarePadding(Mat src) {
        int longSide = Math.max(src.width(), src.height());
        Mat squared = new Mat(new Size(longSide, longSide), src.type(), paddingColor);
        int xOffset = (longSide - src.width()) / 2;
        int yOffset = (longSide - src.height()) / 2;
        src.copyTo(squared.submat(yOffset, yOffset + src.height(), xOffset, xOffset + src.width()));
        return squared;
    }

    /**
     * 缩放到目标大小
     */
    private Mat scaleToTarget(Mat src) {
        Mat result = new Mat();
        Imgproc.resize(src, result, new Size(targetSize, targetSize), 0, 0, Imgproc.INTER_AREA);
        return result;
    }

    /**
     * CenterCrop
     */
    private Mat centerCrop(Mat src) {
        int startX = (src.width() - centerCropSize) / 2;
        int startY = (src.height() - centerCropSize) / 2;
        Rect roi = new Rect(startX, startY, centerCropSize, centerCropSize);
        return new Mat(src, roi).clone();
    }
}
