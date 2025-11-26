package cn.smartjavaai.vision.utils;

import cn.smartjavaai.obb.entity.ObbResult;
import cn.smartjavaai.obb.entity.YoloRotatedBox;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dwj
 * @date 2025/8/24
 */
public class ObbResultFilter {

    private List<String> allowedClasses;
    private int topK;

    public ObbResultFilter(List<String> allowedClasses, int topK) {
        this.allowedClasses = allowedClasses;
        this.topK = topK;
    }

    public ObbResult filter(ObbResult result) {
        if (result == null || result.getRotatedBoxeList() == null) {
            return new ObbResult(Collections.emptyList());
        }

        List<YoloRotatedBox> filtered = result.getRotatedBoxeList();
        if(CollectionUtils.isNotEmpty(allowedClasses)){
            filtered = result.getRotatedBoxeList().stream()
                    .filter(box -> allowedClasses.contains(box.className))
                    .collect(Collectors.toList());
        }
        // 2. 按 score 排序
//        filtered.sort((a, b) -> Float.compare(b.score, a.score));

        // 3. 取前 topK
        if (topK > 0 && filtered.size() > topK) {
            filtered = filtered.subList(0, topK);
        }

        return new ObbResult(filtered);
    }

}
