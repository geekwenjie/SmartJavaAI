package cn.smartjavaai.common.entity.face;
import lombok.Data;

/**
 * 人脸向量搜索结果
 * @author dwj
 */
@Data
public class FaceSearchResult {

    /**
     * 向量ID
     */
    private String id;

    /**
     * 相似度分数
     */
    private float similarity;

    /**
     * 元数据
     */
    private String metadata;

    /**
     * 构造函数
     * @param id 向量ID
     * @param similarity 相似度分数
     * @param metadata 元数据
     */
    public FaceSearchResult(String id, float similarity, String metadata) {
        this.id = id;
        this.similarity = similarity;
        this.metadata = metadata;
    }
}
