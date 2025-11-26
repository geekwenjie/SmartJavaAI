package cn.smartjavaai.translation.utils;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import cn.smartjavaai.translation.config.NllbSearchConfig;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 *
 *
 * @author lwx
 * @date 2025/4/22
 */
public final class TokenUtils {

    private TokenUtils() {
    }

    /**
     * 语言解码
     *
     * @param tokenizer
     * @param output
     * @return
     */
    public static String decode(NllbSearchConfig config, HuggingFaceTokenizer tokenizer, NDArray output) {
        long[] outputIds = output.toLongArray();
        ArrayList<Long> outputIdsList = new ArrayList<>();

        for (long id : outputIds) {
            if (id == config.getEosTokenId() || id==config.getSrcLangId() || id==config.getForcedBosTokenId()) {
                continue;
            }
            outputIdsList.add(id);
        }

        Long[] objArr =  outputIdsList.toArray(new Long[0]);
        long[] ids = new long[objArr.length];
        for (int i = 0; i < objArr.length; i++) {
            ids[i] = objArr[i];
        }
        String text = tokenizer.decode(ids);
        return text;
    }

    /**
     * Token 解码
     * 根据语言的类型更新下面的方法
     *
     * @param reverseMap
     * @param outputIds
     * @return
     */
    public static String decode(Map<Long, String> reverseMap, long[] outputIds) {
        int[] intArray = Arrays.stream(outputIds).mapToInt(l -> (int) l).toArray();

        StringBuffer sb = new StringBuffer();
        for (int value : intArray) {
            // 65000 <pad>
            // 0 </s>
            if (value == 65000 || value == 0 || value == 8)
                continue;
            String text = reverseMap.get(Long.valueOf(value));
            sb.append(text);
        }

        String result = sb.toString();
        result = result.replaceAll("▁"," ");
        return result;
    }
}
