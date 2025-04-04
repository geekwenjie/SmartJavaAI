package cn.smartjavaai.face.algo;

import ai.djl.Device;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.ImageFeatureExtractorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.Rectangle;
import cn.smartjavaai.face.AbstractFaceAlgorithm;
import cn.smartjavaai.face.FaceDetectedResult;
import cn.smartjavaai.face.FaceDetectionTranslator;
import cn.smartjavaai.face.ModelConfig;
import cn.smartjavaai.face.translator.FaceFeatureTranslator;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dwj
 */
public class FeatureExtractionAlgo extends AbstractFaceAlgorithm {


    private Criteria<Image, float[]> faceFeatureCriteria;

    private Predictor<Image, float[]> predictor;

    private ZooModel<Image, float[]> model;

    public static final List<Float> mean =
            Arrays.asList(
                    127.5f / 255.0f,
                    127.5f / 255.0f,
                    127.5f / 255.0f,
                    128.0f / 255.0f,
                    128.0f / 255.0f,
                    128.0f / 255.0f);


    /**
     * 加载人脸特征提取模型
     * @param config
     * @throws Exception
     */
    @Override
    public void loadFaceFeatureModel(ModelConfig config) throws Exception {
        String normalize = mean.stream().map(Object::toString).collect(Collectors.joining(","));
        faceFeatureCriteria =
                Criteria.builder()
                        .setTypes(Image.class, float[].class)
                        .optModelName("face_feature") // specify model file prefix
                        .optModelUrls(StringUtils.isNotBlank(config.getModelPath()) ? null :
                                "https://resources.djl.ai/test-models/pytorch/face_feature.zip")
                        .optModelPath(StringUtils.isNotBlank(config.getModelPath()) ? Paths.get(config.getModelPath()) : null)
                        .optTranslator(new FaceFeatureTranslator())
                        .optArgument("normalize", normalize)
                        .optEngine("PyTorch") // Use PyTorch engine
                        .optProgress(new ProgressBar())
                        .build();

        model = faceFeatureCriteria.loadModel();
        predictor = model.newPredictor();
    }


    /**
     * 特征提取
     * @param imagePath 图片路径
     * @return
     * @throws Exception
     */
    @Override
    public float[] featureExtraction(String imagePath) throws Exception {
        Path imageFile = Paths.get(imagePath);
        Image img = ImageFactory.getInstance().fromFile(imageFile);
        img.getWrappedImage();
        return predictor.predict(img);
    }

    /**
     * 特征提取
     * @param inputStream 输入流
     * @return
     * @throws Exception
     */
    @Override
    public float[] featureExtraction(InputStream inputStream) throws Exception {
        Image img = ImageFactory.getInstance().fromInputStream(inputStream);
        img.getWrappedImage();
        return predictor.predict(img);
    }

    /**
     * 计算相似度
     * @param feature1 图1特征
     * @param feature2 图2特征
     * @return
     * @throws Exception
     */
    @Override
    public float calculSimilar(float[] feature1, float[] feature2) throws Exception {
        float ret = 0.0f;
        float mod1 = 0.0f;
        float mod2 = 0.0f;
        int length = feature1.length;
        for (int i = 0; i < length; ++i) {
            ret += feature1[i] * feature2[i];
            mod1 += feature1[i] * feature1[i];
            mod2 += feature2[i] * feature2[i];
        }
        return (float) ((ret / Math.sqrt(mod1) / Math.sqrt(mod2) + 1) / 2.0f);
    }

    /**
     * 特征比较
     * @param imagePath1 图1路径
     * @param imagePath2 图2路径
     * @return
     * @throws Exception
     */
    @Override
    public float featureComparison(String imagePath1, String imagePath2) throws Exception {
        float[] feature1 = featureExtraction(imagePath1);
        float[] feature2 = featureExtraction(imagePath2);
        return calculSimilar(feature1, feature2);
    }

    /**
     * 特征比较
     * @param inputStream1 图1输入流
     * @param inputStream2 图2输入流
     * @return
     * @throws Exception
     */
    @Override
    public float featureComparison(InputStream inputStream1, InputStream inputStream2) throws Exception {
        float[] feature1 = featureExtraction(inputStream1);
        float[] feature2 = featureExtraction(inputStream2);
        return calculSimilar(feature1, feature2);
    }

    /*@Override
    public float[] recognize(FaceRegion region) {
        return new float[0];
    }*/

    /*@Override
    public void loadModel(ModelConfig config) throws Exception {

    }*/
}
