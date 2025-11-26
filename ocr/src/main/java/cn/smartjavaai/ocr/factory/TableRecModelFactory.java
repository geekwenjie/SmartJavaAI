package cn.smartjavaai.ocr.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.config.TableStructureConfig;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.enums.TableStructureModelEnum;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModelImpl;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.direction.PPOCRMobileV2ClsModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModelImpl;
import cn.smartjavaai.ocr.model.table.CommonTableStructureModel;
import cn.smartjavaai.ocr.model.table.TableStructureModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCR 表格识别模型工厂
 * @author dwj
 */
@Slf4j
public class TableRecModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile TableRecModelFactory instance;

    /**
     * 模型缓存
     */
    private static final ConcurrentHashMap<TableStructureModelEnum, TableStructureModel> tableStructureModelMap = new ConcurrentHashMap<>();


    /**
     * 模型注册表
     */
    private static final Map<TableStructureModelEnum, Class<? extends TableStructureModel>> tableStructureRegistry =
            new ConcurrentHashMap<>();


    public static TableRecModelFactory getInstance() {
        if (instance == null) {
            synchronized (TableRecModelFactory.class) {
                if (instance == null) {
                    instance = new TableRecModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param tableStructureModelEnum
     * @param clazz
     */
    private static void registerTableStructureModel(TableStructureModelEnum tableStructureModelEnum, Class<? extends TableStructureModel> clazz) {
        tableStructureRegistry.put(tableStructureModelEnum, clazz);
    }


    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public TableStructureModel getTableStructureModel(TableStructureConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return tableStructureModelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createTableStructureModel(config);
        });
    }



    /**
     * 创建模型
     * @param config
     * @return
     */
    private TableStructureModel createTableStructureModel(TableStructureConfig config) {
        Class<?> clazz = tableStructureRegistry.get(config.getModelEnum());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        TableStructureModel model = null;
        try {
            model = (TableStructureModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }




    // 初始化默认算法
    static {
        registerTableStructureModel(TableStructureModelEnum.SLANET, CommonTableStructureModel.class);
        registerTableStructureModel(TableStructureModelEnum.SLANET_PLUS, CommonTableStructureModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }

    /**
     * 关闭所有已加载的模型
     */
    public void closeAll() {
        tableStructureModelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        tableStructureModelMap.clear();
    }

    /**
     * 移除缓存的模型
     * @param modelEnum
     */
    public static void removeFromCache(TableStructureModelEnum modelEnum) {
        tableStructureModelMap.remove(modelEnum);
    }

}
