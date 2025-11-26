package cn.smartjavaai.ocr.model.plate.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.ocr.entity.PlateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dwj
 */
public class CRNNPlateRecTranslator implements Translator<Image, PlateResult> {

    private static final String plateName = "#京沪津渝冀晋蒙辽吉黑苏浙皖闽赣鲁豫鄂湘粤桂琼川贵云藏陕甘青宁新学警港澳挂使领民航危0123456789ABCDEFGHJKLMNPQRSTUVWXYZ险品";
    private static final String[] plateColors = {"黑色", "蓝色", "绿色", "白色", "黄色"};
    private static final float MEAN = 0.588f;
    private static final float STD = 0.193f;

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();

        // Resize to (168, 48)
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        array = NDImageUtils.resize(array, 168, 48);

        // Normalize
        array = array.toType(DataType.FLOAT32, false)
                .div(255f)
                .sub(MEAN)
                .div(STD);

        // HWC to CHW
        array = array.transpose(2, 0, 1);
        array = array.expandDims(0); // batch dimension

        return new NDList(array);
    }

    @Override
    public PlateResult processOutput(TranslatorContext ctx, NDList list) {
        NDArray plateOutput = list.get(0);  // shape: [1, T, num_classes]
        NDArray colorOutput = list.get(1);  // shape: [1, num_colors]

        int[] plateIdx = plateOutput.argMax(-1)
                .toType(DataType.INT32, false)
                .toIntArray();
        int colorIdx = colorOutput.argMax(1).toType(DataType.INT32, false).toIntArray()[0];

        String plateNo = decodePlate(plateIdx);
        String plateColor = plateColors[colorIdx];

        return new PlateResult(plateNo, plateColor);
    }

    private String decodePlate(int[] preds) {
        int pre = 0;
        List<Integer> newPreds = new ArrayList<>();
        for (int idx : preds) {
            if (idx != 0 && idx != pre) {
                newPreds.add(idx);
            }
            pre = idx;
        }

        StringBuilder sb = new StringBuilder();
        for (int i : newPreds) {
            if (i >= 0 && i < plateName.length()) {
                sb.append(plateName.charAt(i));
            }
        }
        return sb.toString();
    }

    @Override
    public Batchifier getBatchifier() {
        return null; // 非批量任务
    }
}

