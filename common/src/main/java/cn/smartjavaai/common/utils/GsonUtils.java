package cn.smartjavaai.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @author dwj
 */
public class GsonUtils {

    private static final Gson GSON = new Gson();

    private GsonUtils() {
        // 私有构造，防止实例化
    }

    /**
     * 将 JSON 字符串安全转换为 JsonObject
     *
     * @param jsonStr JSON 字符串
     * @return JsonObject，如果解析失败返回 null
     */
    public static JsonObject parseToJsonObject(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return null;
        }
        try {
            return JsonParser.parseString(jsonStr).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            // 解析失败返回 null
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型对象
     *
     * @param jsonStr JSON 字符串
     * @param clazz   目标类型
     * @param <T>     类型参数
     * @return 对象实例，如果解析失败返回 null
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(jsonStr, clazz);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串，如果对象为 null 返回 null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }

}
