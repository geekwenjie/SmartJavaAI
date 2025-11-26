package cn.smartjavaai.face.utils;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import cn.smartjavaai.common.utils.BufferedImageUtils;
import cn.smartjavaai.common.utils.ImageUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import com.seeta.sdk.SeetaImageData;
import com.seeta.sdk.SeetaPointF;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;

/**
 * 人脸对齐
 * @author Calvin
 */
public class FaceAlignUtils {
    /**
     * 根据目标点，进行旋转仿射变换
     * Perform rotation and affine transformation based on the target 5 points
     *
     * @param src
     * @param rot_mat
     * @return
     */
    public static Mat warpAffine(Mat src, Mat rot_mat) {
        Mat rot = new Mat();
        // 进行仿射变换，变换后大小为src的大小
        // Perform affine transformation, the size after transformation is the same as the size of src
        Scalar scalar = new Scalar(135, 133, 132);
        Size size = new Size(512, 512);
        Imgproc.warpAffine(src, rot, rot_mat, size, 0, 0, scalar);
        return rot;
    }
    public static Mat warpAffine(Mat src, Mat rot_mat, int width, int height) {
        Mat rot = new Mat();
        Size size = new Size(width, height);
        Scalar scalar = new Scalar(135, 133, 132);
        Imgproc.warpAffine(src, rot, rot_mat, size,0, 0, scalar);
        return rot;
    }


    public static Mat warpAffine(Mat src, Mat rot_mat, int width, int height, int flags) {
        Mat rot = new Mat();
        Size size = new Size(width, height);
        Imgproc.warpAffine(src, rot, rot_mat, size, flags);
        return rot;
    }

    public static SeetaImageData faceAlign(BufferedImage sourceImage, SeetaPointF[] pointFS) {
        NDManager manager = NDManager.newBaseManager();
        //获取子图中人脸关键点坐标
        double[][] pointsArray = Seetaface6Utils.facePoints(pointFS);
        NDArray srcPoints = manager.create(pointsArray);
        NDArray dstPoints = FaceUtils.faceTemplate512x512(manager);
        // 5点仿射变换
        Mat affine_matrix = OpenCVUtils.toOpenCVMat(manager, srcPoints, dstPoints);
        Mat mat = FaceAlignUtils.warpAffine(OpenCVUtils.image2Mat(sourceImage), affine_matrix);
        BufferedImage alignImage = OpenCVUtils.mat2Image(mat);
        SeetaImageData imageData = new SeetaImageData(alignImage.getWidth(), alignImage.getHeight(), 3);
        imageData.data = BufferedImageUtils.getMatrixBGR(alignImage);
        return imageData;
    }
}
