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
import cn.smartjavaai.common.utils.NMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dwj
 */
public class ONetModel {


    public static MtcnnBatchResult thirdStage(NDManager manager, Predictor<NDList, NDList> onetPredictor, NDArray imgs, NDArray boxes, int w, int h, NDArray scoresFiltered,NDArray image_indsFiltered) throws TranslateException {
        // Third stage
        NDArray points = manager.zeros(new Shape(0, 5, 2));
        NDList pad = MtcnnUtils.pad(boxes, (int)w, (int)h);
        NDArray y = pad.get(0);
        NDArray ey = pad.get(1);
        NDArray x = pad.get(2);
        NDArray ex = pad.get(3);
        List<NDArray> crops = new ArrayList<>();
        long numFaces = y.size(0);
        for (long k = 0; k < numFaces; k++) {
            // 检查坐标合法性
            if (ey.getInt(k) > (y.getInt(k) - 1) &&
                    ex.getInt(k) > (x.getInt(k) - 1)) {

                // 裁剪 (imageInd, :, y1:ey, x1:ex)
                NDArray imgK = imgs.get(
                        image_indsFiltered.getLong(k) + ", :" +
                                ", " + (y.getInt(k) - 1) + ":" + ey.getInt(k) +
                                ", " + (x.getInt(k) - 1) + ":" + ex.getInt(k)
                ).expandDims(0); // 加 batch 维

                // 缩放到 (24, 24)

                //  (N, H, W, C)
                NDArray transposed = imgK.transpose(0, 2, 3, 1);
                transposed = NDImageUtils.resize(transposed, 48, 48, Image.Interpolation.AREA);
                // (N, C, H, W)
                transposed = transposed.transpose(0, 3, 1, 2);
                crops.add(transposed);
            }
        }

        // 合并成一个 batch
        NDArray im_data = NDArrays.concat(new NDList(crops), 0);
        // 归一化
        im_data = im_data.sub(127.5).mul(0.0078125);
        // 74 48 48
        NDList out = onetPredictor.predict(new NDList(im_data));

        NDArray out0 = out.get(0).transpose(1, 0); // permute(1,0)
        NDArray out1 = out.get(1).transpose(1, 0);
        NDArray out2 = out.get(2).transpose(1, 0);
        NDArray score = out2.get(1); // out1[1, :]
        points = out1.duplicate();
        NDArray ipass = score.gt(0.7); // score > threshold[1]
        // ipass 为布尔/0-1张量，长度应等于 points 的第 1 维（这里是 7）
        NDArray ipassBool = ipass.toType(DataType.BOOLEAN, false);
        long[] colIdx = ipassBool.nonzero().toLongArray();  // 取 True 的列索引
        // 把第 1 维换到第 0 维：(10, 7) -> (7, 10)
        NDArray moved = points.swapAxes(0, 1);
        // 现在按第一维取行即可，相当于选中列
        NDArray selected = moved.get(points.getManager().create(colIdx));  // (sel, 10)
        // 换回原来的轴顺序：(sel, 10) -> (10, sel)
        points = selected.swapAxes(0, 1);
        // 筛选 boxes 和 scores
        // 先获取布尔索引为 true 的行索引
        long[] validIndices = ipass.nonzero().toLongArray();
        // 筛选 boxes 对应行
        NDArray boxesSelected = boxes.get(manager.create(validIndices)); // 行筛选
        // 取前 4 列
        boxesSelected = boxesSelected.get(":, 0:4"); // 只保留前 4 列
        scoresFiltered = score.get(ipass).reshape(-1, 1); // score[ipass].unsqueeze(1)
        boxes = NDArrays.concat(new NDList(boxesSelected, scoresFiltered), 1); // 拼接成 (N,5)

        // 筛选 image_inds
        image_indsFiltered = image_indsFiltered.get(ipass);
        NDArray mv = out0.transpose() // (N, 4)
                .get(ipass); // 1-D 花式索引在第 0 维，得到 (k, 4)


        // w_i = boxes[:, 2] - boxes[:, 0] + 1
        NDArray w_i = boxes.get(":,2").sub(boxes.get(":,0")).add(1);

        // h_i = boxes[:, 3] - boxes[:, 1] + 1
        NDArray h_i = boxes.get(":,3").sub(boxes.get(":,1")).add(1);

        // points_x = w_i.repeat(5, 1) * points[:5, :] + boxes[:, 0].repeat(5, 1) - 1
        NDArray w_repeat = w_i.expandDims(0).repeat(0, 5); // shape: [5, N]
        NDArray p_x = points.get("0:5,:").mul(w_repeat)
                .add(boxes.get(":,0").expandDims(0).repeat(0, 5))
                .sub(1);

        // points_y = h_i.repeat(5, 1) * points[5:10, :] + boxes[:, 1].repeat(5, 1) - 1
        NDArray h_repeat = h_i.expandDims(0).repeat(0, 5); // shape: [5, N]
        NDArray p_y = points.get("5:10,:").mul(h_repeat)
                .add(boxes.get(":,1").expandDims(0).repeat(0, 5))
                .sub(1);

        // points = torch.stack((points_x, points_y)).permute(2, 1, 0)
        NDArray pointsStacked = NDArrays.stack(new NDList(p_x, p_y)); // shape: [2, 5, N]
        points = pointsStacked.transpose(2, 1, 0); // permute(2, 1, 0) => shape [N, 5, 2]

        // boxes = bbreg(boxes, mv)
        boxes = MtcnnUtils.bbreg(boxes, mv);

        NDArray pick = NMSUtils.batchedNms(boxes.get(":, :4"), boxes.get(":, 4"), image_indsFiltered, 0.7f, manager);
        boxes = boxes.get(pick);
        image_indsFiltered = image_indsFiltered.get(pick);
        points = points.get(pick);

        List<NDArray> batchBoxes = new ArrayList<>();
        List<NDArray> batchPoints = new ArrayList<>();

        for (int b_i = 0; b_i < 1; b_i++) {
            // mask: image_inds == b_i
            NDArray mask = image_indsFiltered.eq(b_i);

            // 只保留当前 batch 的 boxes 和 points
            NDArray batchBox = boxes.get(mask);
            NDArray batchPoint = points.get(mask);

            batchBoxes.add(batchBox);
            batchPoints.add(batchPoint);
        }
        return processBatchBoxes(batchBoxes, batchPoints,true, manager);
    }

