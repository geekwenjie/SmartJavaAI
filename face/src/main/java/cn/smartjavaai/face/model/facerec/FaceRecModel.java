package cn.smartjavaai.face.model.facerec;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.face.vector.entity.FaceVector;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * 人脸识别模型
 * @author dwj
 */
public interface FaceRecModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(FaceRecConfig config); // 加载模型


    /**
     * 计算相似度
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     */
    default float calculSimilar(float[] feature1, float[] feature2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }



    /**
     * 特征比较
     * @param image1 图1
     * @param image2 图2
     * @return
     */
    default R<Float> featureComparison(Image image1, Image image2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     */
    @Deprecated
    default R<Float> featureComparison(String imagePath1, String imagePath2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }



    /**
     * 特征比较
     * @param sourceImage1 图1BufferedImage
     * @param sourceImag2 图2BufferedImage
     * @return
     */
    @Deprecated
    default R<Float> featureComparison(BufferedImage sourceImage1, BufferedImage sourceImag2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 特征比较
     * @param imageData1
     * @param imageData2
     * @return
     */
    @Deprecated
    default R<Float> featureComparison(byte[] imageData1, byte[] imageData2){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 注册人脸
     * 提取分数最高人脸进行注册
     * @param faceRegisterInfo 注册人脸信息
     * @param image
     * @return
     */
    default R<String> register(FaceRegisterInfo faceRegisterInfo, Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 注册人脸
     * 提取分数最高人脸进行注册
     * @param faceRegisterInfo 注册人脸信息
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<String> register(FaceRegisterInfo faceRegisterInfo, String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 注册人脸
     * 提取分数最高人脸进行注册
     * @param faceRegisterInfo 注册人脸信息
     * @param inputStream
     * @return
     */
    @Deprecated
    default R<String> register(FaceRegisterInfo faceRegisterInfo, InputStream inputStream){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 注册人脸
     * 提取分数最高人脸进行注册
     * @param faceRegisterInfo 注册人脸信息
     * @param sourceImage
     * @return
     */
    @Deprecated
    default R<String> register(FaceRegisterInfo faceRegisterInfo, BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 注册人脸
     * 提取分数最高人脸进行注册
     * @param faceRegisterInfo 注册人脸信息
     * @param imageData
     * @return
     */
    @Deprecated
    default R<String> register(FaceRegisterInfo faceRegisterInfo, byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 注册人脸
     * 提取分数最高人脸进行注册
     * @param faceRegisterInfo 注册人脸信息
     * @param feature 人脸特征
     * @return
     */
    default R<String> register(FaceRegisterInfo faceRegisterInfo, float[] feature){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 更新或注册人脸
     * 自动提取分数最高人脸进行更新
     * @param faceRegisterInfo 注册人脸信息
     * @param image
     * @return
     */
    default void upsertFace(FaceRegisterInfo faceRegisterInfo, Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 更新或注册人脸
     * 自动提取分数最高人脸进行更新
     * @param faceRegisterInfo 注册人脸信息
     * @param imagePath
     * @return
     */
    @Deprecated
    default void upsertFace(FaceRegisterInfo faceRegisterInfo, String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 更新或注册人脸
     * 自动提取分数最高人脸进行更新
     * @param faceRegisterInfo 注册人脸信息
     * @param sourceImage
     * @return
     */
    @Deprecated
    default void upsertFace(FaceRegisterInfo faceRegisterInfo, BufferedImage sourceImage){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 更新或注册人脸
     * 自动提取分数最高人脸进行更新
     * @param faceRegisterInfo 注册人脸信息
     * @param feature 人脸特征
     * @return
     */
    default void upsertFace(FaceRegisterInfo faceRegisterInfo, float[] feature){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 更新或注册人脸
     * 自动提取分数最高人脸进行更新
     * @param faceRegisterInfo 注册人脸信息
     * @param imageData
     * @return
     */
    @Deprecated
    default void upsertFace(FaceRegisterInfo faceRegisterInfo, byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 查询人脸（查询图片中所有人脸）
     * @param image
     * @param params 人脸查询参数
     * @return
     */
    default R<DetectionResponse> search(Image image, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 查询人脸（查询图片中所有人脸）
     * @param imagePath
     * @param params 人脸查询参数
     * @return
     */
    @Deprecated
    default R<DetectionResponse> search(String imagePath, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 查询人脸（查询图片中所有人脸）
     * 适用于多人脸场景
     * @param sourceImage
     * @return
     */
    @Deprecated
    default R<DetectionResponse> search(BufferedImage sourceImage, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 查询人脸（查询图片中所有人脸）
     * 适用于多人脸场景
     * @param imageData
     * @return
     */
    @Deprecated
    default R<DetectionResponse> search(byte[] imageData, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 查询人脸
     * 适用于多人脸场景
     * @param feature 人脸特征
     * @return
     */
    default List<FaceSearchResult> search(float[] feature, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 查询人脸
     * 从图像中提取分数最高的人脸特征，并在人脸库中进行 1:N 查询
     * 适用于单人脸场景
     * @param image
     * @param params 人脸查询参数
     * @return
     */
    default R<List<FaceSearchResult>> searchByTopFace(Image image, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 查询人脸
     * 从图像中提取分数最高的人脸特征，并在人脸库中进行 1:N 查询
     * 适用于单人脸场景
     * @param imagePath
     * @param params 人脸查询参数
     * @return
     */
    @Deprecated
    default R<List<FaceSearchResult>> searchByTopFace(String imagePath, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 查询人脸
     * 从图像中提取分数最高的人脸特征，并在人脸库中进行 1:N 查询
     * 适用于单人脸场景
     * @param sourceImage
     * @return
     */
    @Deprecated
    default R<List<FaceSearchResult>> searchByTopFace(BufferedImage sourceImage, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 查询人脸
     * 从图像中提取分数最高的人脸特征，并在人脸库中进行 1:N 查询
     * 适用于单人脸场景
     * @param imageData
     * @return
     */
    @Deprecated
    default R<List<FaceSearchResult>> searchByTopFace(byte[] imageData, FaceSearchParams params){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 使用人脸ID获取人脸信息
     * @param id
     * @return
     */
    default R<FaceVector> getFaceInfoById(String id){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 获取人脸列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    default R<List<FaceVector>> listFaces(long pageNum, long pageSize){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 删除已注册人脸
     * @param keys
     * @return
     */
    default void removeRegister(String... keys){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 清空人脸库数据
     */
    default void clearFace(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 特征提取（所有人脸）
     * 适用于多人脸场景
     * @param image
     * @return
     */
    default R<DetectionResponse> extractFeatures(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征提取（所有人脸）
     * 适用于多人脸场景
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<DetectionResponse> extractFeatures(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征提取（所有人脸）
     * 适用于多人脸场景
     * @param imageData 图片字节流
     * @return
     */
    @Deprecated
    default R<DetectionResponse> extractFeatures(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征提取（所有人脸）
     * 适用于多人脸场景
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default R<DetectionResponse> extractFeatures(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default R<float[]> extractFeatures(Image image, DetectionInfo detectionInfo){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征提取（提取分数最高人脸特征）
     * 适用于单人脸场景
     * @param image
     * @return
     */
    default R<float[]> extractTopFaceFeature(Image image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 特征提取（提取分数最高人脸特征）
     * 适用于单人脸场景
     * @param image BufferedImage
     * @return
     */
    @Deprecated
    default R<float[]> extractTopFaceFeature(BufferedImage image){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征提取（提取分数最高人脸特征）
     * 适用于单人脸场景
     * @param imagePath 图片路径
     * @return
     */
    @Deprecated
    default R<float[]> extractTopFaceFeature(String imagePath){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 特征提取（提取分数最高人脸特征）
     * 适用于单人脸场景
     * @param imageData 图片字节流
     * @return
     */
    @Deprecated
    default R<float[]> extractTopFaceFeature(byte[] imageData){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     *  加载人脸特征
     */
    default void loadFaceFeatures(){
         throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 释放人脸特征缓存
     */
    default void releaseFaceFeatures(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 是否加载人脸库完成
     * @return
     */
    default boolean isLoadFaceCompleted(){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    /**
     * 绘制人脸搜索结果
     * @param image
     * @param params
     * @param displayField
     */
    default Image drawSearchResult(Image image, FaceSearchParams params, String displayField){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default GenericObjectPool<Predictor<Image, float[]>> getPool() {
        throw new UnsupportedOperationException("默认不支持该功能");
    }

    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }

}
