package cn.smartjavaai.common.utils;

import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Point;
import ai.djl.ndarray.NDArray;
import cn.smartjavaai.common.entity.R;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author dwj
 */
public class DJLCommonUtils {

    private static final List<String> SUPPORTED_PROTOCOLS = Arrays.asList(
            "file://",
            "http://",
            "https://",
            "jar://",
            "djl://",
            "s3://",
            "hdfs://"
    );

    /**
     * 检查模型目录中是否存在 "serving.properties" 文件
     *
     * @param modelPath 模型目录路径
     * @return true 表示存在，false 表示不存在
     */
    public static boolean isServingPropertiesExists(Path modelPath) {
        if (modelPath == null || !Files.exists(modelPath)) {
            return false;
        }
        // 确定目录路径
        Path dirPath = Files.isDirectory(modelPath) ? modelPath : modelPath.getParent();
        if (dirPath == null) {
            return false; // 可能是根目录的文件
        }

        // 判断目录下的 serving.properties 是否存在
        Path servingFile = dirPath.resolve("serving.properties");
        return Files.exists(servingFile);
    }

    /**
     * 判断 NDArray 是否为空
     * @param ndArray
     * @return
     */
    public static boolean isNDArrayEmpty(NDArray ndArray){
        return Objects.isNull(ndArray) || ndArray.size() == 0;
    }


    /**
     * float NDArray To float[][] Array
     * @param ndArray
     * @return
     */
    public static float[][] floatNDArrayToArray(NDArray ndArray) {
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        float[][] arr = new float[rows][cols];

        float[] arrs = ndArray.toFloatArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                arr[i][j] = arrs[i * cols + j];
            }
        }
        return arr;
    }


    /**
     * float NDArray To float[][] Array
     * @param ndArray
     * @param cvType
     * @return
     */
    public static Mat floatNDArrayToMat(NDArray ndArray, int cvType) {
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        Mat mat = new Mat(rows, cols, cvType);

        float[] arrs = ndArray.toFloatArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arrs[i * cols + j]);
            }
        }
        return mat;
    }

    /**
     * float NDArray To Mat
     * @param ndArray
     * @return
     */
    public static Mat floatNDArrayToMat(NDArray ndArray) {
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        Mat mat = new Mat(rows, cols, CvType.CV_32F);

        float[] arrs = ndArray.toFloatArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arrs[i * cols + j]);
            }
        }

        return mat;

    }

    /**
     * uint8 NDArray To Mat
     * @param ndArray
     * @return
     */
    public static Mat uint8NDArrayToMat(NDArray ndArray) {
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        Mat mat = new Mat(rows, cols, CvType.CV_8U);

        byte[] arrs = ndArray.toByteArray();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arrs[i * cols + j]);
            }
        }
        return mat;
    }


    /**
     * List To Mat
     * @param points
     * @return
     */
    public static Mat toMat(List<Point> points) {
        Mat mat = new Mat(points.size(), 2, CvType.CV_32F);
        for (int i = 0; i < points.size(); i++) {
            ai.djl.modality.cv.output.Point point = points.get(i);
            mat.put(i, 0, (float) point.getX());
            mat.put(i, 1, (float) point.getY());
        }
        return mat;
    }

    /**
     * 构建一个空的 DetectedObjects 对象
     * @return
     */
    public static DetectedObjects buildEmptyDetectedObjects(){
        List<String> classNames = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();
        List<BoundingBox> boxes = new ArrayList<>();
        return new DetectedObjects(classNames, probabilities, boxes);
    }

    /**
     * 判断路径是否以已知协议开头
     * @param path 模型路径
     * @return 是否以支持的协议开头
     */
    public static boolean hasSupportedProtocol(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return SUPPORTED_PROTOCOLS.stream().anyMatch(path::startsWith);
    }


}
