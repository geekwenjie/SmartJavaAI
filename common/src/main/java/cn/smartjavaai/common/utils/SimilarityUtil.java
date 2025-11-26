package cn.smartjavaai.common.utils;


import cn.smartjavaai.common.enums.SimilarityType;

/**
 * 特征相似度计算工具类
 * 支持三种计算方式：IP（内积）、L2（欧氏距离）、COSINE（余弦相似度）
 * 所有计算结果归一化到[0,1]范围
 */
public class SimilarityUtil {

    /**
     * 计算特征相似度
     * @param features1 特征向量1
     * @param features2 特征向量2
     * @param similarityType 计算类型 (IP, L2, COSINE)
     * @param normalizeScore 是否归一化结果到 [0,1]
     * @return 相似度
     */
    public static float calculate(float[] features1, float[] features2,
                                  SimilarityType similarityType,
                                  boolean normalizeScore) {
        validateInput(features1, features2);

        switch (similarityType) {
            case IP:
                return innerProductSimilarity(features1, features2, normalizeScore);
            case L2:
                return euclideanSimilarity(features1, features2, normalizeScore);
            case COSINE:
                return cosineSimilarity(features1, features2, normalizeScore);
            default:
                throw new IllegalArgumentException("不支持的相似度计算类型: " + similarityType);
        }
    }

    // ================ 私有计算方法 ================

    /**
     * 计算内积相似度（归一化到[0,1]）
     * 适用于归一化向量（结果范围[-1,1] -> [0,1]）
     */
    private static float innerProductSimilarity(float[] v1, float[] v2, boolean normalize) {
        float dot = dotProduct(v1, v2);
        return normalize ? (dot + 1.0f) / 2.0f : dot;
    }

    /**
     * 计算欧氏距离相似度（归一化到[0,1]）
     * 距离越小相似度越高，距离为0时相似度为1
     */
    private static float euclideanSimilarity(float[] v1, float[] v2, boolean normalize) {
        float dist = euclideanDistance(v1, v2);
        return normalize ? 1.0f / (1.0f + dist) : dist;
    }

    /**
     * 计算余弦相似度（归一化到[0,1]）
     * 适用于非归一化向量（结果范围[-1,1] -> [0,1]）
     */
    private static float cosineSimilarity(float[] v1, float[] v2, boolean normalize) {
        float dot = dotProduct(v1, v2);
        float norm1 = vectorNorm(v1);
        float norm2 = vectorNorm(v2);

        if (norm1 <= 0 || norm2 <= 0) {
            return 0.0f;
        }

        float cosine = dot / (norm1 * norm2);
        return normalize ? (cosine + 1.0f) / 2.0f : cosine;
    }

    // ================ 基础向量操作 ================

    /**
     * 计算点积（内积）
     */
    public static float dotProduct(float[] v1, float[] v2) {
        float sum = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            sum += v1[i] * v2[i];
        }
        return sum;
    }

    /**
     * 计算欧氏距离
     */
    public static float euclideanDistance(float[] v1, float[] v2) {
        float sumSquaredDiff = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            float diff = v1[i] - v2[i];
            sumSquaredDiff += diff * diff;
        }
        return (float) Math.sqrt(sumSquaredDiff);
    }

    /**
     * 计算向量模长
     */
    public static float vectorNorm(float[] vector) {
        float sum = 0.0f;
        for (float v : vector) {
            sum += v * v;
        }
        return (float) Math.sqrt(sum);
    }

    // ================ 输入验证 ================

    /**
     * 验证输入向量
     */
    private static void validateInput(float[] v1, float[] v2) {
        if (v1 == null || v2 == null) {
            throw new IllegalArgumentException("特征向量不能为null");
        }

        if (v1.length == 0 || v2.length == 0) {
            throw new IllegalArgumentException("特征向量不能为空");
        }

        if (v1.length != v2.length) {
            throw new IllegalArgumentException("特征向量长度不一致: " +
                    v1.length + " vs " + v2.length);
        }
    }
}
