package cn.smartjavaai.ocr.idcard;

import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 身份证 OCR 解析相关的通用工具。
 */
final class IdCardOcrUtils {

    static final Set<String> ETHNIC_SET = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗",
            "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山",
            "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬",
            "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒",
            "乌孜别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙",
            "鄂伦春", "赫哲", "门巴", "珞巴", "基诺"
    )));

    private IdCardOcrUtils() {
    }

    static List<OcrItem> filterSmallBoxes(List<OcrItem> items, double factor) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        List<Double> areas = items.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .map(IdCardOcrUtils::estimateBoxArea)
                .sorted()
                .collect(Collectors.toList());

        if (areas.size() < 5) {
            return items;
        }

        double median = areas.get(areas.size() / 2);
        double threshold = median * factor;
        List<OcrItem> result = items.stream()
                .filter(it -> it.getOcrBox() == null || estimateBoxArea(it.getOcrBox()) >= threshold)
                .collect(Collectors.toList());
        return result.isEmpty() ? items : result;
    }

    static double estimateBoxArea(OcrBox box) {
        if (box == null) {
            return 0.0;
        }
        float[] pts = box.toFloatArray();
        float minX = Math.min(Math.min(pts[0], pts[2]), Math.min(pts[4], pts[6]));
        float minY = Math.min(Math.min(pts[1], pts[3]), Math.min(pts[5], pts[7]));
        float maxX = Math.max(Math.max(pts[0], pts[2]), Math.max(pts[4], pts[6]));
        float maxY = Math.max(Math.max(pts[1], pts[3]), Math.max(pts[5], pts[7]));
        double width = Math.max(1.0, maxX - minX);
        double height = Math.max(1.0, maxY - minY);
        return width * height;
    }
}
