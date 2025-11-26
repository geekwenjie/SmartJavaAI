package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.TranslateException;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.common.utils.NMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dwj
 */
public class PNetModel {


    /**
     * 生成金字塔缩放比例列表
     * @param image
     * @return
     */
    public static List<Double> generateScales(Image image){
        long h = image.getHeight();
        long w = image.getWidth();
        // 计算最小缩放比例
        double minsize = 20;
        double m = 12.0 / minsize;
        double minl = Math.min(h, w) * m;

        // 创建金字塔缩放比例列表
        double factor = 0.709;  // 你原代码的 factor
        List<Double> scales = new ArrayList<>();
        double scale_i = m;
        while (minl >= 12) {
            scales.add(scale_i);
            scale_i *= factor;
            minl *= factor;
        }
        return scales;
    }


    public static NDArray pNetPre(Image input,NDManager manager){
        // Image -> NDArray (H, W, C)
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        // 增加 batch 维度 (1, H, W, C) ----
        array = array.expandDims(0);
        // 交换维度 (N, C, H, W)
        array = array.transpose(0, 3, 1, 2);
        // 转成模型的数据类型
        if (!array.getDataType().equals(DataType.FLOAT32)) {
            array = array.toType(DataType.FLOAT32, false);
        }
        return array;
    }


    public static NDList firstStage(NDManager manager, Predictor<NDList, NDList> pnetPredictor, Image image) throws TranslateException {
        List<Double> scales = MtcnnProcess.generateScales(image);
        NDArray imgs = pNetPre(image,manager);
        int h = image.getHeight();
        int w = image.getWidth();
        // 第一阶段
        NDList boxes_list = new NDList();
        NDList image_inds_list = new NDList();
        NDList scale_picks_list = new NDList();
        int offset = 0;
        for (double scale : scales) {
            int newH = (int) (h * scale + 1);
            int newW = (int) (w * scale + 1);
            //  (N, H, W, C)
            NDArray transposed = imgs.transpose(0, 2, 3, 1);
            transposed = NDImageUtils.resize(transposed, newW, newH, Image.Interpolation.AREA);
            // (N, C, H, W)
            transposed = transposed.transpose(0, 3, 1, 2);
            // 归一化
            transposed = transposed.sub(127.5).mul(0.0078125f);


            NDList outputPnet = pnetPredictor.predict(new NDList(transposed));
            NDArray reg = outputPnet.get(0);   // [B, 4, H, W]
            NDArray probs = outputPnet.get(1); // [B, 2, H, W]

            List<NDArray> boundingBox = generateBoundingBox(reg, probs.get(":, 1"), (float)scale, 0.6f);

            NDArray boxes_scale = boundingBox.get(0); // [N,9]
            NDArray imgIndND = boundingBox.get(1); // [N]

            NDArray pick = NMSUtils.batchedNms(boxes_scale.get(":,:4"), boxes_scale.get(":,4"), imgIndND, 0.5f,manager);

            boxes_list.add(boxes_scale);
            image_inds_list.add(imgIndND);
            scale_picks_list.add(pick.add(offset));
            offset += boxes_scale.getShape().get(0);
        }
        // 270 9
        NDArray boxes = NDArrays.concat(boxes_list, 0);
        NDArray image_inds = NDArrays.concat(image_inds_list, 0);
        NDArray scale_picks = NDArrays.concat(scale_picks_list, 0);

        // NMS within each scale + image
        boxes = boxes.get(scale_picks);         // scalePicksAll 是 NDArrays.concat 后的 INT64 NDArray
        image_inds = image_inds.get(scale_picks); // 同样索引

        // NMS within each image
        NDArray pick = NMSUtils.batchedNms(
                boxes.get(":, :4"),       // 坐标
                boxes.get(":, 4"),        // score
                image_inds,                // 每个框对应的图片编号
                0.7f,                      // IoU 阈值
                manager
        );
        // 8 9
        boxes = boxes.get(pick);
        image_inds = image_inds.get(pick);


        NDArray regw = boxes.get(":, 2").sub(boxes.get(":, 0"));
        NDArray regh = boxes.get(":, 3").sub(boxes.get(":, 1"));

        NDArray qq1 = boxes.get(":, 0").add(boxes.get(":, 5").mul(regw));
        NDArray qq2 = boxes.get(":, 1").add(boxes.get(":, 6").mul(regh));
        NDArray qq3 = boxes.get(":, 2").add(boxes.get(":, 7").mul(regw));
        NDArray qq4 = boxes.get(":, 3").add(boxes.get(":, 8").mul(regh));

        boxes = NDArrays.stack(new NDList(qq1, qq2, qq3, qq4, boxes.get(":, 4")), 1);
        boxes = MtcnnUtils.rerec(boxes);
        return new NDList(boxes, image_inds, imgs);
    }

