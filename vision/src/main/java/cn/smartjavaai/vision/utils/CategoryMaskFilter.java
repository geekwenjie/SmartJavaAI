package cn.smartjavaai.vision.utils;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.output.CategoryMask;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * CategoryMask过滤器
 * @author dwj
 */
@Data
public class CategoryMaskFilter {

    private List<String> allowedClasses;

    /**
     * 构造函数，初始化允许的分类列表和置信度阈值
     *
     * @param allowedClasses 允许的分类列表，null表示不过滤分类
     */
    public CategoryMaskFilter(List<String> allowedClasses) {
        this.allowedClasses = allowedClasses != null ? new ArrayList<>(allowedClasses) : null;
    }


    /**
     * 对给定的CategoryMask进行过滤，只保留允许的分类。
     *
     * @param categoryMask 待过滤的CategoryMask
     * @return 过滤后的CategoryMask
     */
    public CategoryMask filter(CategoryMask categoryMask) {
        if (categoryMask == null) {
            throw new NullPointerException("Input categoryMask cannot be null");
        }

        // If allowedClasses is null, return a copy of the original CategoryMask
        if (allowedClasses == null) {
            List<String> classNamesCopy = new ArrayList<>(categoryMask.getClasses());
            int[][] maskCopy = copyMask(categoryMask.getMask());
            return new CategoryMask(classNamesCopy, maskCopy);
        }

        // Get original class names and mask
        List<String> originalClassNames = categoryMask.getClasses();
        int[][] originalMask = categoryMask.getMask();

        // Create a new class names list for allowed classes
        List<String> filteredClassNames = new ArrayList<>();

        // Map original indices to new indices for allowed classes
        List<Integer> allowedOriginalIndices = new ArrayList<>();
        for (int i = 0; i < originalClassNames.size(); i++) {
            String className = originalClassNames.get(i);
            if (allowedClasses.contains(className)) {
                filteredClassNames.add(className);
                allowedOriginalIndices.add(i);
            }
        }

        // Create a new mask with the same dimensions
        int height = originalMask.length;
        int width = (height > 0) ? originalMask[0].length : 0;
        int[][] filteredMask = new int[height][width];

        // Update mask: remap pixels of allowed classes to new indices, others to 0
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int originalIndex = originalMask[h][w];
                int newIndex = allowedOriginalIndices.indexOf(originalIndex);
                if (newIndex != -1) {
                    filteredMask[h][w] = newIndex;
                } else {
                    filteredMask[h][w] = 0; // Background
                }
            }
        }

        return new CategoryMask(filteredClassNames, filteredMask);
    }

    /**
     * Helper method to copy the mask array.
     *
     * @param originalMask the original mask to copy
     * @return a deep copy of the mask
     */
    private int[][] copyMask(int[][] originalMask) {
        int height = originalMask.length;
        int width = (height > 0) ? originalMask[0].length : 0;
        int[][] copy = new int[height][width];
        for (int h = 0; h < height; h++) {
            System.arraycopy(originalMask[h], 0, copy[h], 0, width);
        }
        return copy;
    }


}
