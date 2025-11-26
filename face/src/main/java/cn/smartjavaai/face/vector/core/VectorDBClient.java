package cn.smartjavaai.face.vector.core;

import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.vector.entity.FaceVector;
import cn.smartjavaai.common.entity.face.FaceSearchResult;
import cn.smartjavaai.face.vector.exception.VectorDBException;

import java.util.List;

/**
 * 向量数据库客户端接口
 * 定义与向量数据库交互的通用操作
 * @author dwj
 */
public interface VectorDBClient extends AutoCloseable {

    /**
     * 初始化连接和集合
     * @throws VectorDBException 初始化异常
     */
    void initialize();

    /**
     * 创建集合
     * @param collectionName 集合名称
     * @param dimension 向量维度
     */
    void createCollection(String collectionName, int dimension);

    /**
     * 删除集合
     * @param collectionName 集合名称
     */
    void dropCollection(String collectionName);

    /**
     * 检查集合是否存在
     * @param collectionName 集合名称
     * @return 是否存在
     */
    boolean hasCollection(String collectionName);


    /**
     * 插入人脸向量
     * @param faceVector
     * @return
     */
    String insert(FaceVector faceVector);

    /**
     * 更新或新增人脸向量
     * @param faceVector
     */
    void upsert(FaceVector faceVector);

    /**
     * 批量插入人脸向量
     * @param faceVectors
     * @return
     */
    List<String> insertBatch(List<FaceVector> faceVectors);

    /**
     * 根据ID删除向量
     * @param id
     */
    void delete(String id);

    /**
     * 批量删除向量
     * @param ids
     */
    void deleteBatch(List<String> ids);

    /**
     * 搜索相似人脸
     * @param queryVector
     * @param faceSearchParams
     * @return
     */
    List<FaceSearchResult> search(float[] queryVector, FaceSearchParams faceSearchParams);

    /**
     * 获取集合中的向量数量
     * @param collectionName 集合名称
     * @return 向量数量
     */
    long count(String collectionName);

    /**
     * 关闭连接
     */
    @Override
    void close();

    /**
     * 使用人脸ID获取人脸信息
     * @param id
     * @return
     */
    FaceVector getFaceInfoById(String id);


    /**
     * 获取人脸列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<FaceVector> listFaces(long pageNum, long pageSize);

    /**
     * 加载人脸特征到内存
     */
    void loadFaceFeatures();

    /**
     * 释放人脸特征缓存
     */
    void releaseFaceFeatures();

}
