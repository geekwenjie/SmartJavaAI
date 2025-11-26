package cn.smartjavaai.common.utils;

import cn.hutool.core.codec.Base64;

/**
 *
 * @author dwj
 * @date 2025/6/28
 */
public class Base64ImageUtils {


    /**
     * 将 Base64 字符串（可带头部）转图片
     */
    public static byte[] base64ToImage(String base64Str){
        String cleanBase64 = stripBase64Header(base64Str);
        return Base64.decode(cleanBase64);
    }

    /**
     * 检查 Base64 字符串是否带有 Data URI 头部
     */
    public static boolean hasBase64Header(String base64Str) {
        return base64Str != null && base64Str.startsWith("data:") && base64Str.contains(";base64,");
    }

    /**
     * 去除 Base64 字符串的 Data URI 头部
     */
    public static String stripBase64Header(String base64Str) {
        if (hasBase64Header(base64Str)) {
            return base64Str.substring(base64Str.indexOf(",") + 1);
        }
        return base64Str;
    }

}
