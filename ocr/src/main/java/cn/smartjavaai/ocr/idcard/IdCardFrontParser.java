package cn.smartjavaai.ocr.idcard;

import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.ocr.entity.IdCardFrontInfo;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.utils.BoxUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 身份证正面解析逻辑。
 *
 * 从 OCR 结果中解析：
 * - 姓名（兼容“姓名+值在同一检测框”与“键值分框”的情况）
 * - 性别
 * - 民族
 * - 出生日期
 * - 公民身份号码
 * - 住址
 */
@Slf4j
public class IdCardFrontParser implements IdCardParser<IdCardFrontInfo> {

    private static final Pattern BIRTHDAY_WITH_SEPARATORS = Pattern.compile(
            "((?:19|20)\\d{2})[年/._-]?((?:1[0-2])|(?:0?[1-9]))[月/._-]?((?:3[01])|(?:[12]\\d)|(?:0?[1-9]))日?"
    );

    private static final Pattern BIRTHDAY_COMPACT = Pattern.compile(
            "((?:19|20)\\d{2})((?:1[0-2])|(?:0[1-9]))((?:3[01])|(?:[12]\\d)|(?:0[1-9]))"
    );

    @Override
    public IdCardFrontInfo parse(OcrInfo ocrInfo) {
        IdCardFrontInfo info = new IdCardFrontInfo();
        if (ocrInfo == null || ocrInfo.getLineList() == null || ocrInfo.getLineList().isEmpty()) {
            return info;
        }

        // 扁平化所有 item
        List<OcrItem> allItems = ocrInfo.getLineList().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 过滤明显过小的检测框（噪声）
        allItems = filterSmallBoxes(allItems);

        // 可用的检测框列表：解析出字段后逐步移除，减少后续遍历量
        List<OcrItem> remainingItems = new ArrayList<>(allItems);
        Map<OcrBox, OcrItem> itemByBox = buildBoxItemMap(remainingItems);

        // 1. 姓名（兼容“标签+值同框”以及右侧单独值框）
        OcrItem nameLabel = findLabel(remainingItems, Arrays.asList("姓名", "姓名:", "姓名："));
        if (nameLabel != null) {
            StringBuilder nameSb = new StringBuilder();
            if (nameLabel.getText() != null) {
                nameSb.append(nameLabel.getText());
            }
            // 右侧最近一个框（可能是姓名值）
            OcrItem rightItem = findNearestRightItem(nameLabel, remainingItems, itemByBox);
            if (rightItem != null && rightItem.getText() != null) {
                nameSb.append(rightItem.getText());
            }

            // 过滤：只保留中文字符
            String nameText = nameSb.toString()
                    .replace(" ", "")
                    .replaceAll("[^\\u4e00-\\u9fa5]", "");

            // 提取"姓名"后面的内容作为名字
            String name = extractNameAfterLabel(nameText);
            if (name != null && !name.isEmpty()) {
                info.setName(name);
                remainingItems.remove(nameLabel);
                if (rightItem != null) {
                    remainingItems.remove(rightItem);
                }
                itemByBox = buildBoxItemMap(remainingItems);
            }
        }

        // 2. 性别（优先右 → 上 → 下 → 全局）
        OcrItem genderLabel = findLabel(remainingItems, Arrays.asList("性别", "性别:", "性别："));
        if (genderLabel != null) {
            String gender = findGenderAroundLabel(genderLabel, remainingItems, itemByBox);
            if (gender != null) {
                info.setGender(gender);
                final String g = gender;
                // 移除性别相关检测框（若同时包含民族信息则保留用于民族解析）
                remainingItems.removeIf(it -> {
                    String t = it.getText();
                    if (t == null) {
                        return false;
                    }
                    String clean = t.replace(" ", "");
                    if (clean.contains("性别") && !clean.contains("民族")) {
                        return true;
                    }
                    String cg = cleanGender(t);
                    return g.equals(cg) && !clean.contains("民族");
                });
                itemByBox = buildBoxItemMap(remainingItems);
            }
        }

        // 3. 民族（策略同性别）
        OcrItem ethnicLabel = findLabel(remainingItems, Arrays.asList("民族", "民族:", "民族："));
        if (ethnicLabel != null) {
            String ethnicity = findEthnicityAroundLabel(ethnicLabel, remainingItems, itemByBox);
            if (ethnicity != null) {
                info.setEthnicity(ethnicity);
                final String eth = ethnicity;
                remainingItems.removeIf(it -> {
                    String t = it.getText();
                    if (t == null) {
                        return false;
                    }
                    String clean = t.replace(" ", "");
                    if (clean.contains("民族")) {
                        return true;
                    }
                    String e = extractEthnicity(t);
                    return eth.equals(e);
                });
                itemByBox = buildBoxItemMap(remainingItems);
            }
        }

        // 4. 公民身份号码
        OcrItem idSourceItem = null;
        OcrItem idRightItem = null;
        for (OcrItem item : remainingItems) {
            String text = normalizeText(item.getText());
            if (text.contains("公民身份号码") || text.contains("公民身份號碼") || text.toUpperCase().contains("ID")) {
                String idInBox = extractIdNumber(text);
                if (idInBox == null) {
                    OcrItem idRight = findNearestRightItem(item, remainingItems, itemByBox);
                    if (idRight != null) {
                        idInBox = extractIdNumber(normalizeText(idRight.getText()));
                        idRightItem = idRight;
                    }
                }
                if (idInBox != null) {
                    info.setIdNumber(idInBox);
                    idSourceItem = item;
                    break;
                }
            }
        }
        if (idSourceItem != null) {
            remainingItems.remove(idSourceItem);
        }
        if (idRightItem != null) {
            remainingItems.remove(idRightItem);
        }
        if (idSourceItem != null || idRightItem != null) {
            itemByBox = buildBoxItemMap(remainingItems);
        }

        // 5. 出生日期：考虑多框拆分 & 与标签同框等多种情况
        OcrItem birthLabel = findLabel(remainingItems, Arrays.asList("出生", "出生日期", "出生:", "出生："));
        if (birthLabel != null) {
            String birth = findBirthdayAroundLabel(birthLabel, remainingItems, itemByBox);
            if (birth != null) {
                info.setBirthday(birth);
                final String bFinal = birth;
                remainingItems.removeIf(it -> {
                    String t = it.getText();
                    if (t == null) {
                        return false;
                    }
                    String clean = t.replace(" ", "");
                    if (clean.contains("出生")) {
                        return true;
                    }
                    String ex = extractBirthday(t);
                    return bFinal.equals(ex);
                });
                itemByBox = buildBoxItemMap(remainingItems);
            }
        }

        // 如果生日没识别到，则尝试从身份证号码中推断
        if (info.getBirthday() == null && info.getIdNumber() != null && info.getIdNumber().length() >= 14) {
            String id = info.getIdNumber();
            String yyyyMMdd = id.substring(6, 14);
            info.setBirthday(formatBirthdayFromId(yyyyMMdd));
        }

        // 6. 住址（从“住址”右侧开始，向下若干行拼接）
        OcrItem addressLabel = findAddressLabel(remainingItems);
        if (addressLabel != null) {
            String addr = collectAddress(addressLabel, remainingItems, itemByBox);
            info.setAddress(addr);
        }

        return info;
    }

