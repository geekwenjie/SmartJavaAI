package cn.smartjavaai.ocr.factory;

import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.ocr.config.DirectionModelConfig;
import cn.smartjavaai.ocr.config.OcrRecModelConfig;
import cn.smartjavaai.ocr.enums.CommonDetModelEnum;
import cn.smartjavaai.ocr.enums.CommonRecModelEnum;
import cn.smartjavaai.ocr.enums.DirectionModelEnum;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModel;
import cn.smartjavaai.ocr.model.common.detect.OcrCommonDetModelImpl;
import cn.smartjavaai.ocr.config.OcrDetModelConfig;
import cn.smartjavaai.ocr.exception.OcrException;
import cn.smartjavaai.ocr.model.common.direction.OcrDirectionModel;
import cn.smartjavaai.ocr.model.common.direction.PPOCRMobileV2ClsModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModel;
import cn.smartjavaai.ocr.model.common.recognize.OcrCommonRecModelImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OCR模型工厂
 * @author dwj
 */
@Slf4j
public class OcrModelFactory {

    // 使用 volatile 和双重检查锁定来确保线程安全的单例模式
    private static volatile OcrModelFactory instance;

    private static final ConcurrentHashMap<CommonDetModelEnum, OcrCommonDetModel> commonDetModelMap = new ConcurrentHashMap<>();


    private static final ConcurrentHashMap<CommonRecModelEnum, OcrCommonRecModel> commonRecModelMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<DirectionModelEnum, OcrDirectionModel> directionModelMap = new ConcurrentHashMap<>();

    /**
     * 检测模型注册表
     */
    private static final Map<CommonDetModelEnum, Class<? extends OcrCommonDetModel>> commonDetRegistry =
            new ConcurrentHashMap<>();

    /**
     * 识别模型注册表
     */
    private static final Map<CommonRecModelEnum, Class<? extends OcrCommonRecModel>> commonRecRegistry =
            new ConcurrentHashMap<>();

    /**
     * 方向分类模型注册表
     */
    private static final Map<DirectionModelEnum, Class<? extends OcrDirectionModel>> directionRegistry =
            new ConcurrentHashMap<>();


    public static OcrModelFactory getInstance() {
        if (instance == null) {
            synchronized (OcrModelFactory.class) {
                if (instance == null) {
                    instance = new OcrModelFactory();
                }
            }
        }
        return instance;
    }



    /**
     * 注册通用检测模型
     * @param detModelEnum
     * @param clazz
     */
    private static void registerCommonDetModel(CommonDetModelEnum detModelEnum, Class<? extends OcrCommonDetModel> clazz) {
        commonDetRegistry.put(detModelEnum, clazz);
    }

    /**
     * 注册通用识别模型
     * @param recModelEnum
     * @param clazz
     */
    private static void registerCommonRecModel(CommonRecModelEnum recModelEnum, Class<? extends OcrCommonRecModel> clazz) {
        commonRecRegistry.put(recModelEnum, clazz);
    }

    /**
     * 注册通用方向分类模型
     * @param directionModelEnum
     * @param clazz
     */
    private static void registerDirectionModel(DirectionModelEnum directionModelEnum, Class<? extends OcrDirectionModel> clazz) {
        directionRegistry.put(directionModelEnum, clazz);
    }


