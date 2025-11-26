package cn.smartjavaai.objectdetection.criteria;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.YoloV8TranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.objectdetection.config.DetectorModelConfig;
import cn.smartjavaai.objectdetection.constant.DetectorConstant;
import cn.smartjavaai.objectdetection.exception.DetectionException;
import cn.smartjavaai.objectdetection.translator.TensorflowTranslator;
import cn.smartjavaai.vision.utils.TensorflowSynsetUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * YOLO模型Criteria 构建器
 * @author dwj
 * @date 2025/5/14
 */
public class Tensorflow2CriteriaBuilder implements CriteriaBuilderStrategy {
    @Override
    public Criteria<Image, DetectedObjects> buildCriteria(DetectorModelConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }

        Map<String, Object> customParams = getDefaultConfig();
        // 合并用户自定义参数（如有重复，覆盖默认默认值）
        if (config.getCustomParams() != null) {
            customParams.putAll(config.getCustomParams());
        }
        //解析synset
        String synsetUrl = (String)customParams.get("synsetUrl");
        String synsetPath = (String)customParams.get("synsetPath");
        String synsetFileName = (String)customParams.get("synsetFileName");
        Map<Integer, String> classes = null;
        if(StringUtils.isNotBlank(synsetUrl)){
            try {
                classes = TensorflowSynsetUtils.loadSynset(new URL(synsetUrl));
            } catch (IOException e) {
                throw new DetectionException("加载synset异常", e);
            }
        }else if(StringUtils.isNotBlank(synsetPath)){
            try {
                if(!Files.exists(Paths.get(synsetPath))){
                    throw new DetectionException("synsetPath:" + synsetPath + "不存在");
                }
                classes = TensorflowSynsetUtils.loadSynset(Paths.get(synsetPath));
            } catch (IOException e) {
                throw new DetectionException("加载synset异常", e);
            }
        }else if(StringUtils.isNotBlank(synsetFileName)){
            if(StringUtils.isBlank(config.getModelPath())){
                throw new DetectionException("指定synsetFileName，需同时指定modelPath");
            }
            try {
                Path modelPath = Paths.get(config.getModelPath());
                Path synset = modelPath.resolve(synsetFileName);
                if(!Files.exists(synset)){
                    throw new DetectionException(synset.toAbsolutePath().toString() + " 不存在");
                }
                //模型同目录下存在synsetFileName
                classes = TensorflowSynsetUtils.loadSynset(synset);
            } catch (IOException e) {
                throw new DetectionException("加载synset异常", e);
            }
        }else{
            if(StringUtils.isBlank(config.getModelPath())){
                throw new DetectionException("modelPath is null");
            }
            try {
                Path modelPath = Paths.get(config.getModelPath());
                synsetFileName = "mscoco_label_map.pbtxt";
                Path synset = modelPath.resolve(synsetFileName);
                if(!Files.exists(synset)){
                    throw new DetectionException(synset.toAbsolutePath().toString() + " 不存在");
                }
                //模型同目录下存在synsetFileName
                classes = TensorflowSynsetUtils.loadSynset(synset);
            } catch (IOException e) {
                throw new DetectionException("加载synset异常", e);
            }
        }
        customParams.put("classes", classes);
        Criteria.Builder criteriaBuilder = Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optModelPath(Paths.get(config.getModelPath()))
                .optModelName("saved_model")
                .optEngine("TensorFlow")
                .optDevice(device)
                .optTranslator(new TensorflowTranslator(customParams))
                .optProgress(new ProgressBar());
        if(config.getMaxBox() >  0){
            criteriaBuilder.optArgument("maxBox", config.getMaxBox());
        }
        Criteria<Image, DetectedObjects> criteria = criteriaBuilder.build();
        return criteria;
    }

    public Map<String, Object> getDefaultConfig(){
        return new HashMap<>();
    }
}
