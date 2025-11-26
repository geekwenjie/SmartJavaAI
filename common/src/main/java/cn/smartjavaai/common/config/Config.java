package cn.smartjavaai.common.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.UserInfo;
import cn.smartjavaai.common.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;

/**
 * 全局配置
 * @author dwj
 * @date 2025/4/15
 */
@Slf4j
public class Config {

    /**
     * 默认缓存文件夹
     */
    private static final String CACHE_DIR = "smartjavaai_cache";

    private static String cachePath;

    static{
        createCachePath();
        if(StringUtils.isNotBlank(cachePath)){
            System.setProperty("DJL_CACHE_DIR", cachePath);
        }
        System.setProperty("ai.djl.default_engine", "PyTorch");
        log.info("设置默认引擎：{}", "PyTorch");
    }

    // 设置缓存路径的方法
    public static void setCachePath(String customeCachePath) {
        if (StringUtils.isNotBlank(customeCachePath)) {
            /*if(!FileUtils.isValidDirectory(customeCachePath)){
                throw new IllegalArgumentException("无效的缓存路径");
            }*/
            cachePath = customeCachePath;
            FileUtil.mkdir(cachePath);
            // 如果需要在此时直接设置系统属性
            System.setProperty("DJL_CACHE_DIR", cachePath);
        } else {
            throw new IllegalArgumentException("缓存路径不允许为空");
        }
    }

    // 获取缓存路径的方法
    public static String getCachePath() {
        if(StringUtils.isBlank(cachePath)){
            createCachePath();
        }
        if(StringUtils.isNotBlank(cachePath)){
            System.setProperty("DJL_CACHE_DIR", cachePath);
        }
        return cachePath;
    }

    // 获取当前缓存路径的系统属性（如果需要在其他地方使用）
    public static String getCachePathFromSystem() {
        return System.getProperty("DJL_CACHE_DIR");
    }

    private static void createCachePath(){
        String osName = SystemUtil.getOsInfo().getName();
        log.info("当前操作系统：{}", osName);
        if(osName.toLowerCase().contains("windows")){
            cachePath = Paths.get(
                    SystemUtil.getUserInfo().getHomeDir(),
                    "smartjavaai_cache"
            ).toString();
            FileUtil.mkdir(cachePath);
        }else if(osName.toLowerCase().contains("linux")){
            cachePath = "/root/" + CACHE_DIR;
            FileUtil.mkdir(cachePath);
        }else if(osName.toLowerCase().contains("mac")){
            cachePath = Paths.get(
                    SystemUtil.getUserInfo().getHomeDir(),
                    "smartjavaai_cache"
            ).toString();
            FileUtil.mkdir(cachePath);
        }else{
            cachePath = Paths.get(
                    SystemUtil.getUserInfo().getHomeDir(),
                    "smartjavaai_cache"
            ).toString();
            FileUtil.mkdir(cachePath);
        }
    }



}
