package cn.smartjavaai.face.vector.config;

import cn.smartjavaai.face.enums.VectorDBType;
import lombok.Data;

/**
 * 向量数据库基础配置
 * @author dwj
 */
@Data
public abstract class VectorDBConfig {

    /**
     * 向量数据库类型
     */
    private VectorDBType type;



}
