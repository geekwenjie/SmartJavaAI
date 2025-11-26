package cn.smartjavaai.face.model.facedect.mtcnn;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.TranslateException;
import cn.smartjavaai.common.utils.NMSUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dwj
 */
@Slf4j
public class RNetModel {

    public static NDList secondStage(NDManager manager, Predictor<NDList, NDList> rnetPredictor,NDArray imgs, NDArray boxes, NDList pad,  NDArray image_inds) throws TranslateException {
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
                        image_inds.getLong(k) + ", :" +
                                ", " + (y.getInt(k) - 1) + ":" + ey.getInt(k) +
                                ", " + (x.getInt(k) - 1) + ":" + ex.getInt(k)
                ).expandDims(0); // 加 batch 维

                // 缩放到 (24, 24)

                //  (N, H, W, C)
                NDArray transposed = imgK.transpose(0, 2, 3, 1);
                transposed = NDImageUtils.resize(transposed, 24, 24, Image.Interpolation.AREA);
                // (N, C, H, W)
                transposed = transposed.transpose(0, 3, 1, 2);
                crops.add(transposed);
            }
        }
        if (crops.isEmpty()) {
            log.debug("No face detected.");
            return null;
        }

        // 合并成一个 batch
        NDArray im_data = NDArrays.concat(new NDList(crops), 0);
        // 归一化
        im_data = im_data.sub(127.5).mul(0.0078125);
        NDList out = rnetPredictor.predict(new NDList(im_data));
        // 假设 out 是 NDList，threshold 是 float[]，NMSUtils2.batchedNms 已经有了
        NDArray out0 = out.get(0).transpose(1, 0); // permute(1,0)
        NDArray out1 = out.get(1).transpose(1, 0);
        NDArray score = out1.get(1); // out1[1, :]
        NDArray ipass = score.gt(0.7); // score > threshold[1]

        // 筛选 boxes 和 scores
        // 先获取布尔索引为 true 的行索引
        long[] validIndices = ipass.nonzero().toLongArray();
        // 筛选 boxes 对应行
        NDArray boxesSelected = boxes.get(manager.create(validIndices)); // 行筛选
        // 取前 4 列
        boxesSelected = boxesSelected.get(":, 0:4"); // 只保留前 4 列
        NDArray scoresFiltered = score.get(ipass).reshape(-1, 1); // score[ipass].unsqueeze(1)
        boxes = NDArrays.concat(new NDList(boxesSelected, scoresFiltered), 1); // 拼接成 (N,5)

        // 筛选 image_inds
        NDArray image_indsFiltered = image_inds.get(ipass);

        // out0: (4, N)
        NDArray mv = out0.transpose() // (N, 4)
                .get(ipass); // 1-D 花式索引在第 0 维，得到 (k, 4)
//                .transpose(); // 如需要保持 (k, 4) 可省略；如想与 Python 顺序一致可再转置


        // NMS
        NDArray pick = NMSUtils.batchedNms(boxes.get(":, :4"), boxes.get(":, 4"), image_indsFiltered, 0.7f, manager);

        // 最终筛选
        boxes = boxes.get(pick);
        image_indsFiltered = image_indsFiltered.get(pick);
        mv = mv.get(pick);

        // 框回归和方形化
        boxes = MtcnnUtils.bbreg(boxes, mv);
        boxes = MtcnnUtils.rerec(boxes);

        if(boxes.size(0) == 0){
            log.debug("No face detected.");
            return null;
        }
        return new NDList(image_indsFiltered, scoresFiltered,boxes);
    }

}
