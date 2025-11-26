package cn.smartjavaai.vision.utils;

import ai.djl.modality.Classifications;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classification过滤器
 * @author dwj
 */
@Data
public class ClassificationFilter {

    private List<String> allowedClasses;
    private float threshold;

    /**
     * 构造函数，初始化允许的分类列表和置信度阈值
     *
     * @param allowedClasses 允许的分类列表，null表示不过滤分类
     * @param threshold     置信度阈值
     */
    public ClassificationFilter(List<String> allowedClasses, float threshold) {
        this.allowedClasses = allowedClasses != null ? new ArrayList<>(allowedClasses) : null;
        this.threshold = threshold;
    }

    /**
     * 过滤分类结果并返回新的Classifications对象
     *
     * @param classifications 原始分类结果
     * @return 过滤后的Classifications对象
     */
    public Classifications filter(Classifications classifications) {
        // 获取原始分类和概率
        List<String> originalClasses = classifications.getClassNames();
        List<Double> originalProbabilities = classifications.getProbabilities();

        // 过滤分类和概率
        List<String> filteredClasses = new ArrayList<>();
        List<Double> filteredProbabilities = new ArrayList<>();

        for (int i = 0; i < originalClasses.size(); i++) {
            String className = originalClasses.get(i);
            double probability = originalProbabilities.get(i);

            // 检查是否满足置信度阈值和允许的分类列表
            if (probability >= threshold && (allowedClasses == null || allowedClasses.contains(className))) {
                filteredClasses.add(className);
                filteredProbabilities.add(probability);
            }
        }

        // 创建新的Classifications对象
        return new Classifications(filteredClasses, filteredProbabilities);
    }


}
