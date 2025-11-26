package cn.smartjavaai.face.vector.constant;

/**
 * 向量数据库常量类
 * @author dwj
 */
public class VectorDBConstants {
    /**
     * 字段名称常量
     */
    public static class FieldNames {
        /** ID字段名 */
        public static final String ID_FIELD = "id";
        /** 向量字段名 */
        public static final String VECTOR_FIELD = "vector";
        /** 元数据字段名 */
        public static final String METADATA_FIELD = "metadata";
    }

    /**
     * 默认配置常量
     */
    public static class Defaults {
        /** 默认搜索探针数 */
        public static final int DEFAULT_SEARCH_PARAM_NPROBE = 10;
        /** 默认向量维度 */
        public static final int DEFAULT_VECTOR_DIMENSION = 512;
        /** 默认元数据最大长度 */
        public static final int DEFAULT_METADATA_MAX_LENGTH = 32 * 1024;
        /** 默认ID字段最大长度 */
        public static final int DEFAULT_ID_MAX_LENGTH = 36;

        /**
         * 默认集合名称
         */
        public static final String DEFAULT_COLLECTION_NAME = "face";
    }

    /**
     * 搜索参数常量
     */
    public static class SearchParams {
        /** 默认相似度阈值 */
        public static final float DEFAULT_SIMILARITY_THRESHOLD = 0.7f;
        /** 默认返回TOP-K结果数 */
        public static final int DEFAULT_TOP_K = 10;
    }
}