    /**
     * 生成候选框，等价于 Python 版 generateBoundingBox
     *
     * @param reg       NDArray [B,4,H,W]，回归偏移量
     * @param probs     NDArray [B,H,W]，人脸概率
     * @param scale     当前金字塔缩放比例
     * @param threshold 阈值
     * @return 一个包含两个元素的 List：
     *         0 -> NDArray bounding boxes [N,9] (x1,y1,x2,y2,score,dx1,dy1,dx2,dy2)
     *         1 -> NDArray image_inds [N]
     */
    public static List<NDArray> generateBoundingBox(
            NDArray reg, NDArray probs, float scale, float threshold) {

        float stride = 2f;
        float cellSize = 12f;

        // mask = probs >= thresh  -> [B,H,W]
        NDArray mask = probs.gte(threshold);

        // mask_inds = mask.nonzero()  -> [N,3]  每行: (batch, y, x)
        NDArray maskInds = mask.nonzero();

        // image_inds = mask_inds[:, 0]
        NDArray imageInds = maskInds.get(":,0");

        // yx = mask_inds[:, 1:]  [N,2] -> (y, x)
        NDArray yx = maskInds.get(":,1:");

        // bb = mask_inds[:, 1:].flip(1)   Python 是 (y,x) -> (x,y)
        NDArray bb = yx.flip(1); // [N,2]  (x, y)

        // 左上角 (x1, y1) 坐标 q1 = ((stride * bb + 1) / scale).floor()
        NDArray q1 = bb.mul(stride).add(1).div(scale).floor();

        // 右下角 (x2, y2) 坐标 q2 = ((stride * bb + cellsize) / scale).floor()
        NDArray q2 = bb.mul(stride).add(cellSize).div(scale).floor();

        Shape probShape = probs.getShape(); // [B,H,W]
        long H = probShape.get(1);
        long W = probShape.get(2);

        NDArray linearIndex = maskInds.get(":,0").mul(H * W)
                .add(maskInds.get(":,1").mul(W))
                .add(maskInds.get(":,2"));

        NDArray scores = probs.reshape(-1).gather(linearIndex, 0);

        NDArray regPerm = reg.transpose(1, 0, 2, 3); // [4,B,H,W]
        NDArray regFlat = regPerm.reshape(4, -1);    // [4, total]

        NDArray linearIndexForGather = linearIndex.expandDims(0).repeat(0, 4); // [4, N]
        NDArray regPicked = regFlat.gather(linearIndexForGather, 1).transpose(); // [N,4]

        NDArray x1 = q1.get(":, 0").expandDims(1);
        NDArray y1 = q1.get(":, 1").expandDims(1);
        NDArray x2 = q2.get(":, 0").expandDims(1);
        NDArray y2 = q2.get(":, 1").expandDims(1);

        NDArray boundingBoxes = NDArrays.concat(
                new NDList(x1, y1, x2, y2, scores.expandDims(1), regPicked), 1
        );

        return Arrays.asList(boundingBoxes, imageInds);
    }


}
