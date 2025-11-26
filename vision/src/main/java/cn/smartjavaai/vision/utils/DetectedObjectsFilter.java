package cn.smartjavaai.vision.utils;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Classification过滤器
 * @author dwj
 */
@Data
public class DetectedObjectsFilter {

    private List<String> allowedClasses;
    private float threshold;
    private int topK;

    /**
     * 构造函数，初始化允许的分类列表和置信度阈值
     *
     * @param allowedClasses 允许的分类列表，null表示不过滤分类
     * @param threshold     置信度阈值
     */
    public DetectedObjectsFilter(List<String> allowedClasses, float threshold) {
        this.allowedClasses = allowedClasses != null ? new ArrayList<>(allowedClasses) : null;
        this.threshold = threshold;
    }

    public DetectedObjectsFilter(List<String> allowedClasses, float threshold, int topK) {
        this.allowedClasses = allowedClasses;
        this.threshold = threshold;
        this.topK = topK;
    }

    /**
     * 对给定的 DetectedObjects 进行过滤，返回过滤后的结果
     *
     * @param detectedObjects 待过滤的 DetectedObjects 对象
     * @return 过滤后的 DetectedObjects 对象
     */
    public DetectedObjects filter(DetectedObjects detectedObjects) {
        if (Objects.isNull(detectedObjects) || detectedObjects.getNumberOfObjects() == 0) {
            return detectedObjects;
        }
        List<DetectedObjects.DetectedObject> items = detectedObjects.items();
        // 按照允许的类别进行过滤
        List<DetectedObjects.DetectedObject> filtered = new ArrayList<>();
        //过滤类别
        if(!CollectionUtils.isEmpty(allowedClasses)){
            for (DetectedObjects.DetectedObject obj : items) {
                if(allowedClasses.contains(obj.getClassName())){
                    filtered.add(obj);
                }
            }
        }else{
            filtered = items;
        }
        if(topK > 0 && filtered.size() > topK){
            // 按照概率进行排序
            filtered.sort(Comparator.comparingDouble(Classifications.Classification::getProbability).reversed());
            filtered = filtered.subList(0, topK);
        }
        // 构建新的 DetectedObjects 返回
        List<String> names = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        List<BoundingBox> boxes = new ArrayList<>();

        for (DetectedObjects.DetectedObject obj : filtered) {
            names.add(obj.getClassName());
            probs.add(obj.getProbability());
            boxes.add(obj.getBoundingBox());
        }
        return new DetectedObjects(names, probs, boxes);
    }


}
