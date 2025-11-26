package cn.smartjavaai.common.config;

import cn.smartjavaai.common.enums.DeviceEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型配置
 * @author dwj
 */
@Data
public class ModelConfig {

    /**
     * 设备类型
     */
    private DeviceEnum device;

    /**
     * gpu设备ID 当device为GPU时生效
     */
    private int gpuId = 0;

    /**
     * 批量数据打包方式：stack，padding
     */
    private String batchifier;

    /**
     * 模型预测器池大小(默认为cpu核心数)
     */
    private int predictorPoolSize;

    /**
     * 个性化配置（按模型类型动态解析）
     */
    private ConcurrentHashMap<String, Object> customParams = new ConcurrentHashMap<>();

    public <T> T getCustomParam(String key, Class<T> clazz) {
        Object value = customParams.get(key);
        if (value == null) return null;
        return clazz.cast(value);
    }

    public <T> T getCustomParam(String key, Class<T> clazz, T defaultValue) {
        Object value = customParams.getOrDefault(key, defaultValue);
        return clazz.cast(value);
    }


    /**
     * 添加个性化配置项
     */
    public void putCustomParam(String key, Object value) {
        if (customParams == null) {
            customParams = new ConcurrentHashMap<>();
        }
        customParams.put(key, value);
    }




}
