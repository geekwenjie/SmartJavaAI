package cn.smartjavaai.ocr.idcard;

import cn.smartjavaai.ocr.entity.IdCardBackInfo;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.utils.BoxUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 身份证反面解析逻辑。
 *
 * 解析：
 * - 签发机关
 * - 有效期限（起止日期 / 长期）
 */
public class IdCardBackParser implements IdCardParser<IdCardBackInfo> {

    private static final Pattern VALID_PERIOD_PATTERN = Pattern.compile(
            "((?:19|20)\\d{2})[./-]?((?:1[0-2])|(?:0[1-9]))[./-]?((?:3[01])|(?:[12]\\d)|(?:0[1-9]))" +
                    "\\s*[-一至到~]\\s*" +
                    "(((?:19|20)\\d{2})[./-]?((?:1[0-2])|(?:0[1-9]))[./-]?((?:3[01])|(?:[12]\\d)|(?:0[1-9]))|长期)"
    );

    @Override
    public IdCardBackInfo parse(OcrInfo ocrInfo) {
        IdCardBackInfo info = new IdCardBackInfo();
        if (ocrInfo == null || ocrInfo.getLineList() == null || ocrInfo.getLineList().isEmpty()) {
            return info;
        }

        List<OcrItem> allItems = ocrInfo.getLineList().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        allItems = filterSmallBoxes(allItems);

        if (allItems.isEmpty()) {
            return info;
        }
        Map<OcrBox, OcrItem> itemByBox = buildBoxItemMap(allItems);

        OcrItem authorityLabel = findLabel(allItems, Arrays.asList("签发机关", "签发機关", "签发机関", "签发"));
        if (authorityLabel != null) {
            String authority = findIssuingAuthority(authorityLabel, allItems, itemByBox);
            info.setIssuingAuthority(authority);
        }

        OcrItem validLabel = findLabel(allItems, Arrays.asList("有效期限", "有效期", "有效期限:", "有效期限："));
        if (validLabel != null) {
            ValidPeriod validPeriod = findValidPeriod(validLabel, allItems, itemByBox);
            if (validPeriod != null) {
                info.setValidFrom(validPeriod.validFrom);
                info.setValidTo(validPeriod.validTo);
            }
        }

        if (info.getIssuingAuthority() == null || info.getIssuingAuthority().isEmpty()) {
            info.setIssuingAuthority(findIssuingAuthorityByGlobalFallback(allItems));
        }
        if (info.getValidFrom() == null || info.getValidTo() == null) {
            ValidPeriod fallback = findValidPeriodByGlobalFallback(allItems);
            if (fallback != null) {
                if (info.getValidFrom() == null) {
                    info.setValidFrom(fallback.validFrom);
                }
                if (info.getValidTo() == null) {
                    info.setValidTo(fallback.validTo);
                }
            }
        }

        return info;
    }

