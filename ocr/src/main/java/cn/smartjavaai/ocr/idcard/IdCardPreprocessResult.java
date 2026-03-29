package cn.smartjavaai.ocr.idcard;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.ocr.entity.OcrBox;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 身份证预处理结果。
 *
 * - processedImage: 预处理后的图像；若无需旋转，则通常与原图一致
 * - reusableBoxes: 可复用的文本检测框；仅在无需旋转时可直接复用
 * - rotated: 是否进行了整图旋转
 */
@Data
@Accessors(chain = true)
public class IdCardPreprocessResult {

    private Image processedImage;

    private List<OcrBox> reusableBoxes;

    private boolean rotated;
}
