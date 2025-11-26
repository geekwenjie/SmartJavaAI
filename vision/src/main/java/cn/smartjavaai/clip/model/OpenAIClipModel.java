package cn.smartjavaai.clip.model;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.NoopTranslator;
import ai.djl.util.Pair;
import cn.smartjavaai.clip.config.ClipModelConfig;
import cn.smartjavaai.clip.exception.ClipException;
import cn.smartjavaai.clip.pool.ClipImagePredictorFactory;
import cn.smartjavaai.clip.pool.ClipImageTextPredictorFactory;
import cn.smartjavaai.clip.pool.ClipTextPredictorFactory;
import cn.smartjavaai.clip.translator.ImageTextTranslator;
import cn.smartjavaai.clip.translator.ImageTranslator;
import cn.smartjavaai.clip.translator.TextTranslator;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.common.pool.PredictorFactory;
import cn.smartjavaai.common.utils.DJLCommonUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.SimilarityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sound.sampled.Clip;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * openai clip 模型
 * @author dwj
 */
@Slf4j
public class OpenAIClipModel implements ClipModel{

    private ClipModelConfig config;

    private ZooModel<NDList, NDList> model;

    private HuggingFaceTokenizer tokenizer;

    private GenericObjectPool<Predictor<Image, float[]>> imageFeaturePredictorPool;

    private GenericObjectPool<Predictor<String, float[]>> textFeaturePredictorPool;

    private GenericObjectPool<Predictor<Pair<Image, String>, float[]>> imgTextPredictorPool;

    @Override
    public void loadModel(ClipModelConfig config) {
        if(Objects.isNull(config)){
            throw new ClipException("config为null");
        }
        if(StringUtils.isBlank(config.getModelPath())){
            throw new ClipException("modelPath为空");
        }
        this.config = config;
        try {
//            Device device = null;
//            if(!Objects.isNull(config.getDevice())){
//                device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
//            }
            boolean isUrl = DJLCommonUtils.hasSupportedProtocol(config.getModelPath());
            Criteria<NDList, NDList> criteria =
                    Criteria.builder()
                            .setTypes(NDList.class, NDList.class)
//                            .optModelUrls("https://resources.djl.ai/demo/pytorch/clip.zip")
                            .optModelUrls(isUrl ? config.getModelPath() : null)
                            .optModelName("clip.pt")
                            .optModelPath(isUrl ? null : Paths.get(config.getModelPath()))
                            .optTranslator(new NoopTranslator())
                            .optEngine("PyTorch")
//                            .optOption("mapLocation", "true")
                            .optDevice(Device.cpu()) // torchscript model only support CPU
                            .build();
            model = criteria.loadModel();
            Path modelCachePath = model.getWrappedModel().getModelPath();
            Path tokenizerPath = modelCachePath.resolve("tokenizer.json");
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
            // 创建池子：每个线程独享 Predictor
            imageFeaturePredictorPool = new GenericObjectPool<>(new ClipImagePredictorFactory(model));
            textFeaturePredictorPool = new GenericObjectPool<>(new ClipTextPredictorFactory(model, tokenizer));
            imgTextPredictorPool = new GenericObjectPool<>(new ClipImageTextPredictorFactory(model, tokenizer));
            int predictorPoolSize = config.getPredictorPoolSize();
            if(config.getPredictorPoolSize() <= 0){
                predictorPoolSize = Runtime.getRuntime().availableProcessors(); // 默认等于CPU核心数
            }
            imageFeaturePredictorPool.setMaxTotal(predictorPoolSize);
            textFeaturePredictorPool.setMaxTotal(predictorPoolSize);
            imgTextPredictorPool.setMaxTotal(predictorPoolSize);
            log.debug("当前设备: " + model.getNDManager().getDevice());
            log.debug("当前引擎: " + Engine.getInstance().getEngineName());
            log.debug("模型推理器线程池最大数量: " + predictorPoolSize);
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            throw new ClipException("模型加载失败", e);
        }

    }