    /**
     * 获取检测模型（通过配置）
     * @param config
     * @return
     */
    public OcrCommonDetModel getDetModel(OcrDetModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return commonDetModelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createCommonDetModel(config);
        });
    }

    /**
     * 获取识别模型（通过配置）
     * @param config
     * @return
     */
    public OcrCommonRecModel getRecModel(OcrRecModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getRecModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return commonRecModelMap.computeIfAbsent(config.getRecModelEnum(), k -> {
            return createCommonRecModel(config);
        });
    }

    /**
     * 获取模型（通过配置）
     * @param config
     * @return
     */
    public OcrDirectionModel getDirectionModel(DirectionModelConfig config) {
        if(Objects.isNull(config) || Objects.isNull(config.getModelEnum())){
            throw new OcrException("未配置OCR模型");
        }
        return directionModelMap.computeIfAbsent(config.getModelEnum(), k -> {
            return createDirectionModel(config);
        });
    }



    /**
     * 创建OCR通用检测模型
     * @param config
     * @return
     */
    private OcrCommonDetModel createCommonDetModel(OcrDetModelConfig config) {
        Class<?> clazz = commonDetRegistry.get(config.getModelEnum());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrCommonDetModel model = null;
        try {
            model = (OcrCommonDetModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    /**
     * 创建OCR通用识别模型
     * @param config
     * @return
     */
    private OcrCommonRecModel createCommonRecModel(OcrRecModelConfig config) {
        Class<?> clazz = commonRecRegistry.get(config.getRecModelEnum());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrCommonRecModel model = null;
        try {
            model = (OcrCommonRecModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }

    /**
     * 创建OCR方向分类模型
     * @param config
     * @return
     */
    private OcrDirectionModel createDirectionModel(DirectionModelConfig config) {
        Class<?> clazz = directionRegistry.get(config.getModelEnum());
        if(clazz == null){
            throw new OcrException("Unsupported model");
        }
        OcrDirectionModel model = null;
        try {
            model = (OcrDirectionModel) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new OcrException(e);
        }
        model.loadModel(config);
        model.setFromFactory(true);
        return model;
    }


    // 初始化默认算法
    static {
        //通用-检测模型
        registerCommonDetModel(CommonDetModelEnum.PP_OCR_V5_SERVER_DET_MODEL, OcrCommonDetModelImpl.class);
        registerCommonDetModel(CommonDetModelEnum.PP_OCR_V5_MOBILE_DET_MODEL, OcrCommonDetModelImpl.class);
        registerCommonDetModel(CommonDetModelEnum.PP_OCR_V4_SERVER_DET_MODEL, OcrCommonDetModelImpl.class);
        registerCommonDetModel(CommonDetModelEnum.PP_OCR_V4_MOBILE_DET_MODEL, OcrCommonDetModelImpl.class);
        registerCommonRecModel(CommonRecModelEnum.PP_OCR_V5_SERVER_REC_MODEL, OcrCommonRecModelImpl.class);
        registerCommonRecModel(CommonRecModelEnum.PP_OCR_V5_MOBILE_REC_MODEL, OcrCommonRecModelImpl.class);
        registerCommonRecModel(CommonRecModelEnum.PP_OCR_V4_SERVER_REC_MODEL, OcrCommonRecModelImpl.class);
        registerCommonRecModel(CommonRecModelEnum.PP_OCR_V4_MOBILE_REC_MODEL, OcrCommonRecModelImpl.class);
        registerDirectionModel(DirectionModelEnum.CH_PPOCR_MOBILE_V2_CLS, PPOCRMobileV2ClsModel.class);
        registerDirectionModel(DirectionModelEnum.PP_LCNET_X0_25, PPOCRMobileV2ClsModel.class);
        registerDirectionModel(DirectionModelEnum.PP_LCNET_X1_0, PPOCRMobileV2ClsModel.class);
        log.debug("缓存目录：{}", Config.getCachePath());
    }


    /**
     * 关闭所有已加载的模型
     */
    public void closeAll() {
        commonDetModelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        commonDetModelMap.clear();

        commonRecModelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        commonRecModelMap.clear();

        directionModelMap.values().forEach(model -> {
            try {
                model.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        directionModelMap.clear();
    }

    /**
     * 移除缓存的检测模型
     * @param modelEnum
     */
    public static void removeDetModelFromCache(CommonDetModelEnum modelEnum) {
        commonDetModelMap.remove(modelEnum);
    }

    /**
     * 移除缓存的识别模型
     * @param modelEnum
     */
    public static void removeRecModelFromCache(CommonRecModelEnum modelEnum) {
        commonRecModelMap.remove(modelEnum);
    }

    /**
     * 移除缓存的方向分类模型
     * @param modelEnum
     */
    public static void removeDirectionModelFromCache(DirectionModelEnum modelEnum) {
        directionModelMap.remove(modelEnum);
    }

}
