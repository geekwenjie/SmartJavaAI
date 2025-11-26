package cn.smartjavaai.ocr.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.ocr.config.PlateDetModelConfig;
import cn.smartjavaai.ocr.config.PlateRecModelConfig;
import cn.smartjavaai.ocr.config.TableStructureConfig;
import cn.smartjavaai.ocr.enums.*;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.plate.CRNNPlateRecModel;
import cn.smartjavaai.ocr.model.plate.PlateDetModel;
import cn.smartjavaai.ocr.model.plate.PlateRecModel;
import cn.smartjavaai.ocr.model.plate.Yolov5PlateDetModel;
import cn.smartjavaai.ocr.model.table.CommonTableStructureModel;
import cn.smartjavaai.ocr.model.table.TableStructureModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 车牌识别模型工厂
 * @author dwj
 */
@Slf4j
public class PlateModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile PlateModelFactory instance;

    /**
     * 模型缓存
     */
    private static final ConcurrentHashMap<PlateDetModelEnum, PlateDetModel> detModelMap = new ConcurrentHashMap<>();

    /**
     * 模型缓存
     */
    private static final ConcurrentHashMap<PlateRecModelEnum, PlateRecModel> recModelMap = new ConcurrentHashMap<>();


    /**
     * 模型注册表
     */
    private static final Map<PlateDetModelEnum, Class<? extends PlateDetModel>> detModelRegistry =
            new ConcurrentHashMap<>();

    /**
     * 模型注册表
     */
    private static final Map<PlateRecModelEnum, Class<? extends PlateRecModel>> recModelRegistry =
            new ConcurrentHashMap<>();


    public static PlateModelFactory getInstance() {
        if (instance == null) {
            synchronized (PlateModelFactory.class) {
                if (instance == null) {
                    instance = new PlateModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册模型
     * @param plateDetModelEnum
     * @param clazz
     */
    private static void registerDetModel(PlateDetModelEnum plateDetModelEnum, Class<? extends PlateDetModel> clazz) {
        detModelRegistry.put(plateDetModelEnum, clazz);
    }

    /**
     * 注册模型
     * @param plateRecModelEnum
     * @param clazz
     */
    private static void registerRecModel(PlateRecModelEnum plateRecModelEnum, Class<? extends PlateRecModel> clazz) {
        recModelRegistry.put(plateRecModelEnum, clazz);
    }


    /**
     * 获取模型
     * @param config
     * @return
     */
    public PlateDetModel getDetModel(PlateDetModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return detModelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createDetModel(config);
        });
    }

    /**
     * 获取模型
     * @param config
     * @return
     */
    public PlateRecModel getRecModel(PlateRecModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return recModelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createRecModel(config);
        });
    }



    /**
     * 创建检测模型
     * @param config
     * @return
     */
    private PlateDetModel createDetModel(PlateDetModelConfig config) {
        Class<?> clazz = detModelRegistry.get(config.getModelEnum());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        PlateDetModel model = null;
        try {
            model = (PlateDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }

    /**
     * 创建识别模型
     * @param config
     * @return
     */
    private PlateRecModel createRecModel(PlateRecModelConfig config) {
        Class<?> clazz = recModelRegistry.get(config.getModelEnum());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        PlateRecModel model = null;
        try {
            model = (PlateRecModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        registerDetModel(PlateDetModelEnum.YOLOV5, Yolov5PlateDetModel.class);
        registerDetModel(PlateDetModelEnum.YOLOV7, Yolov5PlateDetModel.class);
        registerRecModel(PlateRecModelEnum.PLATE_REC_CRNN, CRNNPlateRecModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }

    /**
     * 关闭所有已加载的模型
     */
    public void closeAll() {
        detModelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        detModelMap.clear();

        recModelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        recModelMap.clear();

    }

    /**
     * 移除缓存的检测模型
     * @param modelEnum
     */
    public static void removeDetModelFromCache(PlateDetModelEnum modelEnum) {
        detModelMap.remove(modelEnum);
    }

    /**
     * 移除缓存的识别模型
     * @param modelEnum
     */
    public static void removeRecModelFromCache(PlateRecModelEnum modelEnum) {
        recModelMap.remove(modelEnum);
    }

}
