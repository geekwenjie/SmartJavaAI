package cn.smartjavaai.ocr.model.table.criteria;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.ocr.config.TableStructureConfig;
import cn.smartjavaai.ocr.entity.OcrItem;
import cn.smartjavaai.ocr.entity.TableStructureResult;
import cn.smartjavaai.ocr.enums.TableStructureModelEnum;
import cn.smartjavaai.ocr.model.table.translator.TableStructTranslator;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/7/10
 */
public class StructureCriteriaFactory {


    public static Criteria<Image, TableStructureResult> createCriteria(TableStructureConfig config) {
        Device device = null;
        if(!Objects.isNull(config.getDevice())){
            device = config.getDevice() == DeviceEnum.CPU ? Device.cpu() : Device.gpu(config.getGpuId());
        }
        Criteria<Image, TableStructureResult> criteria = null;
        if(config.getModelEnum() == TableStructureModelEnum.SLANET){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, TableStructureResult.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optOption("removePass", "repeated_fc_relu_fuse_pass")
                            .optDevice(device)
                            .optTranslator(new TableStructTranslator())
                            .optProgress(new ProgressBar())
                            .build();
        }else if(config.getModelEnum() == TableStructureModelEnum.SLANET_PLUS){
            criteria =
                    Criteria.builder()
                            .optEngine("OnnxRuntime")
                            .setTypes(Image.class, TableStructureResult.class)
                            .optModelPath(Paths.get(config.getModelPath()))
                            .optOption("removePass", "repeated_fc_relu_fuse_pass")
                            .optDevice(device)
                            .optTranslator(new TableStructTranslator())
                            .optProgress(new ProgressBar())
                            .build();
        }
        return criteria;
    }


}
