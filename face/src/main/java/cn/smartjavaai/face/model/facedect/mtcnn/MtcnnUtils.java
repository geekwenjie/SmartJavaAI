package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;

/**
 * @author dwj
 */
public class MtcnnUtils {

    /**
     * 矩形框重新映射
     * @param bboxA
     * @return
     */
    public static NDArray rerec(NDArray bboxA) {
        // h = y2 - y1
        NDArray h = bboxA.get(":, 3").sub(bboxA.get(":, 1"));
        // w = x2 - x1
        NDArray w = bboxA.get(":, 2").sub(bboxA.get(":, 0"));

        // l = max(w, h)
        NDArray l = w.maximum(h);

        // x1 = x1 + w*0.5 - l*0.5
        NDArray x1 = bboxA.get(":, 0").add(w.mul(0.5)).sub(l.mul(0.5));
        // y1 = y1 + h*0.5 - l*0.5
        NDArray y1 = bboxA.get(":, 1").add(h.mul(0.5)).sub(l.mul(0.5));

        // x2, y2
        NDArray x2 = x1.add(l);
        NDArray y2 = y1.add(l);

        // 坐标变成 [N,1] 方便拼接
        NDArray coords = NDArrays.concat(
                new NDList(
                        x1.expandDims(1),
                        y1.expandDims(1),
                        x2.expandDims(1),
                        y2.expandDims(1)
                ),
                1
        );

        // 保留原来的 score（bboxA[:, 4:]）
        if (bboxA.getShape().get(1) > 4) {
            NDArray rest = bboxA.get(":, 4:");
            return NDArrays.concat(new NDList(coords, rest), 1);
        } else {
            return coords;
        }
    }

    /**
     * 限制范围
     * @param boxes
     * @param w
     * @param h
     * @return
     */
    public static NDList pad(NDArray boxes, int w, int h) {
        // 去小数 -> 转 int
        boxes = boxes.floor().toType(DataType.INT32, false);
        NDArray x  = boxes.get(":, 0");
        NDArray y  = boxes.get(":, 1");
        NDArray ex = boxes.get(":, 2");
        NDArray ey = boxes.get(":, 3");
        // 限制范围
        x  = x.maximum(1);
        y  = y.maximum(1);
        ex = ex.minimum(w);
        ey = ey.minimum(h);
        return new NDList(y, ey, x, ex);
    }

    /**
     * bbox regression
     * @param boundingbox
     * @param reg
     * @return
     */
    public static NDArray bbreg(NDArray boundingbox, NDArray reg) {

        // 如果 reg 是形状 [N,1,H,W]，重塑为 [H,W] 或 [N,H] 这里假设 NCHW
        if (reg.getShape().get(1) == 1) {
            reg = reg.reshape(reg.getShape().get(2), reg.getShape().get(3));
        }

        // 确保 float32
        boundingbox = boundingbox.toType(DataType.FLOAT32, false);
        reg = reg.toType(DataType.FLOAT32, false);

        // 计算宽高
        NDArray w = boundingbox.get(":, 2").sub(boundingbox.get(":, 0")).add(1);
        NDArray h = boundingbox.get(":, 3").sub(boundingbox.get(":, 1")).add(1);

        NDArray b1 = boundingbox.get(":, 0").add(reg.get(":, 0").mul(w));
        NDArray b2 = boundingbox.get(":, 1").add(reg.get(":, 1").mul(h));
        NDArray b3 = boundingbox.get(":, 2").add(reg.get(":, 2").mul(w));
        NDArray b4 = boundingbox.get(":, 3").add(reg.get(":, 3").mul(h));

        // stack + transpose 对应 Python stack + permute
        NDArray newBox = NDArrays.stack(new NDList(b1, b2, b3, b4), 0).transpose();

        // 更新 boundingbox[:, :4]
        boundingbox.set(new NDIndex(":, 0:4"), newBox);
        return boundingbox;
    }
}
