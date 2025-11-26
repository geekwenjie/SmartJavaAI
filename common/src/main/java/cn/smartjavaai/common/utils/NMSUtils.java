package cn.smartjavaai.common.utils;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dwj
 * @date 2025/7/23
 */
public class NMSUtils {

    /**
     * 通用 NMS 方法，输入 NDArray 形式的 boxes 和 scores，返回保留的索引列表
     *
     * @param boxes   NDArray 形状为 (N, 4)，格式为 [x1, y1, x2, y2]
     * @param scores  NDArray 形状为 (N,) 或 (N,1)，每个 box 的置信度
     * @param iouThreshold IOU 阈值，超过该阈值则认为有重叠
     * @return 保留框的索引列表
     */
    public static int[] nms(NDArray boxes, NDArray scores, float iouThreshold) {
        if (boxes.isEmpty()) {
            return new int[0];
        }

        NDArray x1 = boxes.get(":, 0");
        NDArray y1 = boxes.get(":, 1");
        NDArray x2 = boxes.get(":, 2");
        NDArray y2 = boxes.get(":, 3");

        NDArray areas = x2.sub(x1).add(1).mul(y2.sub(y1).add(1));

        // 按照置信度降序排序
        NDArray order = scores.argSort().flip(0);

        List<Integer> keep = new ArrayList<>();

        while (order.size() > 0) {
            int idx = (int)order.getLong(0);
            keep.add(idx);

            if (order.size() == 1) break;

            NDArray currentBox = boxes.get(idx);
            NDArray others = boxes.get(order);

            NDArray xx1 = x1.get(order).maximum(x1.get(idx));
            NDArray yy1 = y1.get(order).maximum(y1.get(idx));
            NDArray xx2 = x2.get(order).minimum(x2.get(idx));
            NDArray yy2 = y2.get(order).minimum(y2.get(idx));

            NDArray w = xx2.sub(xx1).add(1).maximum(0);
            NDArray h = yy2.sub(yy1).add(1).maximum(0);
            NDArray inter = w.mul(h);

            NDArray remAreas = areas.get(order);
            NDArray union = remAreas.add(areas.get(idx)).sub(inter);
            NDArray iou = inter.div(union);

            NDArray mask = iou.lte(iouThreshold);
            order = order.get(mask);
        }
        return keep.stream().mapToInt(i -> i).toArray();
    }



    /**
     * 批量执行 NMS，输入 NDArray 形式的 boxes、scores 和 idxs，返回保留的索引列表
     *
     * @param boxes   NDArray 形状为 (N, 4)，格式为 [x1, y1, x2, y2]
     * @param scores  NDArray 形状为 (N,) 或 (N,1)，每个 box 的置信度
     * @param idxs    NDArray 形状为 (N,)，每个 box 对应的 batch id
     * @param iouThreshold IOU 阈值，超过该阈值则认为有
     * @return 批量保留框的索引列表
     *
     */
    public static NDArray batchedNms(NDArray boxes, NDArray scores, NDArray idxs, float iouThreshold, NDManager manager) {

//        System.out.println("---------------boxes:" + Arrays.toString(boxes.toFloatArray()));

        List<NDArray> keepList = new ArrayList<>();

        // 获取唯一 batch id
        NDArray uniqueIdxs = idxs.unique().get(0);

        for (long batchId : uniqueIdxs.toLongArray()) {
            // 找出当前 batch 的框
            NDArray mask = idxs.eq(batchId);
            NDArray batchBoxes = boxes.get(mask);
            NDArray batchScores = scores.get(mask);

            // 执行单 batch NMS
            int[] keepIndices = mtcnnNms(batchBoxes, batchScores, iouThreshold);

            if (keepIndices.length > 0) {
                // 将局部索引映射回全局索引
                NDArray globalIndices = manager.arange(boxes.getShape().get(0))
                        .get(mask)
                        .toType(DataType.INT64, false)
                        .get(manager.create(keepIndices));

                keepList.add(globalIndices);
            }
        }

        if (keepList.isEmpty()) {
            return manager.create(new long[0]);
        }
        return NDArrays.concat(new NDList(keepList));
    }


    public static int[] mtcnnNms(NDArray boxes, NDArray scores, float iouThreshold) {
        if (boxes.isEmpty()) {
            return new int[0];
        }

        NDArray x1 = boxes.get(":, 0");
        NDArray y1 = boxes.get(":, 1");
        NDArray x2 = boxes.get(":, 2");
        NDArray y2 = boxes.get(":, 3");

        // 面积
        NDArray areas = x2.sub(x1).add(1).mul(y2.sub(y1).add(1));

        // scores 降序索引
        NDArray order = scores.argSort();
        //System.out.println("order：" + order.getShape());
        //System.out.println("order：" + Arrays.toString(order.toLongArray()));

        List<Integer> keep = new ArrayList<>();

        while (order.size() > 0) {
            int i = (int) order.getLong(-1);
            keep.add(i);

            if (order.size() == 1) break; // 没框了就退出

            // 剩余框
            NDArray idx = order.get("0:-1");

            NDArray xx1 = x1.get(i).maximum(x1.get(idx));
            NDArray yy1 = y1.get(i).maximum(y1.get(idx));
            NDArray xx2 = x2.get(i).minimum(x2.get(idx));
            NDArray yy2 = y2.get(i).minimum(y2.get(idx));

            NDArray w = xx2.sub(xx1).add(1).maximum(0);
            NDArray h = yy2.sub(yy1).add(1).maximum(0);
            NDArray inter = w.mul(h);

            NDArray union = areas.get(i).minimum(areas.get(idx));
            NDArray iou = inter.div(union);

//            System.out.println("Max IoU: " + iou.max().getFloat());
//            System.out.println("Min IoU: " + iou.min().getFloat());
//            System.out.println("Mean IoU: " + iou.mean().getFloat());

//            System.out.println("Before: " + order.size());
            // 保留 IoU <= 阈值的框
            NDArray mask = iou.lte(iouThreshold);
//            System.out.println("Mask size: " + mask.size() + " True count: " + mask.sum());

            // 更新 order
            order = idx.get(mask);
//            System.out.println("After: " + order.size());
        }

        return keep.stream().mapToInt(Integer::intValue).toArray();
    }

}
