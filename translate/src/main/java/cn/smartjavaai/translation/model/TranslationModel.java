package cn.smartjavaai.translation.model;

import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.translation.config.TranslationModelConfig;
import cn.smartjavaai.translation.entity.TranslateParam;

/**
 * 机器翻译通用检测模型
 * @author lwx
 * @date 2025/6/05
 */
public interface TranslationModel extends AutoCloseable{

    /**
     * 加载模型
     * @param config
     */
    void loadModel(TranslationModelConfig config); // 加载模型


    /**
     * 机器翻译
     * @param translateParam 翻译参数
     * @return
     */
    default R<String> translate(TranslateParam translateParam) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    /**
     * 机器翻译
     * @param input 输入文本
     * @return
     */
    default R<String> translate(String input) {
        throw new UnsupportedOperationException("默认不支持该功能");
    }


    default void setFromFactory(boolean fromFactory){
        throw new UnsupportedOperationException("默认不支持该功能");
    }


}
