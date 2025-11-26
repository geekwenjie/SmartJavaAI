package cn.smartjavaai.face.enums;

/**
 * 向量数据库类型枚举
 * @author dwj
 * @date 2025/5/29
 */
public enum VectorDBType {

    /**
     * Sqlite,非专用向量库的备用方案
     */
    SQLITE,

    /**
     * Milvus向量数据库
     */
    MILVUS;

    /**
     * 未来可以添加其他向量数据库类型
     */
    // FAISS,
    // ELASTICSEARCH,
    // PINECONE

}
