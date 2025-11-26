package cn.smartjavaai.ocr.model.plate.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.*;
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
import java.util.List;
import java.util.Map;

/**
 * @author dwj
 */
public class Yolov8PlateDetectTranslator implements Translator<Image, DetectedObjects> {

    private int inputSize = 640;
    private float minConfThreshold = 0.3f;
    private float iouThreshold = 0.5f;

    private float confThreshold = 0;

    private int imageWidth;
    private int imageHeight;

    private int topK;

    private LetterBoxUtils.ResizeResult letterBoxResult;

    public Yolov8PlateDetectTranslator(Map<String, ?> arguments) {
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
        letterBoxResult = LetterBoxUtils.letterbox(manager, array, inputSize, inputSize, 114f, LetterBoxUtils.PaddingPosition.CENTER);
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

        NDArray preds = list.get(0); // shape: (1, 6, 8400)
        preds = preds.squeeze(0).transpose(1, 0); // shape: (8400, 6)

        // preds shape: (8400, 6)
        NDArray classScores = preds.get(":, 4:6"); // shape: (8400, 2)

        // 获取每行最大值（对应 Python 的 .amax(1)）
        NDArray maxScores = classScores.max(new int[]{1}); // shape: (8400,)

        // 构造 mask：score > conf
        NDArray confMask = maxScores.gt(minConfThreshold); // shape: (8400,)

        // 应用 mask 筛选
        preds = preds.get(confMask); // shape: (N_filtered, 6)

        if (preds.isEmpty()) {
            return null;
        }

        // 提取 box (xywh)，转换为 xyxy
        NDArray boxes = preds.get(":, 0:4"); // shape: (N, 4)
        boxes = xywh2xyxy(boxes); // 自定义函数：center xywh -> xyxy

        // 1. 得分和类别索引
        NDArray scoresAndClasses = preds.get(":, 4:6");  // shape (num, 2)
        NDArray scores = scoresAndClasses.max(new int[]{1}, true);  // keepDim = true
        NDArray index = scoresAndClasses.argMax(1).expandDims(1);  // 最大值索引，类别，shape (num, 1)

        // 4. 拼接
        NDArray result = NDArrays.concat(new NDList(boxes, scores, index), 1);  // 在列方向拼接

        // NMS 过滤掉重叠框
        int[] keepIndices = NMSUtils.nms(boxes, scores.squeeze(), iouThreshold); // scores.squeeze() ➝ (N,)
        NDArray kept = result.get(manager.create(keepIndices));
        // 如果超过 topK，则截断
        if (keepIndices.length > topK) {
            int[] topkIndices = new int[topK];
            System.arraycopy(keepIndices, 0, topkIndices, 0, topK);
            keepIndices = topkIndices;
        }
        //恢复原图坐标（除回比例，减掉 padding）
        NDArray restored = LetterBoxUtils.restoreBox(kept, letterBoxResult.r, letterBoxResult.left, letterBoxResult.top, 5,0);

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
            // row结构：(x1, y1, x2, y2, score, classIndex)
            float x1 = row[0];
            float y1 = row[1];
            float x2 = row[2];
            float y2 = row[3];
            float score = row[4];
            int classIndex = (int) row[5];

            double prob = score;
            String className = classIndex == 0 ? "single" : "double";

            // 转相对坐标，DJL的Rectangle用比例坐标（0~1）
            double rectX = x1 / imageWidth;
            double rectY = y1 / imageHeight;
            double rectW = (x2 - x1) / imageWidth;
            double rectH = (y2 - y1) / imageHeight;

            // 构建 Polygon 四个角点
//            List<Point> pointsSrc = new ArrayList<>();
//            pointsSrc.add(new Point(row[5], row[6]));
//            pointsSrc.add(new Point(row[7], row[8]));
//            pointsSrc.add(new Point(row[9], row[10]));
//            pointsSrc.add(new Point(row[11], row[12]));

            Rectangle rectangle = new Rectangle(rectX, rectY, rectW, rectH);
            classNames.add(className);
            probabilities.add(prob);
            boundingBoxes.add(rectangle);
        }
        DetectedObjects detectedObjects = new DetectedObjects(classNames, probabilities, boundingBoxes);
        return detectedObjects;

    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }



    public static NDArray xywh2xyxy(NDArray xywh) {
        NDArray x = xywh.get(":, 0");
        NDArray y = xywh.get(":, 1");
        NDArray w = xywh.get(":, 2").div(2);
        NDArray h = xywh.get(":, 3").div(2);
        NDArray x1 = x.sub(w);
        NDArray y1 = y.sub(h);
        NDArray x2 = x.add(w);
        NDArray y2 = y.add(h);
        return NDArrays.stack(new NDList(x1, y1, x2, y2), 1);
    }

}
