package cn.smartjavaai.face.seetaface;

import com.seeta.sdk.QualityOfLBN;
import lombok.Data;

/**
 * Seetaface6 清晰度（深度学习）结果
 * @author dwj
 * @date 2025/6/25
 */
@Data
public class ClarityDLResult {

    private QualityOfLBN.LIGHTSTATE lightstate;
    private QualityOfLBN.BLURSTATE blurstate;
    private QualityOfLBN.NOISESTATE noisestate;

    public ClarityDLResult(int[] light, int[] blur, int[] noise) {
        this.lightstate = QualityOfLBN.LIGHTSTATE.values()[light[0]];
        this.blurstate = QualityOfLBN.BLURSTATE.values()[blur[0]];
        this.noisestate = QualityOfLBN.NOISESTATE.values()[noise[0]];
    }

}
