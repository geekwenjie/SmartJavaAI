package cn.smartjavaai.common.preprocess;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionRectangle;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author dwj
 */
public class DJLImagePreprocessor implements ImagePreprocessor<Image>{


    private final ImagePreprocessor<?> delegate;
    private Image input = null;

    public DJLImagePreprocessor(Image image, DetectionRectangle rect) {
        this.input = image;
        if (input.getWrappedImage() instanceof BufferedImage) {
            this.delegate = new BufferedImagePreprocessor((BufferedImage) input.getWrappedImage(), rect);
        } else if (input.getWrappedImage() instanceof Mat) {
            this.delegate = new OpenCVPreprocessor((Mat) input.getWrappedImage(), rect);
        } else {
            throw new IllegalArgumentException("Unsupported input type");
        }
    }

    @Override
    public DJLImagePreprocessor setExtendRatio(float ratio) {
        delegate.setExtendRatio(ratio);
        return this;
    }

    @Override
    public DJLImagePreprocessor setTargetSize(int size) {
        delegate.setTargetSize(size);
        return this;
    }

    @Override
    public DJLImagePreprocessor setCenterCropSize(int size) {
        delegate.setCenterCropSize(size);
        return this;
    }

    @Override
    public DJLImagePreprocessor enableSquarePadding(boolean enable) {
        delegate.enableSquarePadding(enable);
        return this;
    }

    @Override
    public DJLImagePreprocessor enableScaling(boolean enable) {
        delegate.enableScaling(enable);
        return this;
    }

    @Override
    public DJLImagePreprocessor enableCenterCrop(boolean enable) {
        delegate.enableCenterCrop(enable);
        return this;
    }

    @Override
    public ImagePreprocessor setPaddingColor(Color color) {
        return delegate.setPaddingColor(color);
    }


    @Override
    public Image process() {
        Object result = delegate.process();
        if (result instanceof BufferedImage) {
            return SmartImageFactory.getInstance().fromBufferedImage((BufferedImage) result);
        } else if (result instanceof Mat) {
            return SmartImageFactory.getInstance().fromMat((Mat) result);
        }
        throw new IllegalStateException("Unsupported process result: " + result.getClass());
    }

}
