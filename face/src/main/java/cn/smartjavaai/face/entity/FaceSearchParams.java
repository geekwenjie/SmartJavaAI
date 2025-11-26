package cn.smartjavaai.face.entity;

import lombok.Data;

/**
 * 人脸查询参数
 * @author dwj
 * @date 2025/5/30
 */
@Data
public class FaceSearchParams {

    /**
     * 搜索结果数量
     */
    private Integer topK = 1;

    /**
     * 搜索阈值
     */
    private Float threshold;

    /**
     * 是否对查询结果进行归一化
     */
    private Boolean normalizeSimilarity;


    public FaceSearchParams() {
    }

    public FaceSearchParams(Integer topK, Float threshold) {
        this.topK = topK;
        this.threshold = threshold;
    }

    public FaceSearchParams(Integer topK, Float threshold, Boolean normalizeSimilarity) {
        this.topK = topK;
        this.threshold = threshold;
        this.normalizeSimilarity = normalizeSimilarity;
    }
}

