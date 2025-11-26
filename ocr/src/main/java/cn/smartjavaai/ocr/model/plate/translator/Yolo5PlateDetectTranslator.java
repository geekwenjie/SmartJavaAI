package cn.smartjavaai.ocr.model.plate.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Landmark;
import ai.djl.modality.cv.output.Point;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.common.utils.LetterBoxUtils;
import cn.smartjavaai.common.utils.NMSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author dwj
 */
public class Yolo5PlateDetectTranslator implements Translator<Image, DetectedObjects> {

    private int inputSize = 640;
    private float minConfThreshold = 0.3f;
    private float iouThreshold = 0.5f;

    private float confThreshold = 0;

    private int imageWidth;
    private int imageHeight;

    private int topK;

    public Yolo5PlateDetectTranslator(Map<String, ?> arguments) {
        confThreshold =
                arguments.containsKey("confThreshold")
                        ? Integer.parseInt(arguments.get("confThreshold").toString())
                        : 0.3f;

        iouThreshold =
                arguments.containsKey("iouThreshold")
                        ? Integer.parseInt(arguments.get("iouThreshold").toString())
                        : 0.5f;

        topK = arguments.containsKey("topk")
                ? Integer.parseInt(arguments.get("topk").toString())
                : 100;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        imageWidth = (int) array.getShape().get(1);
        imageHeight = (int) array.getShape().get(0);
        //Letter box resize 640x640 with padding (保持比例，补边缘)
        LetterBoxUtils.ResizeResult letterBoxResult = LetterBoxUtils.letterbox(manager, array, inputSize, inputSize, 114f, LetterBoxUtils.PaddingPosition.CENTER);
        ctx.setAttachment("letterBoxResult", letterBoxResult);
        array = letterBoxResult.image;
        // 转为 float32 且归一化到 0~1
        array = array.toType(DataType.FLOAT32, false).div(255f); // HWC
        // HWC -> CHW
        array = array.transpose(2, 0, 1); // CHW
        return new NDList(array.expandDims(0));
    }

    @Override
    public DetectedObjects processOutput(TranslatorContext ctx, NDList list) {
        NDManager manager = ctx.getNDManager();
        LetterBoxUtils.ResizeResult letterBoxResult = (LetterBoxUtils.ResizeResult)ctx.getAttachment("letterBoxResult");
        //[x_center, y_center, w, h, obj_conf, 8个关键点, class1_conf, class2_conf]
        //目标置信度 obj_conf 5:13 关键点 [13:15]分类得分：单层车牌 / 双层车牌
        NDArray dets = list.singletonOrThrow();
        //置信度过滤 (1,25200, 15)
        NDArray dets0 = dets.get(0);
        NDArray conf = dets0.get(":, 4"); // shape [N]
        NDArray mask = conf.gt(minConfThreshold);
        //筛选出符合条件的框(17,15)
        NDArray detsFiltered = dets0.get(mask); // 筛掉低置信度

        //把分类得分 [13:15] * 置信度 [4:5] 做联合概率
        NDArray clsLogits = detsFiltered.get(":, 13:15"); // (N, 2)
        NDArray confFiltered = detsFiltered.get(":, 4").reshape(-1, 1); // (N, 1)
        clsLogits = clsLogits.mul(confFiltered); // (N, 2)，变成 obj_conf * class_conf

        NDArray jointScore = clsLogits.max(new int[]{1}); // shape (N,)
        // 联合过滤
        NDArray jointMask = jointScore.gt(confThreshold);
        detsFiltered = detsFiltered.get(jointMask);

        if (detsFiltered.isEmpty()) {
            return new DetectedObjects(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        clsLogits = clsLogits.get(jointMask);


        //中心点框 [x,y,w,h] ➔ 左上右下 [x1,y1,x2,y2]
        NDArray xywh = detsFiltered.get(":, 0:4"); // (N, 4)
        NDArray halfWH = xywh.get(":, 2:4").div(2); // (N, 2)
        NDArray xy1 = xywh.get(":, 0:2").sub(halfWH); // (N, 2)
        NDArray xy2 = xywh.get(":, 0:2").add(halfWH); // (N, 2)
        NDArray boxes = NDArrays.concat(new NDList(xy1, xy2), 1); // (N, 4)

        // 分类得分最大值：score (N, 1)，对应类别 index (N, 1)
        NDArray scores = clsLogits.max(new int[]{1}, true); // (N, 1)
        NDArray indices = clsLogits.argMax(1).reshape(-1, 1).toType(DataType.FLOAT32, false); // (N, 1)

        // 关键点坐标 [5:13]
        NDArray keyPoints = detsFiltered.get(":, 5:13"); // (N, 8)

        // 拼成最终结果：(x1, y1, x2, y2, score, 8关键点, index)
        NDArray output = NDArrays.concat(new NDList(boxes, scores, keyPoints, indices), 1); // (N, 14)

        // NMS 过滤掉重叠框
        int[] keepIndices = NMSUtils.nms(boxes, scores.squeeze(), iouThreshold); // scores.squeeze() ➝ (N,)】
        if (keepIndices.length == 0) {
            return new DetectedObjects(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }
        NDArray kept = output.get(manager.create(keepIndices));
        // 如果超过 topK，则截断
        if (keepIndices.length > topK) {
            int[] topkIndices = new int[topK];
            System.arraycopy(keepIndices, 0, topkIndices, 0, topK);
            keepIndices = topkIndices;
        }
        //恢复原图坐标（除回比例，减掉 padding）
        NDArray restored = LetterBoxUtils.restoreBox(kept, letterBoxResult.r, letterBoxResult.left, letterBoxResult.top, 5,8);

        List<String> classNames = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        float[] flatData = restored.toFloatArray();
        long[] shape = restored.getShape().getShape(); // 比如 (N, 14)
        int rows = (int) shape[0];
        int cols = (int) shape[1];

        // 把一维数组重组为二维数组
        float[][] data = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(flatData, i * cols, data[i], 0, cols);
        }

        for (float[] row : data) {
            // row结构：(x1, y1, x2, y2, score, kp1,..., kp8, classIndex)
            float x1 = row[0];
            float y1 = row[1];
            float x2 = row[2];
            float y2 = row[3];
            float score = row[4];
            int classIndex = (int) row[13];

            double prob = score;
            String className = classIndex == 0 ? "single" : "double";

            // 转相对坐标，DJL的Rectangle用比例坐标（0~1）
            double rectX = x1 / imageWidth;
            double rectY = y1 / imageHeight;
            double rectW = (x2 - x1) / imageWidth;
            double rectH = (y2 - y1) / imageHeight;

            // 构建 Polygon 四个角点
            List<Point> pointsSrc = new ArrayList<>();
            pointsSrc.add(new Point(row[5], row[6]));
            pointsSrc.add(new Point(row[7], row[8]));
            pointsSrc.add(new Point(row[9], row[10]));
            pointsSrc.add(new Point(row[11], row[12]));

            Landmark box = new Landmark(rectX, rectY, rectW, rectH, pointsSrc);
            classNames.add(className);
            probabilities.add(prob);
            boundingBoxes.add(box);
        }
        DetectedObjects detectedObjects = new DetectedObjects(classNames, probabilities, boundingBoxes);
        return detectedObjects;

    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }




}
