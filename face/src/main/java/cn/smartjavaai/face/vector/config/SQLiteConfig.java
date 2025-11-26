package cn.smartjavaai.face.vector.config;

import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.face.enums.VectorDBType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dwj
 * @date 2025/5/31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SQLiteConfig extends VectorDBConfig {

    /**
     * 数据库路径(包含文件名称)
     */
    private String dbPath;

    /**
     * 相似度计算方式
     */
    private SimilarityType similarityType;


    public SQLiteConfig() {
        setType(VectorDBType.SQLITE);
    }
}
