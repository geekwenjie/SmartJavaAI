package smartai.examples.vision;

import ai.djl.modality.cv.Image;
import cn.smartjavaai.clip.config.ClipModelConfig;
import cn.smartjavaai.clip.enums.ClipModelEnum;
import cn.smartjavaai.clip.model.ClipModel;
import cn.smartjavaai.clip.model.ClipModelFactory;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.SimilarityUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * clip模型demo
 * @author dwj
 */
@Slf4j
public class ClipDemo {

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    @BeforeClass
    public static void beforeAll() throws IOException {
        //将图片处理的底层引擎切换为 OpenCV
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    public ClipModel getModel(){
        ClipModelConfig config = new ClipModelConfig();
        config.setModelEnum(ClipModelEnum.OPENAI);
        config.setModelPath("/Users/wenjie/Documents/develop/model/vision/clip/openai/clip.pt");
        //从jar包中加载模型
//        config.setModelPath("jar://META-INF/models/clip/openai.zip");
        config.setDevice(device);
        return ClipModelFactory.getInstance().getModel(config);
    }

    /**
     * 提取图片特征
     */
    @Test
    public void extractImageFeatures(){
        try {
            ClipModel model = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/clip/dog.jpg"));
            //获取图片特征
            R<float[]> features = model.extractImageFeatures(image);
            if(features.isSuccess()){
                log.info("图片特征：{}", features.getData());
            }else{
                log.info("图片特征获取失败：{}", features.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取文本特征
     */
    @Test
    public void extractTextFeatures() {
        try {
            ClipModel model = getModel();
            // 提取单个文本特征
            String text = "a photo of a dog";
            R<float[]> features = model.extractTextFeatures(text);
            if(features.isSuccess()){
                log.info("文本特征：{}", features.getData());
            }else{
                log.info("文本特征获取失败：{}", features.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文本搜索图像（基于图像和文本直接比对）
     */
    @Test
    public void searchImagesByText() {
        try {
            ClipModel model = getModel();
            String text = "a photo of a dog";

            // 读取图片列表
            List<File> images = ImageUtils.listImageFiles("src/main/resources/clip");
            List<Float> similarities = new ArrayList<>();

            // 1. 计算每张图片的相似度
            for (File imageFile : images) {
                Image image = SmartImageFactory.getInstance().fromFile(imageFile.toPath());
                R<Float> similarity = model.compareTextAndImage(image, text);
                if (similarity.isSuccess()) {
                    similarities.add(similarity.getData());
                    log.info("图片：{}，相似度：{}", imageFile.getName(), similarity.getData());
                } else {
                    log.warn("图片：{}，相似度计算失败：{}", imageFile.getName(), similarity.getMessage());
                }
            }
            if (similarities.isEmpty()) {
                log.warn("没有计算到有效的相似度结果");
                return;
            }

            // 2. 计算 Softmax 概率
            double total = similarities.stream()
                    .mapToDouble(Math::exp)
                    .sum();

            List<Double> probabilities = similarities.stream()
                    .map(v -> Math.exp(v) / total)
                    .collect(Collectors.toList());

            // 3. 找出相似度最高的图片
            int maxIndex = IntStream.range(0, similarities.size())
                    .boxed()
                    .max(Comparator.comparing(similarities::get))
                    .orElse(-1);

            // 4. 打印结果
            log.info("---- 结果统计 ----");
            for (int i = 0; i < images.size(); i++) {
                log.info("图片：{}，相似度：{}，概率：{}",
                        images.get(i).getName(),
                        similarities.get(i),
                        String.format("%.4f", probabilities.get(i)));
            }

            log.info("最匹配的图片：{}，相似度：{}，Softmax 概率：{}",
                    images.get(maxIndex).getName(),
                    similarities.get(maxIndex),
                    String.format("%.4f", probabilities.get(maxIndex)));

        } catch (Exception e) {
            log.error("执行 searchImagesByText 异常", e);
        }
    }

    /**
     * 文本搜索图像（基于图像和文本的特征值比对）
     */
    @Test
    public void searchImagesByText2() {
        try {
            ClipModel model = getModel();
            String text = "a photo of a dog";
            // 读取图片列表
            List<File> images = ImageUtils.listImageFiles("src/main/resources/clip");
            List<Float> similarities = new ArrayList<>();
            R<float[]> textFeatures = model.extractTextFeatures(text);
            float scale = 100f; // 缩放因子，越大 softmax 差异越明显
            // 1. 计算每张图片的相似度
            for (File imageFile : images) {
                Image image = SmartImageFactory.getInstance().fromFile(imageFile.toPath());
                R<float[]> imageFeatures = model.extractImageFeatures(image);
                if (imageFeatures.isSuccess()) {
                    float similarity = SimilarityUtil.calculate(
                            imageFeatures.getData(),
                            textFeatures.getData(),
                            SimilarityType.COSINE,
                            false
                    );
                    similarities.add(similarity * scale);
                } else {
                    log.warn("图片：{}，特征提取失败：{}", imageFile.getName(), imageFeatures.getMessage());
                    similarities.add(Float.NEGATIVE_INFINITY); // 特征提取失败，赋极小值
                }
            }

            if (similarities.isEmpty()) {
                log.warn("没有计算到有效的相似度结果");
                return;
            }

            // 2. 计算 Softmax 概率
            double total = similarities.stream()
                    .mapToDouble(Math::exp)
                    .sum();

            List<Double> probabilities = similarities.stream()
                    .map(v -> Math.exp(v) / total)
                    .collect(Collectors.toList());

            // 3. 找出相似度最高的图片
            int maxIndex = IntStream.range(0, similarities.size())
                    .boxed()
                    .max(Comparator.comparing(similarities::get))
                    .orElse(-1);

            // 4. 打印结果
            log.info("---- 结果统计 ----");
            for (int i = 0; i < images.size(); i++) {
                log.info("图片：{}，相似度：{}，概率：{}",
                        images.get(i).getName(),
                        similarities.get(i),
                        String.format("%.4f", probabilities.get(i)));
            }

            log.info("最匹配的图片：{}，相似度：{}，Softmax 概率：{}",
                    images.get(maxIndex).getName(),
                    similarities.get(maxIndex),
                    String.format("%.4f", probabilities.get(maxIndex)));

        } catch (Exception e) {
            log.error("执行 searchImagesByText 异常", e);
        }
    }


    /**
     * 图像搜索文本
     */
    @Test
    public void searchTextByImage() {
        try {
            ClipModel model = getModel();
            String[] textArray = {"a diagram", "a dog", "a cat"};
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/clip/dog.jpg"));
            //获取图片特征
            R<float[]> features = model.extractImageFeatures(image);
            List<Float> similarities = new ArrayList<>();
            // 1. 计算每张图片的相似度
            for (String text : textArray) {
                R<Float> similarity = model.compareTextAndImage(image, text);
                if (similarity.isSuccess()) {
                    similarities.add(similarity.getData());
                    log.info("文本：{}，相似度：{}", text, similarity.getData());
                } else {
                    log.warn("文本：{}，相似度计算失败：{}", text, similarity.getMessage());
                }
            }
            if (similarities.isEmpty()) {
                log.warn("没有计算到有效的相似度结果");
                return;
            }

            // 2. 计算 Softmax 概率
            double total = similarities.stream()
                    .mapToDouble(Math::exp)
                    .sum();

            List<Double> probabilities = similarities.stream()
                    .map(v -> Math.exp(v) / total)
                    .collect(Collectors.toList());

            // 3. 找出相似度最高的图片
            int maxIndex = IntStream.range(0, similarities.size())
                    .boxed()
                    .max(Comparator.comparing(similarities::get))
                    .orElse(-1);

            // 4. 打印结果
            log.info("---- 结果统计 ----");
            for (int i = 0; i < textArray.length; i++) {
                log.info("文本：{}，相似度：{}，概率：{}",
                        textArray[i],
                        similarities.get(i),
                        String.format("%.4f", probabilities.get(i)));
            }

            log.info("最匹配的文本：{}，相似度：{}，Softmax 概率：{}",
                    textArray[maxIndex],
                    similarities.get(maxIndex),
                    String.format("%.4f", probabilities.get(maxIndex)));

        } catch (Exception e) {
            log.error("执行 searchImagesByText 异常", e);
        }
    }

    /**
     * 以图搜图
     */
    @Test
    public void searchImagesByImage() {
        try {
            ClipModel model = getModel();
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image1 = SmartImageFactory.getInstance().fromFile(Paths.get("src/main/resources/cat2.jpeg"));
            List<Float> similarities = new ArrayList<>();
            // 读取图片列表
            List<File> images = ImageUtils.listImageFiles("src/main/resources/clip");
            // 1. 计算每张图片的相似度
            for (File imageFile : images) {
                Image image = SmartImageFactory.getInstance().fromFile(imageFile.toPath());
                R<Float> similarity = model.compareImage(image1, image, 100);
                if (similarity.isSuccess()) {
                    similarities.add(similarity.getData());
                    log.info("图片：{}，相似度：{}", imageFile.getName(), similarity.getData());
                } else {
                    log.warn("图片：{}，相似度计算失败：{}", imageFile.getName(), similarity.getMessage());
                }
            }
            if (similarities.isEmpty()) {
                log.warn("没有计算到有效的相似度结果");
                return;
            }

            // 2. 计算 Softmax 概率
            double total = similarities.stream()
                    .mapToDouble(Math::exp)
                    .sum();

            List<Double> probabilities = similarities.stream()
                    .map(v -> Math.exp(v) / total)
                    .collect(Collectors.toList());

            // 3. 找出相似度最高的图片
            int maxIndex = IntStream.range(0, similarities.size())
                    .boxed()
                    .max(Comparator.comparing(similarities::get))
                    .orElse(-1);

            // 4. 打印结果
            log.info("---- 结果统计 ----");
            for (int i = 0; i < images.size(); i++) {
                log.info("图片：{}，相似度：{}，概率：{}",
                        images.get(i).getName(),
                        similarities.get(i),
                        String.format("%.4f", probabilities.get(i)));
            }

            log.info("最匹配的图片：{}，相似度：{}，Softmax 概率：{}",
                    images.get(maxIndex).getName(),
                    similarities.get(maxIndex),
                    String.format("%.4f", probabilities.get(maxIndex)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
