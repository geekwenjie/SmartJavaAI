package cn.smartjavaai.face.vector.core;



import cn.smartjavaai.face.vector.config.MilvusConfig;
import cn.smartjavaai.face.vector.config.SQLiteConfig;
import cn.smartjavaai.face.vector.config.VectorDBConfig;
import cn.smartjavaai.face.vector.exception.VectorDBException;

/**
 * 向量数据库工厂类
 * 用于创建不同类型的向量数据库客户端
 * @author dwj
 */
public class VectorDBFactory {

    private VectorDBFactory() {
        // 私有构造函数，防止实例化
    }

    /**
     * 创建向量数据库客户端
     * @param config 配置信息
     * @return 向量数据库客户端
     * @throws VectorDBException 创建异常
     */
    public static VectorDBClient createClient(VectorDBConfig config) {
        if (config == null) {
            throw new VectorDBException("配置不能为空");
        }
        VectorDBClient client;

        switch (config.getType()) {
            case SQLITE:
                if (!(config instanceof SQLiteConfig)) {
                    throw new VectorDBException("SQLite类型需要SQLiteConfig配置");
                }
                client = new SQLiteClient((SQLiteConfig) config);
                break;
            case MILVUS:
                if (!(config instanceof MilvusConfig)) {
                    throw new VectorDBException("Milvus类型需要MilvusConfig配置");
                }
                client = new MilvusClient((MilvusConfig) config);
                break;
            // 未来可以在这里添加其他向量数据库的支持
            default:
                throw new VectorDBException("不支持的向量数据库类型: " + config.getType());
        }
        return client;
    }
}
