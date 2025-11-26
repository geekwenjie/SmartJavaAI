package cn.smartjavaai.speech.tts.entity;

import com.k2fsa.sherpa.onnx.OfflineTtsCallback;
import lombok.Data;

/**
 * @author dwj
 */
@Data
public class SherpaTtsParams extends TtsParams{

    private OfflineTtsCallback callback;

}