    @Override
    public R<float[]> extractImageFeatures(Image image) {
        Predictor<Image, float[]> predictor = null;
        try {
            predictor = imageFeaturePredictorPool.borrowObject();
            return R.ok(predictor.predict(image));
        } catch (Exception e) {
            throw new ClipException("特征提取错误", e);
        }finally {
            if (predictor != null) {
                try {
                    imageFeaturePredictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    @Override
    public R<float[]> extractImageFeatures(String imagePath) {
        Image image = null;
        try {
            image = SmartImageFactory.getInstance().fromFile(imagePath);
            return extractImageFeatures(image);
        } catch (IOException e) {
            throw new ClipException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(image);
        }
    }

    @Override
    public R<float[]> extractTextFeatures(String inputs) {
        Predictor<String, float[]> predictor = null;
        try {
            predictor = textFeaturePredictorPool.borrowObject();
            return R.ok(predictor.predict(inputs));
        } catch (Exception e) {
            throw new ClipException("特征提取错误", e);
        }finally {
            if (predictor != null) {
                try {
                    textFeaturePredictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    @Override
    public R<Float> compareTextAndImage(Image image, String text) {
        Predictor<Pair<Image, String>, float[]> predictor = null;
        try {
            predictor = imgTextPredictorPool.borrowObject();
            float[] imageFeatures = predictor.predict(new Pair<>(image, text));
            if (imageFeatures == null || imageFeatures.length == 0){
                return R.fail(R.Status.Unknown.getCode(), "特征为空");
            }
            return R.ok(imageFeatures[0]);
        } catch (Exception e) {
            throw new ClipException("特征提取错误", e);
        }finally {
            if (predictor != null) {
                try {
                    imgTextPredictorPool.returnObject(predictor); //归还
                } catch (Exception e) {
                    log.warn("归还Predictor失败", e);
                    try {
                        predictor.close(); // 归还失败才销毁
                    } catch (Exception ex) {
                        log.error("关闭Predictor失败", ex);
                    }
                }
            }
        }
    }

    @Override
    public R<Float> compareFeatures(float[] feature1, float[] feature2, float scale) {
        float similarity = SimilarityUtil.calculate(feature1, feature2, SimilarityType.COSINE, false);
        return R.ok(similarity * scale);
    }

    @Override
    public R<Float> compareImage(Image image1, Image image2) {
        return compareImage(image1, image2, 1.0f);
    }

    @Override
    public R<Float> compareImage(Image image1, Image image2, float scale) {
        R<float[]> features1 = extractImageFeatures(image1);
        R<float[]> features2 = extractImageFeatures(image2);
        if(!features1.isSuccess()){
            return R.fail(features1.getCode(), features1.getMessage());
        }
        if(!features2.isSuccess()){
            return R.fail(features2.getCode(), features2.getMessage());
        }
        return compareFeatures(features1.getData(), features2.getData(), scale);
    }

    @Override
    public R<Float> compareImage(String imagePath1, String imagePath2) {
        return compareImage(imagePath1, imagePath2, 1.0f);
    }

    @Override
    public R<Float> compareImage(String imagePath1, String imagePath2, float scale) {
        Image image1 = null;
        Image image2 = null;
        try {
            image1 = SmartImageFactory.getInstance().fromFile(imagePath1);
            image2 = SmartImageFactory.getInstance().fromFile(imagePath2);
            return compareImage(image1, image2, scale);
        } catch (IOException e) {
            throw new ClipException(e);
        } finally {
            ImageUtils.releaseOpenCVMat(image1);
            ImageUtils.releaseOpenCVMat(image2);
        }
    }

    @Override
    public R<Float> compareText(String input1, String input2) {
        return compareText(input1, input2, 1.0f);
    }

    @Override
    public R<Float> compareText(String input1, String input2, float scale) {
        R<float[]> features1 = extractTextFeatures(input1);
        R<float[]> features2 = extractTextFeatures(input2);
        if(!features1.isSuccess()){
            return R.fail(features1.getCode(), features1.getMessage());
        }
        if(!features2.isSuccess()){
            return R.fail(features2.getCode(), features2.getMessage());
        }
        return compareFeatures(features1.getData(), features2.getData(), scale);
    }

    @Override
    public void close() throws Exception {
        try {
            if (imageFeaturePredictorPool != null) {
                imageFeaturePredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (textFeaturePredictorPool != null) {
                textFeaturePredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (imgTextPredictorPool != null) {
                imgTextPredictorPool.close();
            }
        } catch (Exception e) {
            log.warn("关闭 predictorPool 失败", e);
        }
        try {
            if (model != null) {
                model.close();
            }
        } catch (Exception e) {
            log.warn("关闭 model 失败", e);
        }
        try {
            if (tokenizer != null) {
                tokenizer.close();
            }
        } catch (Exception e) {
            log.warn("关闭 tokenizer 失败", e);
        }
    }

    private boolean fromFactory = false;
    public boolean isFromFactory() {
        return fromFactory;
    }

    @Override
    public void setFromFactory(boolean fromFactory) {
        this.fromFactory = fromFactory;
    }
}