    // ----------------- 文本与 Box 相关的工具方法 -----------------

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(" ", "")
                .replaceAll("[^0-9Xx\\u4e00-\\u9fa5]", "");
    }

    /**
     * 从包含"姓名"的文本中提取姓名值（提取"姓名"后面的所有中文字符）
     */
    private String extractNameAfterLabel(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        int nameIndex = text.indexOf("姓名");
        if (nameIndex >= 0) {
            String name = text.substring(nameIndex + 2);
            return name.isEmpty() ? null : name;
        }
        int surnameIndex = text.indexOf("姓");
        if (surnameIndex >= 0 && surnameIndex < text.length() - 1) {
            String name = text.substring(surnameIndex + 1);
            return name.isEmpty() ? null : name;
        }
        int givenNameIndex = text.indexOf("名");
        if (givenNameIndex >= 0 && givenNameIndex < text.length() - 1) {
            String name = text.substring(givenNameIndex + 1);
            return name.isEmpty() ? null : name;
        }
        // 如果都没找到，返回原文本（可能已经是纯姓名）
        return text;
    }

    private String cleanGender(String text) {
        if (text == null) {
            return null;
        }
        String clean = text.replace(" ", "");
        if (clean.contains("男")) {
            return "男";
        }
        if (clean.contains("女")) {
            return "女";
        }
        return null;
    }

    /**
     * 按“右 → 上 → 下 → 全局”顺序查找性别信息
     */
    private String findGenderAroundLabel(OcrItem genderLabel, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        if (genderLabel == null || genderLabel.getOcrBox() == null || allItems == null || allItems.isEmpty()) {
            return null;
        }
        // 情况一：标签所在框本身包含“性别+男/女”
        String selfGender = cleanGender(genderLabel.getText());
        if (selfGender != null) {
            return selfGender;
        }

        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OcrBox anchorBox = genderLabel.getOcrBox();

        String gender = findGenderByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.RIGHT, 3);
        if (gender != null) {
            return gender;
        }
        gender = findGenderByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.UP, 3);
        if (gender != null) {
            return gender;
        }
        gender = findGenderByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.DOWN, 3);
        if (gender != null) {
            return gender;
        }

        return findNearbyFallback(anchorBox, allItems, this::cleanGender);
    }

    /**
     * 按指定方向在若干个最近框中查找性别
     */
    private String findGenderByDirection(OcrBox anchorBox,
                                         Map<OcrBox, OcrItem> itemByBox,
                                         List<OcrBox> boxList,
                                         BoxUtils.Direction direction,
                                         int limit) {
        List<OcrBox> neighbors = BoxUtils.findNearestBoxes(anchorBox, boxList, direction, limit);
        if (neighbors == null || neighbors.isEmpty()) {
            return null;
        }
        for (OcrBox neighbor : neighbors) {
            OcrItem item = itemByBox.get(neighbor);
            if (item == null) {
                continue;
            }
            String g = cleanGender(item.getText());
            if (g != null) {
                return g;
            }
        }
        return null;
    }

    /**
     * 按“右 → 上 → 下 → 全局”顺序查找民族信息
     */
    private String findEthnicityAroundLabel(OcrItem ethnicLabel, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        if (ethnicLabel == null || ethnicLabel.getOcrBox() == null || allItems == null || allItems.isEmpty()) {
            return null;
        }

        String selfEthnic = extractEthnicity(ethnicLabel.getText());
        if (selfEthnic != null) {
            return selfEthnic;
        }

        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OcrBox anchorBox = ethnicLabel.getOcrBox();

        String ethnicity = findEthnicityByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.RIGHT, 3);
        if (ethnicity != null) {
            return ethnicity;
        }
        ethnicity = findEthnicityByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.UP, 3);
        if (ethnicity != null) {
            return ethnicity;
        }
        ethnicity = findEthnicityByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.DOWN, 3);
        if (ethnicity != null) {
            return ethnicity;
        }

        return findNearbyFallback(anchorBox, allItems, this::extractEthnicity);
    }

    /**
     * 按指定方向在若干个最近框中查找民族
     */
    private String findEthnicityByDirection(OcrBox anchorBox,
                                            Map<OcrBox, OcrItem> itemByBox,
                                            List<OcrBox> boxList,
                                            BoxUtils.Direction direction,
                                            int limit) {
        List<OcrBox> neighbors = BoxUtils.findNearestBoxes(anchorBox, boxList, direction, limit);
        if (neighbors == null || neighbors.isEmpty()) {
            return null;
        }
        for (OcrBox neighbor : neighbors) {
            OcrItem item = itemByBox.get(neighbor);
            if (item == null) {
                continue;
            }
            String e = extractEthnicity(item.getText());
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    /**
     * 从文本中提取并纠正民族信息
     */
    private String extractEthnicity(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) {
            return null;
        }
        String cleanText = ocrText.replaceAll("[^\\u4e00-\\u9fa5]", "");
        cleanText = cleanText.replaceAll("^民族", "");
        for (String ethnic : IdCardOcrUtils.ETHNIC_SET) {
            if (cleanText.contains(ethnic)) {
                return ethnic;
            }
        }
        return null;
    }

    /**
     * 兜底时仅在标签附近寻找合法候选，避免被远处噪声或少数民族文字误带偏。
     */
    private String findNearbyFallback(OcrBox anchorBox,
                                      List<OcrItem> allItems,
                                      Function<String, String> extractor) {
        if (anchorBox == null || allItems == null || allItems.isEmpty() || extractor == null) {
            return null;
        }

        double anchorWidth = boxWidth(anchorBox);
        double anchorHeight = boxHeight(anchorBox);
        Point anchorCenter = boxCenter(anchorBox);

        OcrItem bestItem = null;
        double bestScore = Double.MAX_VALUE;

        for (OcrItem item : allItems) {
            if (item == null || item.getOcrBox() == null) {
                continue;
            }
            String extracted = extractor.apply(item.getText());
            if (extracted == null) {
                continue;
            }

            Point candidateCenter = boxCenter(item.getOcrBox());
            double dx = Math.abs(candidateCenter.getX() - anchorCenter.getX());
            double dy = Math.abs(candidateCenter.getY() - anchorCenter.getY());

            // 性别/民族通常紧邻标签，限制在局部窗口内做兜底。
            if (dx > anchorWidth * 5.0 || dy > anchorHeight * 2.5) {
                continue;
            }

            double score = dy * 2.0 + dx;
            if (score < bestScore) {
                bestScore = score;
                bestItem = item;
            }
        }

        return bestItem == null ? null : extractor.apply(bestItem.getText());
    }

    /**
     * 按“自身 → 右 → 下 → 上 → 全局”顺序查找出生日期
     */

    private String findBirthdayAroundLabel(OcrItem birthLabel, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        if (birthLabel == null || birthLabel.getOcrBox() == null || allItems == null || allItems.isEmpty()) {
            return null;
        }

        String fromSelf = extractBirthday(birthLabel.getText());
        if (fromSelf != null) {
            return fromSelf;
        }

        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OcrBox anchorBox = birthLabel.getOcrBox();

        String fromRight = findBirthdayByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.RIGHT, 4);
        if (fromRight != null) {
            return fromRight;
        }

        String fromDown = findBirthdayByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.DOWN, 4);
        if (fromDown != null) {
            return fromDown;
        }

        String fromUp = findBirthdayByDirection(anchorBox, itemByBox, boxList, BoxUtils.Direction.UP, 3);
        if (fromUp != null) {
            return fromUp;
        }

        for (OcrItem item : allItems) {
            String b = extractBirthday(item.getText());
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    /**
     * 按指定方向在若干最近框中尝试组合/单独解析出生日期
     */
    private String findBirthdayByDirection(OcrBox anchorBox,
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

        if (neighborItems.isEmpty()) {
            return null;
        }

        for (OcrItem item : neighborItems) {
            String b = extractBirthday(item.getText());
            if (b != null) {
                return b;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (OcrItem item : neighborItems) {
            if (item.getText() != null) {
                sb.append(item.getText().replace(" ", ""));
            }
        }
        String merged = sb.toString();
        if (!merged.isEmpty()) {
            String b = extractBirthday(merged);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    /**
     * 从文本中提取身份证号（优先 18 位二代证）
     */
    private String extractIdNumber(String text) {
        if (text == null) {
            return null;
        }
        String candidate = text.toUpperCase();
        Matcher m = Pattern.compile("[0-9X]{15,18}").matcher(candidate);
        while (m.find()) {
            String id = m.group();
            if (id.length() == 18) {
                return id;
            }
        }
        return null;
    }

    /**
     * 从文本中提取生日（标准化为 yyyy-MM-dd）
     */
    private String extractBirthday(String text) {
        if (text == null) {
            return null;
        }
        String clean = text.replace(" ", "");

        Matcher m1 = BIRTHDAY_WITH_SEPARATORS.matcher(clean);
        if (m1.find()) {
            String birthday = normalizeBirthday(m1.group(1), m1.group(2), m1.group(3));
            if (birthday != null) {
                return birthday;
            }
        }

        Matcher m2 = BIRTHDAY_COMPACT.matcher(clean);
        if (m2.find()) {
            String birthday = normalizeBirthday(m2.group(1), m2.group(2), m2.group(3));
            if (birthday != null) {
                return birthday;
            }
        }
        return null;
    }

    private String normalizeBirthday(String raw) {
        String clean = raw.replace("年", "-")
                .replace("月", "-")
                .replace("日", "")
                .replace("/", "-")
                .replace(".", "-");
        String[] parts = clean.split("-");
        if (parts.length != 3) {
            return null;
        }
        String y = parts[0];
        String m = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
        String d = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
        return y + "-" + m + "-" + d;
    }

    private String normalizeBirthday(String year, String month, String day) {
        if (year == null || month == null || day == null) {
            return null;
        }
        String normalizedMonth = month.length() == 1 ? "0" + month : month;
        String normalizedDay = day.length() == 1 ? "0" + day : day;
        try {
            LocalDate date = LocalDate.of(
                    Integer.parseInt(year),
                    Integer.parseInt(normalizedMonth),
                    Integer.parseInt(normalizedDay)
            );
            return date.toString();
        } catch (DateTimeException | NumberFormatException e) {
            return null;
        }
    }

    private String formatBirthdayFromId(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return null;
        }
        String y = yyyymmdd.substring(0, 4);
        String m = yyyymmdd.substring(4, 6);
        String d = yyyymmdd.substring(6, 8);
        return y + "-" + m + "-" + d;
    }

    private Point boxCenter(OcrBox box) {
        float[] pts = box.toFloatArray();
        float cx = (pts[0] + pts[2] + pts[4] + pts[6]) / 4;
        float cy = (pts[1] + pts[3] + pts[5] + pts[7]) / 4;
        return new Point(cx, cy);
    }

    private double boxWidth(OcrBox box) {
        float[] pts = box.toFloatArray();
        float minX = Math.min(Math.min(pts[0], pts[2]), Math.min(pts[4], pts[6]));
        float maxX = Math.max(Math.max(pts[0], pts[2]), Math.max(pts[4], pts[6]));
        return Math.max(1.0, maxX - minX);
    }

    private double boxHeight(OcrBox box) {
        float[] pts = box.toFloatArray();
        float minY = Math.min(Math.min(pts[1], pts[3]), Math.min(pts[5], pts[7]));
        float maxY = Math.max(Math.max(pts[1], pts[3]), Math.max(pts[5], pts[7]));
        return Math.max(1.0, maxY - minY);
    }

    /**
     * 过滤掉面积明显小于整体的检测框，粗略去噪
     */
    private List<OcrItem> filterSmallBoxes(List<OcrItem> items) {
        List<OcrItem> result = IdCardOcrUtils.filterSmallBoxes(items, 0.15);
        log.debug("身份证正面解析：过滤小框完成，原始数量={}，过滤后数量={}", items == null ? 0 : items.size(), result == null ? 0 : result.size());
        return result;
    }

    /**
     * 基于 BoxUtils，从锚点右侧找到最近的一个文本框
     */
    private OcrItem findNearestRightItem(OcrItem anchor, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        if (anchor == null || anchor.getOcrBox() == null) {
            return null;
        }
        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OcrBox nearest = BoxUtils.findNearestBox(anchor.getOcrBox(), boxList, BoxUtils.Direction.RIGHT);
        if (nearest == null) {
            return null;
        }
        return itemByBox.get(nearest);
    }

    /**
     * 查找包含标签关键字的 item
     */
    private OcrItem findLabel(List<OcrItem> allItems, List<String> keywords) {
        for (OcrItem item : allItems) {
            String text = item.getText();
            if (text == null) {
                continue;
            }
            String clean = text.replace(" ", "");
            for (String kw : keywords) {
                if (clean.contains(kw)) {
                    return item;
                }
            }
        }
        return null;
    }

    private OcrItem findAddressLabel(List<OcrItem> allItems) {
        OcrItem direct = findLabel(allItems, Arrays.asList("住址", "地址", "住址:", "住址："));
        if (direct != null) {
            return direct;
        }
        if (allItems == null) {
            return null;
        }
        for (OcrItem item : allItems) {
            String text = item.getText();
            if (text == null) {
                continue;
            }
            String clean = text.replace(" ", "");
            if (looksLikeAddressLabel(clean)) {
                return item;
            }
        }
        return null;
    }

    private boolean looksLikeAddressLabel(String text) {
        if (text == null || text.length() < 2) {
            return false;
        }
        if (text.contains("址") && (text.startsWith("住") || text.startsWith("佳") || text.startsWith("往"))) {
            return true;
        }
        return false;
    }

    /**
     * 拼接地址：
     * 1. 从“住址”右侧开始，整行向右拼接多个检测框
     * 2. 继续向下拼接若干行同一列附近的文本
     * 3. 若某一行疑似“公民身份号码”行，则终止
     */
    private String collectAddress(OcrItem addressLabel, List<OcrItem> allItems, Map<OcrBox, OcrItem> itemByBox) {
        StringBuilder sb = new StringBuilder();
        if (addressLabel == null || addressLabel.getOcrBox() == null || allItems == null || allItems.isEmpty()) {
            return "";
        }

        List<OcrBox> boxList = allItems.stream()
                .map(OcrItem::getOcrBox)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<OcrBox> usedBoxes = new HashSet<>();

        String labelAddressText = extractAddressAfterLabel(addressLabel.getText());
        if (!labelAddressText.isEmpty()) {
            appendMergedText(sb, labelAddressText);
            usedBoxes.add(addressLabel.getOcrBox());
        }

        OcrItem firstLineItem = findNearestRightItem(addressLabel, allItems, itemByBox);
        if (firstLineItem != null && firstLineItem.getOcrBox() != null) {
            String firstLineText = buildAddressLine(firstLineItem.getOcrBox(), itemByBox, boxList, usedBoxes);
            if (!firstLineText.isEmpty() && !isIdNumberLine(firstLineText)) {
                appendMergedText(sb, firstLineText);
            } else if (isIdNumberLine(firstLineText)) {
                return sb.toString();
            }
        }

        OcrBox current = firstLineItem != null && firstLineItem.getOcrBox() != null
                ? firstLineItem.getOcrBox()
                : addressLabel.getOcrBox();

        for (int i = 0; i < 3; i++) {
            List<OcrBox> downs = BoxUtils.findNearestBoxes(
                    current,
                    boxList,
                    BoxUtils.Direction.DOWN,
                    1
            );
            if (downs == null || downs.isEmpty()) {
                break;
            }
            OcrBox down = downs.get(0);
            if (!isLikelyAddressContinuation(current, down, itemByBox)) {
                break;
            }
            String lineText = buildAddressLine(down, itemByBox, boxList, usedBoxes);
            if (lineText.isEmpty()) {
                break;
            }
            if (isIdNumberLine(lineText)) {
                break;
            }
            appendMergedText(sb, lineText);
            current = down;
        }

        return sb.toString();
    }

    private String extractAddressAfterLabel(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String clean = text.replace(" ", "");
        int idx = clean.indexOf("住址");
        if (idx >= 0) {
            return clean.substring(idx + 2);
        }
        idx = clean.indexOf("佳址");
        if (idx >= 0) {
            return clean.substring(idx + 2);
        }
        idx = clean.indexOf("往址");
        if (idx >= 0) {
            return clean.substring(idx + 2);
        }
        idx = clean.indexOf("地址");
        if (idx >= 0) {
            return clean.substring(idx + 2);
        }
        return "";
    }

    private void appendMergedText(StringBuilder sb, String nextText) {
        if (nextText == null || nextText.isEmpty()) {
            return;
        }
        if (sb.length() == 0) {
            sb.append(nextText);
            return;
        }
        String existing = sb.toString();
        int maxOverlap = Math.min(existing.length(), nextText.length());
        for (int overlap = maxOverlap; overlap > 0; overlap--) {
            if (existing.regionMatches(existing.length() - overlap, nextText, 0, overlap)) {
                sb.append(nextText.substring(overlap));
                return;
            }
        }
        sb.append(nextText);
    }

    private boolean isLikelyAddressContinuation(OcrBox current, OcrBox candidate, Map<OcrBox, OcrItem> itemByBox) {
        if (current == null || candidate == null) {
            return false;
        }
        OcrItem candidateItem = itemByBox.get(candidate);
        if (candidateItem == null) {
            return false;
        }
        String text = candidateItem.getText();
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String compact = text.replace(" ", "");
        if (!compact.matches(".*[\\u4e00-\\u9fa5].*")) {
            return false;
        }
        if (compact.matches("[A-Za-z]+")) {
            return false;
        }

        double currentBottom = Math.max(Math.max(current.getBottomLeft().getY(), current.getBottomRight().getY()),
                Math.max(current.getTopLeft().getY(), current.getTopRight().getY()));
        double candidateTop = Math.min(Math.min(candidate.getTopLeft().getY(), candidate.getTopRight().getY()),
                Math.min(candidate.getBottomLeft().getY(), candidate.getBottomRight().getY()));
        double verticalGap = candidateTop - currentBottom;
        double allowedGap = Math.max(boxHeight(current), boxHeight(candidate)) * 1.2;
        if (verticalGap > allowedGap) {
            return false;
        }

        double currentCenterX = boxCenter(current).getX();
        double candidateCenterX = boxCenter(candidate).getX();
        double centerDeltaX = Math.abs(candidateCenterX - currentCenterX);
        double allowedDeltaX = Math.max(boxWidth(current), boxWidth(candidate)) * 0.9;
        return centerDeltaX <= allowedDeltaX;
    }

    /**
     * 根据某一行的起始框，向右拼接同一行上的多个检测框文本
     */
    private String buildAddressLine(OcrBox rowAnchor,
                                    Map<OcrBox, OcrItem> itemByBox,
                                    List<OcrBox> boxList,
                                    Set<OcrBox> usedBoxes) {
        if (rowAnchor == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        OcrItem anchorItem = itemByBox.get(rowAnchor);
        if (anchorItem != null && anchorItem.getText() != null && !usedBoxes.contains(rowAnchor)) {
            sb.append(anchorItem.getText().replace(" ", ""));
            usedBoxes.add(rowAnchor);
        }

        List<OcrBox> rightBoxes = BoxUtils.findNearestBoxes(rowAnchor, boxList, BoxUtils.Direction.RIGHT, 8);
        if (rightBoxes != null && !rightBoxes.isEmpty()) {
            for (OcrBox rb : rightBoxes) {
                if (usedBoxes.contains(rb)) {
                    continue;
                }
                OcrItem item = itemByBox.get(rb);
                if (item != null && item.getText() != null) {
                    sb.append(item.getText().replace(" ", ""));
                    usedBoxes.add(rb);
                }
            }
        }

        return sb.toString();
    }

    /**
     * 判断一行文本是否疑似“公民身份号码”行
     */
    private boolean isIdNumberLine(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String norm = normalizeText(text);
        if (norm.contains("公民身份号码") || norm.contains("公民身份號碼") || norm.toUpperCase().contains("ID")) {
            return true;
        }
        return extractIdNumber(norm) != null;
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
}
