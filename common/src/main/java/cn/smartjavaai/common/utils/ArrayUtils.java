package cn.smartjavaai.common.utils;

/**
 * 数组工具类
 * @author dwj
 * @date 2025/6/27
 */
public class ArrayUtils {

    /**
     * 求和并找到最大值的索引
     * @param arr1
     * @param arr2
     * @return
     */
    public static int sumAndFindMaxIndex(float[] arr1, float[] arr2, int length) {
        float[] sum = new float[length];

        // 处理可能为null的情况，null当作全0数组处理
        for (int i = 0; i < length; i++) {
            float v1 = (arr1 != null && arr1.length > i) ? arr1[i] : 0f;
            float v2 = (arr2 != null && arr2.length > i) ? arr2[i] : 0f;
            sum[i] = v1 + v2;
        }

        // 找最大值索引
        int maxIndex = 0;
        float maxValue = sum[0];
        for (int i = 1; i < length; i++) {
            if (sum[i] > maxValue) {
                maxValue = sum[i];
                maxIndex = i;
            }
        }

        // 返回最大值的索引
        return maxIndex;
    }
}
