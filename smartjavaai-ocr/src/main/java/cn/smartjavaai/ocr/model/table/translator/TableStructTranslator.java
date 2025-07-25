package cn.smartjavaai.ocr.model.table.translator;

import ai.djl.Model;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.entity.TableStructureResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表格识别的前后处理
 */
public class TableStructTranslator implements Translator<Image, TableStructureResult> {

    private final int maxLength = 488;
    private int height;
    private int width;
    private float scale = 1.0f;
    private float xScale;
    private float yScale;
    private List<String> dict;
    private String beg_str = "sos";
    private String end_str = "eos";
    private List<String> td_token = new ArrayList<>();

    @Override
    public void prepare(TranslatorContext ctx) throws IOException {
        Model model = ctx.getModel();
        try (InputStream is = model.getArtifact("table_structure_dict_ch.txt").openStream()) {
            dict = Utils.readLines(is, false);
            dict.add(0,beg_str);
            if(dict.contains("<td>"))
                dict.remove("<td>");
            if(!dict.contains("<td></td>"))
                dict.add("<td></td>");
            dict.add(end_str);
        }

        td_token.add("<td>");
        td_token.add("<td");
        td_token.add("<td></td>");
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDArray img = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
        height = input.getHeight();
        width = input.getWidth();

        img = ResizeTableImage(img, height, width, maxLength);
        img = PaddingTableImage(ctx, img, maxLength);

        img = img.transpose(2, 0, 1).div(255).flip(0);
        img = NDImageUtils.normalize(
                        img, new float[]{0.485f, 0.456f, 0.406f}, new float[]{0.229f, 0.224f, 0.225f});
        img = img.expandDims(0);
        return new NDList(img);
    }

    @Override
    public TableStructureResult processOutput(TranslatorContext ctx, NDList list) {
        NDArray bbox_preds = list.get(0);
        NDArray structure_probs = list.get(1);

        NDArray structure_idx = structure_probs.argMax(2);
        structure_probs = structure_probs.max(new int[]{2});

        List<List<String>> structure_batch_list = new ArrayList<>();
        List<List<NDArray>> bbox_batch_list = new ArrayList<>();
        List<List<NDArray>> result_score_list = new ArrayList<>();

        // get ignored tokens
        int beg_idx = dict.indexOf(beg_str);
        int end_idx = dict.indexOf(end_str);

        long batch_size = structure_idx.size(0);
        for (int batch_idx = 0; batch_idx < batch_size; batch_idx++) {
            List<String> structure_list = new ArrayList<>();
            List<NDArray> bbox_list = new ArrayList<>();
            List<NDArray> score_list = new ArrayList<>();

            long len = structure_idx.get(batch_idx).size();
            for (int idx = 0; idx < len; idx++) {
                int char_idx = (int) structure_idx.get(batch_idx).get(idx).toLongArray()[0];
                if (idx > 0 && char_idx == end_idx) {
                    break;
                }
//                if (char_idx == beg_idx || char_idx == end_idx) {
//                    continue;
//                }
                String text = dict.get(char_idx);
                if(td_token.indexOf(text)>-1){
                    NDArray bbox = bbox_preds.get(batch_idx, idx);
//                    bbox.set(new NDIndex("0::2"), bbox.get(new NDIndex("0::2")));
//                    bbox.set(new NDIndex("1::2"), bbox.get(new NDIndex("1::2")));
                    bbox_list.add(bbox);
                }
                structure_list.add(text);
                score_list.add(structure_probs.get(batch_idx, idx));
            }

            structure_batch_list.add(structure_list); // structure_str
            bbox_batch_list.add(bbox_list);
            result_score_list.add(score_list);
        }
        List<String> structure_str_list =structure_batch_list.get(0);
        List<NDArray> bbox_list = bbox_batch_list.get(0);
        List<NDArray> score_list = result_score_list.get(0);

        structure_str_list.add(0,"<html>");
        structure_str_list.add(1,"<body>");
        structure_str_list.add(2,"<table>");
        structure_str_list.add("</table>");
        structure_str_list.add("</body>");
        structure_str_list.add("</html>");

        List<OcrItem> ocrItemList = new ArrayList<>();

        for (int i = 0; i < bbox_list.size(); i++) {
            NDArray box = bbox_list.get(i);
            float[] arr = new float[4];
            arr[0] = box.get(new NDIndex("0::2")).min().toFloatArray()[0];
            arr[1] = box.get(new NDIndex("1::2")).min().toFloatArray()[0];
            arr[2] = box.get(new NDIndex("0::2")).max().toFloatArray()[0];
            arr[3] = box.get(new NDIndex("1::2")).max().toFloatArray()[0];

            Point topLeft = new Point(arr[0] * xScale * width, arr[1] * yScale * height);
            Point topRight = new Point(arr[2] * xScale * width, arr[1] * yScale * height);
            Point bottomRight = new Point(arr[2] * xScale * width, arr[3] * yScale * height);
            Point bottomLeft = new Point(arr[0] * xScale * width, arr[3] * yScale * height);


            OcrBox ocrBox = new OcrBox(topLeft, topRight, bottomRight, bottomLeft);
            //String tag = structure_str_list.get(i + 3); // 前面加了<html><body><table> 所以偏移+3
            float score = score_list.get(i).toFloatArray()[0]; // 获取每个结构token的得分
            OcrItem item = new OcrItem();
            item.setOcrBox(ocrBox);
            item.setScore(score);
            //item.setTableTag(tag);
            ocrItemList.add(item);
        }
        return new TableStructureResult(ocrItemList, structure_str_list);
    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }

    private NDArray ResizeTableImage(NDArray img, int height, int width, int maxLen) {
        int localMax = Math.max(height, width);
        float ratio = maxLen * 1.0f / localMax;
        int resize_h = (int) (height * ratio);
        int resize_w = (int) (width * ratio);
        scale = ratio;

        if(width > height){
            xScale = 1f;
            yScale = (float)width /(float)height;
        } else{
            xScale = (float)height /(float)width;
            yScale = 1f;
        }

        img = NDImageUtils.resize(img, resize_w, resize_h);
        return img;
    }

    private NDArray PaddingTableImage(TranslatorContext ctx, NDArray img, int maxLen) {
        NDArray paddingImg = ctx.getNDManager().zeros(new Shape(maxLen, maxLen, 3), DataType.UINT8);
        paddingImg.set(
                new NDIndex("0:" + img.getShape().get(0) + ",0:" + img.getShape().get(1) + ",:"), img);
        return paddingImg;
    }

}
