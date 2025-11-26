package cn.smartjavaai.face.vector.entity;


import lombok.Data;

import java.util.UUID;

/**
 * 人脸向量实体类
 * @author smartjavaai
 */
@Data
public class FaceVector {

    /**
     * 向量ID
     */
    private String id;

    /**
     * 人脸特征向量
     */
    private float[] vector;

    /**
     * 元数据，可以存储人脸相关的其他信息（JSON格式）
     */
    private String metadata;

    /**
     * 默认构造函数
     */
    public FaceVector() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * 构造函数
     * @param vector 人脸特征向量
     */
    public FaceVector(float[] vector) {
        this();
        this.vector = vector;
    }

    /**
     * 构造函数
     * @param vector 人脸特征向量
     * @param metadata 元数据
     */
    public FaceVector(float[] vector, String metadata) {
        this();
        this.vector = vector;
        this.metadata = metadata;
    }

    /**
     * 构造函数
     * @param id 向量ID
     * @param vector 人脸特征向量
     * @param metadata 元数据
     */
    public FaceVector(String id, float[] vector, String metadata) {
        this.id = id;
        this.vector = vector;
        this.metadata = metadata;
    }
}
