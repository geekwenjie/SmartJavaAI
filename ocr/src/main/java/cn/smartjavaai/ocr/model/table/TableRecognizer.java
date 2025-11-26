package cn.smartjavaai.ocr.model.table;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.FileUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.OcrRecOptions;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.entity.TableStructureResult;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import cn.smartjavaai.ocr.utils.ConvertHtml2Excel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表格内容识别器
 * @author dwj
 */
@Slf4j
public class TableRecognizer {

    private OcrCommonDetModel textDetector;
    private TableStructureModel tableStructureModel;
    private OcrCommonRecModel textRecModel;
    private OcrDirectionModel directionModel;

    private TableRecognizer(Builder builder) {
        this.tableStructureModel = builder.tableStructureModel;
        this.textRecModel = builder.textRecModel;
        this.directionModel = builder.directionModel;
        this.textDetector = builder.textDetector;
        textRecModel.setTextDetModel(textDetector);
        textRecModel.setDirectionModel(directionModel);
    }

    public static Builder builder() {
        return new Builder();
    }

    // 链式设置文本识别模型
    public TableRecognizer withTextRecModel(OcrCommonRecModel textRecModel) {
        this.textRecModel = textRecModel;
        return this;
    }

    // 链式设置表格结构模型
    public TableRecognizer withStructureModel(TableStructureModel tableStructureModel) {
        this.tableStructureModel = tableStructureModel;
        return this;
    }

    /**
     * 表格识别
     * @param image
     * @return
     */
    public R<TableStructureResult> recognize(Image image) {
        //表格结构识别
        R<TableStructureResult> result = tableStructureModel.detect(image);
        if(!result.isSuccess()){
            return R.fail(result.getCode(), result.getMessage());
        }
        //文本检测+文字识别
        boolean enableDirectionCorrect = directionModel == null ? false : true;
        OcrRecOptions options = new OcrRecOptions(enableDirectionCorrect, false);
        OcrInfo ocrInfo = textRecModel.recognize(image, options);
        List<String> tableContentList = buildTable(result.getData(), ocrInfo);
        String html = convertHtml(result.getData().getTableTagList(), tableContentList);
        result.getData().setHtml(html);
        return result;
    }


