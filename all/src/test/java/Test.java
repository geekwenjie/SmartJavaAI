import ai.djl.Application;
import ai.djl.Model;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.entity.OcrBox;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * @author dwj
 * @date 2025/4/24
 */
@Slf4j
public class Test {


    public static String savePath = "/Users/wenjie/Downloads/";
    //public static String image1Path = "/Users/wenjie/Documents/idea_workplace/SmartJavaAI-Demo/src/main/resources/5.jpg";
    public static String image1Path = "/Users/wenjie/Documents/idea_workplace/SmartJavaAI-Demo/src/main/resources/MJ_20250226_172200.png";

    public static String image2Path = "/Users/wenjie/Documents/idea_workplace/SmartJavaAI-Demo/src/main/resources/MJ_20250226_172222.png";

    public static void main(String[] args) throws IOException {
//        // 加载模型
//        Model model = ModelZoo.loadModel(Criteria.builder()
//                .optApplication(Application.NLP.ANY)
//                .optEngine("PyTorch")
//                .optModelName("Llama 3")
//                        .optTranslatorFactory(new Llama3TranslatorFactory())
//                .optTranslatorProvider(() -> new Llama3Translator())
//                .build());


    }


}
