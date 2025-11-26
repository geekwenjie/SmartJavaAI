package cn.smartjavaai.face.seetaface;


import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import cn.smartjavaai.common.config.Config;
import cn.smartjavaai.common.enums.DeviceEnum;
import cn.smartjavaai.face.exception.FaceException;
import com.seeta.sdk.util.DllItem;
import com.seeta.sdk.util.LoadNativeCore;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 依赖库加载器
 * @author dwj
 */
@Slf4j
public class NativeLoader {


    private static Path seetaface6NativePath;

    private static final String SEETAFACE_LIB_DIR = "seetaface6";


    /**
     * 定义dll 路径和加载顺序的文件
     */
    private static final String PROPERTIES_FILE_NAME = "dll.properties";

    // 使用 volatile 保证内存可见性
    private static volatile boolean isDllLoaded = false;



    public static void loadNativeLibraries(DeviceEnum device) {
        try {
            if (!isDllLoaded) {
                synchronized (NativeLoader.class) {
                    if (!isDllLoaded) {  // 双重检查
                        OsInfo osInfo = SystemUtil.getOsInfo();
                        //检查当前系统是否支持
                        if(!osInfo.isWindows() && !osInfo.isLinux()){
                            throw new FaceException("当前系统不支持：" + osInfo.getName());
                        }
                        //判断硬件架构是否支持GPU
                        if(device != null && device.equals(DeviceEnum.GPU)){
                            //GPU仅支持amd64
                            if(!osInfo.getArch().contains("amd64") && !osInfo.getArch().contains("x86_64")){
                                throw new FaceException("seetaface6 GPU模型不支持当前arch：" + osInfo.getArch());
                            }
                        }
                        seetaface6NativePath = Paths.get(Config.getCachePath(), SEETAFACE_LIB_DIR);
                        //创建目录
                        FileUtil.mkdir(seetaface6NativePath);
                        log.debug("seetaface6依赖库路径: " + seetaface6NativePath.toAbsolutePath().toString());
                        //拷贝依赖库到缓存目录
                        List<File> fileList = getLibFiles(osInfo, device);
                        if(fileList != null && !fileList.isEmpty()){
                            // 加载依赖库文件
                            fileList.forEach(file -> {
                                System.load(file.getAbsolutePath());
                                //log.debug(String.format("load %s finish", file.getAbsolutePath()));
                            });
                        }
                        log.debug("seetaface6 依赖库加载完毕");
                        isDllLoaded = true;
                    }
                }
            } else {
                log.debug("SeetaFace DLL is already loaded.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Native library loading failed", e);
        }
    }

    /**
     * 拷贝依赖库到缓存目录
     * @param osInfo
     * @return
     */
    private static List<File> getLibFiles(OsInfo osInfo,DeviceEnum deviceEnum){
        try {
            String device = getDevice(deviceEnum);
            log.debug("当前设备：{}", device);
            //获取dll文件列表
            List<DllItem> baseList = new ArrayList<>();
            List<DllItem> jniList = new ArrayList<>();
            InputStream propsInputStream = LoadNativeCore.class.getResourceAsStream(getPropertiesPath());
            Props props = new Props();
            props.load(propsInputStream);
            String prefix = getPrefix();
            props.forEach((keyObj, valuObj) -> {
                String key = (String) keyObj;
                String value = (String) valuObj;
                DllItem dllItem = new DllItem();
                dllItem.setKey(key);
                if (key.contains("base")) {
                    if (value.contains("tennis")) {
                        dllItem.setValue(prefix + "base/" + device + "/" + value);
                    } else {
                        dllItem.setValue(prefix + "base/" + value);
                    }
                    baseList.add(dllItem);
                } else {
                    dllItem.setValue(prefix + value);
                    jniList.add(dllItem);
                }
            });
            //给dll文件排序
            List<String> basePath = getSortedPath(baseList);
            List<String> sdkPath = getSortedPath(jniList);
            List<File> fileList = new ArrayList<>();
            //拷贝文件到临时目录
            for (String baseSo : basePath) {
                fileList.add(extractLibrary(baseSo));
            }
            for (String sdkSo : sdkPath) {
                fileList.add(extractLibrary(sdkSo));
            }
            return fileList;
        } catch (Exception e) {
            throw new FaceException("拷贝依赖库失败",e);
        }
    }

    private static String getDevice(DeviceEnum deviceEnum) {
        String device = "CPU";
        if ("amd64".equals(getArch()) && deviceEnum != null) {
            device = deviceEnum == DeviceEnum.GPU ? "GPU" : "CPU";
        }
        return device;
    }


    /**
     * 返回路径文件前缀
     *
     * @return
     */
    private static String getPrefix() {
        String arch = getArch();
        //aarch64
        String os = SystemUtil.getOsInfo().getName();
        //Windows操作系统
        if (os != null && os.toLowerCase().startsWith("windows")) {
            os = "/windows/";
        } else if (os != null && os.toLowerCase().startsWith("linux")) {//Linux操作系统
            os = "/linux/";
        } else { //其它操作系统
            //安卓 乌班图等等，先不写
            return null;
        }
        // "/seetaface6/windows/amd64"
        return "/" + SEETAFACE_LIB_DIR + os + arch + "/";
    }

    private static String getArch() {
        String arch = SystemUtil.getOsInfo().getArch().toLowerCase();
        if (arch.startsWith("amd64")
                || arch.startsWith("x86_64")
                || arch.startsWith("x86-64")
                || arch.startsWith("x64")) {
            arch = "amd64";
        } else if (arch.contains("aarch")) {
            arch = "aarch64";
        } else if (arch.contains("arm")) {
            arch = "arm";
        }
        return arch;
    }

    /**
     * 获取dll配置文件路径
     *
     * @return String
     */
    private static String getPropertiesPath() {
        return getPrefix() + PROPERTIES_FILE_NAME;
    }



    /**
     * 拷贝依赖库到临时目录
     * @param libPath
     * @return
     * @throws IOException
     */
    private static File extractLibrary(String libPath) throws IOException {
        String resourcePath = libPath;
        try (InputStream in = NativeLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException(resourcePath);
            Path path = Paths.get(resourcePath);
            String fileName = path.getFileName().toString();
            Path targetPath = seetaface6NativePath.resolve(fileName);
            if (Files.exists(targetPath)) {
                //log.debug("target file already exists, skip copy: {}", targetPath.toAbsolutePath());
            } else {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                log.debug("copy target path success: {}", targetPath.toAbsolutePath());
                // 设置可执行权限
                if (!SystemUtil.getOsInfo().getName().toLowerCase().contains("win")) {
                    targetPath.toFile().setExecutable(true);
                }
            }
            return targetPath.toFile();
        }
    }


    /**
     * 将获得的配置进行排序 并生成路径
     *
     * @param list
     * @return List<String>
     */
    private static List<String> getSortedPath(List<DllItem> list) {
        return list.stream().sorted(Comparator.comparing(dllItem -> {
            int i = dllItem.getKey().lastIndexOf(".") + 1;
            String substring = dllItem.getKey().substring(i);
            return Integer.valueOf(substring);
        })).map(DllItem::getValue).collect(Collectors.toList());
    }

}
