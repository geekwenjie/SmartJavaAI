package cn.smartjavaai.face.vector.config;


import cn.smartjavaai.face.enums.IdStrategy;
import cn.smartjavaai.face.enums.VectorDBType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Milvus配置类
 * @author smartjavaai
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MilvusConfig extends VectorDBConfig {

    /**
     * 服务器地址
     */
    private String host = "localhost";

    /**
     * 服务器端口
     */
    private int port = 19530;

    /**
     * 索引类型
     */
    private IndexType indexType = IndexType.IVF_FLAT;

    /**
     * 聚类数量，用于IVF索引
     */
    private int nlist = 1024;

    /**
     * 向量维度
     */
    private int dimension;

    /**
     * ID策略
     */
    private IdStrategy idStrategy = IdStrategy.AUTO;


    /**
     * 相似度计算方式
     */
    private MetricType metricType;

    /**
     * 集合名称
     */
    private String collectionName;

    /**
     * 是否使用内存缓存
     */
    private boolean useMemoryCache = true;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 构造函数
     */
    public MilvusConfig() {
        setType(VectorDBType.MILVUS);
    }

    /**
     * 构造函数
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public MilvusConfig(String host, int port) {
        this();
        this.host = host;
        this.port = port;
    }
}