    public static MtcnnBatchResult processBatchBoxes(
            List<NDArray> batchBoxes,
            List<NDArray> batchPoints,
            boolean selectLargest,
            NDManager manager) {

        List<NDArray> boxesOut = new ArrayList<>();
        List<NDArray> probsOut = new ArrayList<>();
        List<NDArray> pointsOut = new ArrayList<>();

        for (int i = 0; i < batchBoxes.size(); i++) {
            NDArray box = batchBoxes.get(i);   // shape [num_boxes, ?] 或空 NDArray
            NDArray point = batchPoints.get(i); // shape [num_boxes, 5, 2] 或空 NDArray

            if (box == null || box.isEmpty()) {
                boxesOut.add(null);
                probsOut.add(null);
                pointsOut.add(null);
                continue;
            }

            NDArray boxesSelected;
            NDArray probsSelected;
            NDArray pointsSelected;

            if (selectLargest) {
                // 计算面积 (x2 - x1) * (y2 - y1)
                NDArray w = box.get(":,2").sub(box.get(":,0"));
                NDArray h = box.get(":,3").sub(box.get(":,1"));
                NDArray areas = w.mul(h);

                // 按面积降序排序
                NDArray order = areas.argSort().flip(0);
                boxesSelected = box.get(order);
                pointsSelected = point.get(order);
            } else {
                boxesSelected = box;
                pointsSelected = point;
            }

            // boxes[:, :4]
            boxesSelected = boxesSelected.get(":,0:4");

            // probs = box[:, 4]
            probsSelected = box.get(":,4");

            boxesOut.add(boxesSelected);
            probsOut.add(probsSelected);
            pointsOut.add(pointsSelected);
        }

        MtcnnBatchResult result = new MtcnnBatchResult(boxesOut, probsOut, pointsOut);
        result.boxes = boxesOut;
        result.probs = probsOut;
        result.points = pointsOut;
        return result;
    }

}
