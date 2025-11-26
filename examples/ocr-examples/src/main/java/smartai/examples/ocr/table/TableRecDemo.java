package smartai.examples.ocr.table;

import ai.djl.modality.cv.Image;
import cn.hutool.core.io.FileUtil;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.ocr.config.*;
import cn.smartjavaai.ocr.entity.OcrInfo;
import cn.smartjavaai.ocr.entity.TableStructureResult;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.enums.TableStructureModelEnum;
import cn.smartjavaai.ocr.factory.OcrModelFactory;
import cn.smartjavaai.ocr.factory.TableRecModelFactory;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import cn.smartjavaai.ocr.model.table.TableRecognizer;
import cn.smartjavaai.ocr.model.table.TableStructureModel;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * OCR 表格识别 示例
 * 模型下载地址：https://pan.baidu.com/s/1MLfd73Vjdpnuls9-oqc9uw?pwd=1234 提取码: 1234
 * 开发文档：http://doc.smartjavaai.cn/
 * @author dwj
 */
@Slf4j
public class TableRecDemo {


    @BeforeClass
    public static void beforeAll() throws IOException {
        SmartImageFactory.setEngine(SmartImageFactory.Engine.OPENCV);
        //修改缓存路径
//        Config.setCachePath("/Users/xxx/smartjavaai_cache");
    }

    //设备类型
    public static DeviceEnum device = DeviceEnum.CPU;

    /**
     * 获取通用识别模型（不带方向矫正）
     * @return
     */
    public OcrCommonRecModel getRecModel(){
        OcrRecModelConfig recModelConfig = new OcrRecModelConfig();
        //指定文本识别模型，切换模型需要同时修改modelEnum及modelPath
        recModelConfig.setRecModelEnum(CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL);
        //指定识别模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        recModelConfig.setRecModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_rec_infer/PP-OCRv5_mobile_rec_infer.onnx");
        recModelConfig.setDevice(device);
        recModelConfig.setTextDetModel(getDetectionModel());
        return OcrModelFactory.getInstance().getRecModel(recModelConfig);
    }

    /**
     * 获取文本检测模型
     * @return
     */
    public OcrCommonDetModel getDetectionModel() {
        OcrDetModelConfig config = new OcrDetModelConfig();
        //指定检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL);
        //指定模型位置，需要更改为自己的模型路径（下载地址请查看文档）
        config.setDetModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-OCRv5_mobile_det_infer/PP-OCRv5_mobile_det_infer.onnx");
        config.setDevice(device);
        return OcrModelFactory.getInstance().getDetModel(config);
    }

    /**
     * 获取方向检测模型
     * @return
     */
    public OcrDirectionModel getDirectionModel(){
        DirectionModelConfig directionModelConfig = new DirectionModelConfig();
        //指定行文本方向检测模型，切换模型需要同时修改modelEnum及modelPath
        directionModelConfig.setModelEnum(DirectionModelEnum.PP_LCNET_X0_25);
        //指定行文本方向检测模型路径，需要更改为自己的模型路径（下载地址请查看文档）
        directionModelConfig.setModelPath("/Users/wenjie/Documents/develop/model/ocr/PP-LCNet_x0_25_textline_ori_infer/PP-LCNet_x0_25_textline_ori_infer.onnx");
        directionModelConfig.setDevice(device);
        return OcrModelFactory.getInstance().getDirectionModel(directionModelConfig);
    }

    /**
     * 创建表格结构识别模型
     * @return
     */
    public TableStructureModel getTableStructureModel(){
        TableStructureConfig config = new TableStructureConfig();
        //指定行文本方向检测模型，切换模型需要同时修改modelEnum及modelPath
        config.setModelEnum(TableStructureModelEnum.SLANET_PLUS);
        //指定行文本方向检测模型路径，需要更改为自己的模型路径（下载地址请查看文档）
        config.setModelPath("/Users/wenjie/Documents/develop/model/ocr/slanet-plus/slanet-plus.onnx");
//        config.setModelPath("/Users/xxx/Documents/develop/model/ocr/SLANet_infer/SLANet.onnx");
        config.setDevice(device);
        return TableRecModelFactory.getInstance().getTableStructureModel(config);
    }




    /**
     * 表格识别
     * 仅支持简单表格
     * 流程：表格结构识别 -> 文本检测 -> 文本识别 -> 合成html table
     * 注意事项：
     * 1、批量检测时，模型应统一放在外层 try 中使用，避免重复加载，自动释放资源更安全。
     * 2、模型文件需要放在单独文件夹
     */
    @Test
    public void recognize2(){
        try {
            TableStructureModel tableStructureModel = getTableStructureModel();
            OcrCommonDetModel detModel = getDetectionModel();
            OcrCommonRecModel recModel = getRecModel();
            OcrDirectionModel directionModel = getDirectionModel();
            //创建表格识别器
            TableRecognizer tableRecognizer = TableRecognizer.builder()
                    .withStructureModel(tableStructureModel)
                    .withTextDetModel(detModel)
//                .withDirectionModel(getDirectionModel()) //如果表格中存在旋转的文字，可以使用方向分类模型
                    .withTextRecModel(recModel).build();
            String imagePath = "src/main/resources/table/table_ch1.png";
            //创建Image对象，可以从文件、url、InputStream创建、BufferedImage、Base64创建，具体使用方法可以查看文档
            Image image = SmartImageFactory.getInstance().fromFile(imagePath);
            R<TableStructureResult> result = tableRecognizer.recognize(image);
            if(result.isSuccess()){
                log.info("result: {}", result.getData().getHtml());
                //导出html内容到文件
                Path outputPath = Paths.get("output/table_ch2_result.html");
                FileUtil.writeUtf8String(result.getData().getHtml(), outputPath.toAbsolutePath().toString());
                //绘制表格结构
                Image resultImage = tableRecognizer.drawTable(result.getData(), image);
                ImageUtils.save(resultImage, "output/table_ch2_result.jpg");
                //导出excel，如果导出失败，可能是因为表格结果识别的结果是错乱的
                try (OutputStream out = Files.newOutputStream(Paths.get("output/table_ch2_result2.xls"))) {
                    tableRecognizer.exportExcel(result.getData().getHtml(), out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
