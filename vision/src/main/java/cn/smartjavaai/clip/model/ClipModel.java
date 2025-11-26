package cn.smartjavaai.clip.model;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.clip.config.ClipModelConfig;
import cn.smartjavaai.common.entity.R;

/**
 * @author dwj
 * @date 2025/10/20
 */
public interface ClipModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(ClipModelConfig config); // 加载模型

    /**
     * 图片特征提取
     * @param image
     * @return
     */
    default R<float[]> extractImageFeatures(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 图片特征提取
     * @param imagePath
     * @return
     */
    default R<float[]> extractImageFeatures(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本特征提取
     * @param inputs
     * @return
     */
    default R<float[]> extractTextFeatures(String inputs){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本和图片特征比较
     * @param image
     * @param text
     * @return
     */
    default R<Float> compareTextAndImage(Image image, String text){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 图片特征比较
     * @param image1 图1
     * @param image2 图2
     * @return
     */
    default R<Float> compareImage(Image image1, Image image2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 图片特征比较
     * @param image1
     * @param image2
     * @param scale
     * @return
     */
    default R<Float> compareImage(Image image1, Image image2, float scale){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 图片特征比较
     * @param imagePath1 图1
     * @param imagePath2 图2
     * @return
     */
    default R<Float> compareImage(String imagePath1, String imagePath2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 图片特征比较
     * @param imagePath1 图1
     * @param imagePath2 图2
     * @return
     */
    default R<Float> compareImage(String imagePath1, String imagePath2, float scale){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 文本特征比较
     * @param input1 文本1
     * @param input2 文本2
     * @return
     */
    default R<Float> compareText(String input1, String input2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 文本特征比较
     * @param input1 文本1
     * @param input2 文本2
     * @return
     */
    default R<Float> compareText(String input1, String input2, float scale){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 特征比较
     * @param feature1
     * @param feature2
     * @param scale
     * @return
     */
    default R<Float> compareFeatures(float[] feature1, float[] feature2, float scale){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