    private String findIssuingAuthority(OcrItem authorityLabel, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        String selfAuthority = extractIssuingAuthority(authorityLabel.getText());
        if (selfAuthority != null) {
            return selfAuthority;
        }

        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OcrBox anchorBox = authorityLabel.getOcrBox();
        String authority = findAuthorityByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.RIGHT, 3);
        if (authority != null) {
            return authority;
        }
        authority = findAuthorityByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.DOWN, 3);
        if (authority != null) {
            return authority;
        }
        authority = findAuthorityByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.UP, 2);
        if (authority != null) {
            return authority;
        }
        return null;
    }

    private String findAuthorityByDirection(OcrBox anchorBox,
                                            Map<OcrBox, OcrItem> itemByBox,
                                            List<OcrBox> boxList,
                                            BoxUtils.Direction direction,
                                            int limit) {
        List<OcrBox> neighbors = BoxUtils.findNearestBoxes(anchorBox, boxList, direction, limit);
        if (neighbors == null || neighbors.isEmpty()) {
            return null;
        }

        List<OcrItem> neighborItems = neighbors.stream()
                .map(itemByBox::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (OcrItem item : neighborItems) {
            String authority = extractIssuingAuthority(item.getText());
            if (authority != null) {
                return authority;
            }
        }

        String merged = neighborItems.stream()
                .map(OcrItem::getText)
                .filter(Objects::nonNull)
                .map(this::normalizeText)
                .collect(Collectors.joining());
        return extractIssuingAuthority(merged);
    }

    private String extractIssuingAuthority(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String clean = normalizeText(text)
                .replace("签发机关", "")
                .replace("签发機关", "")
                .replace("签发机関", "")
                .replace("签发", "");
        if (clean.isEmpty()) {
            return null;
        }
        if (clean.contains("有效期限") || clean.contains("有效期")) {
            return null;
        }
        return clean;
    }

    private ValidPeriod findValidPeriod(OcrItem validLabel, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        ValidPeriod selfPeriod = extractValidPeriod(validLabel.getText());
        if (selfPeriod != null) {
            return selfPeriod;
        }

        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OcrBox anchorBox = validLabel.getOcrBox();
        ValidPeriod period = findValidPeriodByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.RIGHT, 4);
        if (period != null) {
            return period;
        }
        period = findValidPeriodByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.DOWN, 4);
        if (period != null) {
            return period;
        }
        period = findValidPeriodByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.UP, 2);
        if (period != null) {
            return period;
        }
        return null;
    }

    private ValidPeriod findValidPeriodByDirection(OcrBox anchorBox,
                                                   Map<OcrBox, OcrItem> itemByBox,
                                                   List<OcrBox> boxList,
                                                   BoxUtils.Direction direction,
                                                   int limit) {
        List<OcrBox> neighbors = BoxUtils.findNearestBoxes(anchorBox, boxList, direction, limit);
        if (neighbors == null || neighbors.isEmpty()) {
            return null;
        }

        List<OcrItem> neighborItems = neighbors.stream()
                .map(itemByBox::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (OcrItem item : neighborItems) {
            ValidPeriod period = extractValidPeriod(item.getText());
            if (period != null) {
                return period;
            }
        }

        String merged = neighborItems.stream()
                .map(OcrItem::getText)
                .filter(Objects::nonNull)
                .map(this::normalizeText)
                .collect(Collectors.joining());
        return extractValidPeriod(merged);
    }

    private ValidPeriod extractValidPeriod(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String clean = normalizeText(text)
                .replace("有效期限", "")
                .replace("有效期", "");

        Matcher matcher = VALID_PERIOD_PATTERN.matcher(clean);
        if (!matcher.find()) {
            return null;
        }

        String from = normalizeDate(matcher.group(1), matcher.group(2), matcher.group(3));
        if (from == null) {
            return null;
        }

        String toRaw = matcher.group(4);
        String to = "长期".equals(toRaw)
                ? "长期"
                : normalizeDate(matcher.group(5), matcher.group(6), matcher.group(7));
        if (to == null) {
            return null;
        }

        return new ValidPeriod(from, to);
    }

    private String normalizeDate(String year, String month, String day) {
        if (year == null || month == null || day == null) {
            return null;
        }
        return year + "-" + month + "-" + day;
    }

    private String findIssuingAuthorityByGlobalFallback(List<OcrItem> allItems) {
        for (OcrItem item : allItems) {
            String authority = extractIssuingAuthority(item.getText());
            if (authority != null && (authority.contains("公安局") || authority.contains("分局") || authority.contains("机关"))) {
                return authority;
            }
        }
        return null;
    }

    private ValidPeriod findValidPeriodByGlobalFallback(List<OcrItem> allItems) {
        List<String> texts = new ArrayList<>();
        for (OcrItem item : allItems) {
            if (item != null && item.getText() != null) {
                ValidPeriod period = extractValidPeriod(item.getText());
                if (period != null) {
                    return period;
                }
                texts.add(normalizeText(item.getText()));
            }
        }
        return extractValidPeriod(String.join("", texts));
    }

    private OcrItem findLabel(List<OcrItem> allItems, List<String> keywords) {
        for (OcrItem item : allItems) {
            String text = item.getText();
            if (text == null) {
                continue;
            }
            String clean = normalizeText(text);
            for (String keyword : keywords) {
                if (clean.contains(keyword)) {
                    return item;
                }
            }
        }
        return null;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(" ", "")
                .replace("一", "-")
                .replace("到", "-")
                .replace("至", "-")
                .replace("~", "-")
                .replace("—", "-")
                .replace("－", "-");
    }

    private List<OcrItem> filterSmallBoxes(List<OcrItem> items) {
        List<OcrItem> result = IdCardOcrUtils.filterSmallBoxes(items, 0.15);
        return result;
    }

    private Map<OcrBox, OcrItem> buildBoxItemMap(List<OcrItem> items) {
        Map<OcrBox, OcrItem> itemByBox = new IdentityHashMap<>();
        if (items == null) {
            return itemByBox;
        }
        for (OcrItem item : items) {
            if (item != null && item.getOcrBox() != null) {
                itemByBox.put(item.getOcrBox(), item);
            }
        }
        return itemByBox;
    }

    private static class ValidPeriod {
        private final String validFrom;
        private final String validTo;

        private ValidPeriod(String validFrom, String validTo) {
            this.validFrom = validFrom;
            this.validTo = validTo;
        }
    }
}