    /**
     * 表格识别
     * @param image
     * @return
     */
    public R<TableStructureResult> recognize(BufferedImage image) {
        if(!BufferedImageUtils.isImageValid(image)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromBufferedImage(image);
            return recognize(img);
        } catch (Exception e) {
            throw new OcrException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }

    /**
     * 表格识别
     * @param imagePath
     * @return
     */
    public R<TableStructureResult> recognize(String imagePath) {
        if(!FileUtils.isFileExists(imagePath)){
            return R.fail(R.Status.FILE_NOT_FOUND);
        }
        Image img = null;
        try {
            img = SmartImageFactory.getInstance().fromFile(Paths.get(imagePath));
            return recognize(img);
        } catch (IOException e) {
            throw new OcrException("无效的图片", e);
        } finally {
            ImageUtils.releaseOpenCVMat(img);
        }
    }


    /**
     * 表格识别
     * @param imageData
     * @return
     */
    public R<TableStructureResult> recognize(byte[] imageData) {
        if(Objects.isNull(imageData)){
            return R.fail(R.Status.INVALID_IMAGE);
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            return recognize(image);
        } catch (IOException e) {
            throw new OcrException("错误的图像", e);
        }
    }

    /**
     * 绘制表格
     * @param tableStructureResult
     * @param image
     * @param savePath
     */
    public void drawTable(TableStructureResult tableStructureResult, BufferedImage image, String savePath){
        if(Objects.isNull(tableStructureResult) || CollectionUtils.isEmpty(tableStructureResult.getTableTagList())){
            throw new OcrException("表格结构为空");
        }
        for (int i = 0; i < tableStructureResult.getOcrItemList().size(); i++){
            OcrItem item = tableStructureResult.getOcrItemList().get(i);
            DetectionRectangle detectionRectangle = item.getOcrBox().toDetectionRectangle();
            BufferedImageUtils.drawRectAndText(image, detectionRectangle, i + "", Color.RED);
        }
        try {
            BufferedImageUtils.saveImage(image, savePath);
        } catch (IOException e) {
            throw new OcrException(e);
        }
    }


    /**
     * 绘制表格
     * @param tableStructureResult
     * @param image
     * @return
     */
    public BufferedImage drawTable(TableStructureResult tableStructureResult, BufferedImage image){
        if(Objects.isNull(tableStructureResult) || CollectionUtils.isEmpty(tableStructureResult.getTableTagList())){
            throw new OcrException("表格结构为空");
        }
        for (int i = 0; i < tableStructureResult.getOcrItemList().size(); i++){
            OcrItem item = tableStructureResult.getOcrItemList().get(i);
            DetectionRectangle detectionRectangle = item.getOcrBox().toDetectionRectangle();
            BufferedImageUtils.drawRectAndText(image, detectionRectangle, i + "", Color.RED);
        }
        return image;
    }

    /**
     * 绘制表格
     * @param tableStructureResult
     * @param image
     * @return
     */
    public Image drawTable(TableStructureResult tableStructureResult, Image image){
        if(Objects.isNull(tableStructureResult) || CollectionUtils.isEmpty(tableStructureResult.getTableTagList())){
            throw new OcrException("表格结构为空");
        }
        for (int i = 0; i < tableStructureResult.getOcrItemList().size(); i++){
            OcrItem item = tableStructureResult.getOcrItemList().get(i);
            DetectionRectangle detectionRectangle = item.getOcrBox().toDetectionRectangle();
            ImageUtils.drawRectAndText(image, detectionRectangle, i + "");
        }
        return image;
    }

    /**
     * 删除 HTML 中第一个 <style> ... </style> 段落
     * @param html 原始 HTML
     * @return 去掉 <style> 的 HTML
     */
    public static String removeStyleBlock(String html) {
        String lowerHtml = html.toLowerCase();
        int styleStart = lowerHtml.indexOf("<style");
        if (styleStart == -1) {
            return html; // 没有 style，返回原文
        }
        int styleEnd = lowerHtml.indexOf("</style>", styleStart);
        if (styleEnd == -1) {
            return html; // 没闭合标签，不处理
        }
        styleEnd += "</style>".length();
        // 去掉 style 块
        return html.substring(0, styleStart) + html.substring(styleEnd);
    }


    /**
     * 导出 Excel
     * @param html
     * @param out
     */
    public void exportExcel(String html, OutputStream out){
        String content = removeStyleBlock(html);
        content = content.replace("<html><body>", "");
        content = content.replace("</body></html>", "");
        try (HSSFWorkbook workbook = ConvertHtml2Excel.table2Excel(content)){
            workbook.write(out);
            out.flush();
        } catch (Exception e) {
            throw new OcrException("导出excel失败，请检查表结构是否识别正确");
        }
    }

    /**
     * 导出 Excel
     * @param html
     * @param savePath
     */
    public void exportExcel(String html, String savePath){
        String content = removeStyleBlock(html);
        content = content.replace("<html><body>", "");
        content = content.replace("</body></html>", "");
        try (HSSFWorkbook workbook = ConvertHtml2Excel.table2Excel(content)){
            workbook.write(new File(savePath));
        } catch (Exception e) {
            throw new OcrException("导出excel失败，请检查表结构是否识别正确");
        }
    }


    /**
     * 构建表格
     * @param tableStructureResult
     * @param ocrInfo
     * @return
     */
    public List<String> buildTable(TableStructureResult tableStructureResult, OcrInfo ocrInfo) {
        // 获取 Cell 与 文本检测框 的对应关系(1:N)。
        Map<Integer, List<Integer>> matched = new ConcurrentHashMap<>();
        List<OcrItem> ocrItems = ocrInfo.getOcrItemList();

        for (int i = 0; i < ocrItems.size(); i++) {
            OcrBox ocrBox = ocrItems.get(i).getOcrBox();
            int[] box_1 = {
                    (int)ocrBox.getTopLeft().getX(),
                    (int)ocrBox.getTopLeft().getY(),
                    (int)ocrBox.getBottomRight().getX(),
                    (int)ocrBox.getBottomRight().getY()
            };
            // 获取两两cell之间的L1距离和 1- IOU
            List<Pair<Float, Float>> distances = new ArrayList<>();
            for (OcrItem cell : tableStructureResult.getOcrItemList()) {
                OcrBox cellBox = cell.getOcrBox();
                int[] box_2 = {
                        (int)cellBox.getTopLeft().getX(),
                        (int)cellBox.getTopLeft().getY(),
                        (int)cellBox.getBottomRight().getX(),
                        (int)cellBox.getBottomRight().getY()
                };
                float distance = distance(box_1, box_2);
                float iou = 1 - computeIou(box_1, box_2);
                distances.add(Pair.of(distance, iou));
            }
            // 根据距离和IOU挑选最"近"的cell
            Pair<Float, Float> nearest = sorted(distances);

            // 获取最小距离对应的下标id，也等价于cell的下标id  （distances列表是根据遍历cells生成的）
            int id = 0;
            for (int idx = 0; idx < distances.size(); idx++) {
                Pair<Float, Float> current = distances.get(idx);
                if (current.getLeft().floatValue() == nearest.getLeft().floatValue()
                        && current.getRight().floatValue() == nearest.getRight().floatValue()) {
                    id = idx;
                    break;
                }
            }
            if (!matched.containsKey(id)) {
                List<Integer> textIds = new ArrayList<>();
                textIds.add(i);
                // cell id, text id list (dt_boxes index list)
                matched.put(id, textIds);
            } else {
                matched.get(id).add(i);
            }
        }

        List<String> cell_contents = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        for (int i = 0; i < tableStructureResult.getOcrItemList().size(); i++) {
            List<Integer> textIds = matched.get(i);
            List<String> contents = new ArrayList<>();
            String content = "";
            if (textIds != null) {
                for (Integer id : textIds) {
                    contents.add(ocrItems.get(id).getText());
                }
                content = StringUtils.join(contents, " ");
            }
            cell_contents.add(content);
            probs.add(-1.0);
        }
        return cell_contents;
    }

    /**
     * 计算欧式距离
     * Calculate L1 distance
     *
     * @param box_1
     * @param box_2
     * @return
     */
    private int distance(int[] box_1, int[] box_2) {
        int x1 = box_1[0];
        int y1 = box_1[1];
        int x2 = box_1[2];
        int y2 = box_1[3];
        int x3 = box_2[0];
        int y3 = box_2[1];
        int x4 = box_2[2];
        int y4 = box_2[3];
        int dis = Math.abs(x3 - x1) + Math.abs(y3 - y1) + Math.abs(x4 - x2) + Math.abs(y4 - y2);
        int dis_2 = Math.abs(x3 - x1) + Math.abs(y3 - y1);
        int dis_3 = Math.abs(x4 - x2) + Math.abs(y4 - y2);
        return dis + Math.min(dis_2, dis_3);
    }

    /**
     * 计算交并比
     * computing IoU
     *
     * @param rec1: (y0, x0, y1, x1), which reflects (top, left, bottom, right)
     * @param rec2: (y0, x0, y1, x1)
     * @return scala value of IoU
     */
    private float computeIou(int[] rec1, int[] rec2) {
        // computing area of each rectangles
        int S_rec1 = (rec1[2] - rec1[0]) * (rec1[3] - rec1[1]);
        int S_rec2 = (rec2[2] - rec2[0]) * (rec2[3] - rec2[1]);

        // computing the sum_area
        int sum_area = S_rec1 + S_rec2;

        // find the each edge of intersect rectangle
        int left_line = Math.max(rec1[1], rec2[1]);
        int right_line = Math.min(rec1[3], rec2[3]);
        int top_line = Math.max(rec1[0], rec2[0]);
        int bottom_line = Math.min(rec1[2], rec2[2]);

        // judge if there is an intersect
        if (left_line >= right_line || top_line >= bottom_line) {
            return 0.0f;
        } else {
            float intersect = (right_line - left_line) * (bottom_line - top_line);
            return (intersect / (sum_area - intersect)) * 1.0f;
        }
    }

    /**
     * 距离排序
     * Distance sorted
     *
     * @param distances
     * @return
     */
    private Pair<Float, Float> sorted(List<Pair<Float, Float>> distances) {
        Comparator<Pair<Float, Float>> comparator =
                new Comparator<Pair<Float, Float>>() {
                    @Override
                    public int compare(Pair<Float, Float> a1, Pair<Float, Float> a2) {
                        // 首先根据IoU排序
                        if (a1.getRight().floatValue() > a2.getRight().floatValue()) {
                            return 1;
                        } else if (a1.getRight().floatValue() == a2.getRight().floatValue()) {
                            // 然后根据L1距离排序
                            if (a1.getLeft().floatValue() > a2.getLeft().floatValue()) {
                                return 1;
                            }
                            return -1;
                        }
                        return -1;
                    }
                };

        // 距离排序
        List<Pair<Float, Float>> newDistances = new ArrayList<>();
        CollectionUtils.addAll(newDistances, new Object[distances.size()]);
        Collections.copy(newDistances, distances);
        Collections.sort(newDistances, comparator);
        return newDistances.get(0);
    }

    /**
     * 生成表格html
     * Generate table html
     *
     * @param pred_structures
     * @param cell_contents
     * @return
     */
    public String convertHtml(List<String> pred_structures, List<String> cell_contents) {
        StringBuffer html = new StringBuffer();
        // 添加统一的样式（可选放到<head>中）
        html.append("<style>\n");
        html.append("table { border-collapse: collapse; }\n");
        html.append("td, th, table { border: 1px solid black; padding: 5px; }\n");
        html.append("</style>\n");
        int td_index = 0;
        for (String tag : pred_structures) {
            if (tag.contains("<td></td>")) {
                String content = cell_contents.get(td_index);
                html.append("<td>");
                html.append(content);
                html.append("</td>");
                td_index++;
                continue;
            }
            html.append(tag);
        }
        return html.toString();
    }


    public static class Builder {
        private TableStructureModel tableStructureModel;
        private OcrCommonRecModel textRecModel;
        private OcrDirectionModel directionModel;
        private OcrCommonDetModel textDetector;

        public Builder withStructureModel(TableStructureModel model) {
            this.tableStructureModel = model;
            return this;
        }

        public Builder withTextRecModel(OcrCommonRecModel model) {
            this.textRecModel = model;
            return this;
        }

        public Builder withDirectionModel(OcrDirectionModel model) {
            this.directionModel = model;
            return this;
        }

        public Builder withTextDetModel(OcrCommonDetModel model) {
            this.textDetector = model;
            return this;
        }

        public TableRecognizer build() {
            if (this.tableStructureModel == null) {
                throw new IllegalStateException("tableStructureModel 未设置");
            }
            if (this.textDetector == null) {
                throw new IllegalStateException("textDetector 未设置");
            }
            if (this.textRecModel == null) {
                throw new IllegalStateException("textRecModel 未设置");
            }
            return new TableRecognizer(this);
        }
    }

}
