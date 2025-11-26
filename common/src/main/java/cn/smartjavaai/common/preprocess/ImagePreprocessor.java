package cn.smartjavaai.common.preprocess;

import java.awt.*;

/**
 * 图片预处理
 * @author dwj
 */
public interface ImagePreprocessor<T> {


    ImagePreprocessor<T> setExtendRatio(float ratio);

    ImagePreprocessor<T> setTargetSize(int size);

    ImagePreprocessor<T> setCenterCropSize(int size);

    ImagePreprocessor<T> enableSquarePadding(boolean enable);

    ImagePreprocessor<T> enableScaling(boolean enable);

    ImagePreprocessor<T> enableCenterCrop(boolean enable);

    ImagePreprocessor<T> setPaddingColor(Color color);

    T process();

}
