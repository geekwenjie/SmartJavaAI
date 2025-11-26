package cn.smartjavaai.common.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件操作工具类
 * @author dwj
 * @date 2025/4/4
 */
public class FileUtils {

    /**
     * 检查文件是否存在
     * @param filePath
     * @return
     */
    public static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();  // 确保是文件且存在
    }

    /**
     * 检查目录是否存在
     * @param path
     * @return
     */
    public static boolean isValidDirectory(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * 查找指定目录下指定后缀的文件
     *
     * @param dir       目录
     * @param suffix    文件后缀，例如 ".txt"、".wav"
     * @param recursive 是否递归子目录
     * @return 文件列表
     */
    public static List<File> findFilesWithSuffix(File dir, String suffix, boolean recursive) {
        List<File> result = new ArrayList<>();
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return result;
        }
        searchFiles(dir, suffix, recursive, result);
        return result;
    }

    // 搜索方法
    private static void searchFiles(File dir, String suffix, boolean recursive, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                if(file.getName().endsWith(suffix)){
                    result.add(file);
                    continue;
                }
                if (recursive) {
                    searchFiles(file, suffix, true, result);
                }
            } else if (file.isFile() && file.getName().endsWith(suffix)) {
                result.add(file);
            }
        }
    }

    /**
     * 查找指定目录下指定文件名的文件
     *
     * @param dir       目录
     * @param fileName  文件名（精确匹配）
     * @param recursive 是否递归子目录
     * @return 文件列表
     */
    public static List<File> findFilesByName(File dir, String fileName, boolean recursive) {
        List<File> result = new ArrayList<>();
        if (dir == null || !dir.exists() || !dir.isDirectory() || fileName == null) {
            return result;
        }
        searchByName(dir, fileName, recursive, result);
        return result;
    }

    // 递归搜索方法
    private static void searchByName(File dir, String fileName, boolean recursive, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(fileName)){
                    result.add(file);
                    continue;
                }
                if (recursive) {
                    searchByName(file, fileName, true, result);
                }
            } else if (file.isFile() && file.getName().equals(fileName)) {
                result.add(file);
            }
        }
    }

    /**
     * 将文件列表转换为绝对路径字符串
     *
     * @param files 文件列表
     * @return 绝对路径字符串，用逗号分隔
     */
    public static String joinAbsolutePaths(List<File> files) {
        if (files == null || files.isEmpty()) {
            return "";
        }
        return files.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(","));
    }

    /**
     * 在指定目录中查找文件名包含指定关键字的文件，可选指定后缀。
     *
     * @param dirPath     要搜索的目录路径
     * @param keyword     文件名包含的关键字（可为 null）
     * @param extension   文件后缀名（例如 ".wav"，可为 null）
     * @param recursive   是否递归搜索子目录
     * @return 匹配的文件列表
     */
    public static List<File> searchFiles(String dirPath, String keyword, String extension, boolean recursive) {
        List<File> result = new ArrayList<>();
        File dir = new File(dirPath);

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("目录不存在或不是目录：" + dirPath);
            return result;
        }

        File[] files = dir.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory() && recursive) {
                // 递归子目录
                result.addAll(searchFiles(file.getAbsolutePath(), keyword, extension, true));
            } else if (file.isFile()) {
                String name = file.getName().toLowerCase();
                boolean matchKeyword = (keyword == null || name.contains(keyword.toLowerCase()));
                boolean matchExt = (extension == null || name.endsWith(extension.toLowerCase()));

                if (matchKeyword && matchExt) {
                    result.add(file);
                }
            }
        }
        return result;
    }

}
