package cn.smartjavaai.face.utils;

import cn.smartjavaai.common.entity.DetectionInfo;
import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.Point;
import cn.smartjavaai.common.entity.face.FaceAttribute;
import cn.smartjavaai.common.entity.face.FaceInfo;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.face.EyeStatus;
import cn.smartjavaai.common.enums.face.GenderType;
import cn.smartjavaai.common.enums.face.LivenessStatus;
import cn.smartjavaai.face.enums.QualityGrade;
import com.seeta.sdk.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Seetaface6工具类
 * @author dwj
 * @date 2025/6/24
 */
public class Seetaface6Utils {


    /**
     * 子图中人脸关键点坐标 - Coordinates of key points in the image
     *
     * @param pointFS
     * @return
     */
    public static double[][] facePoints(SeetaPointF[] pointFS) {
        //      图中关键点坐标 - Coordinates of key points in the image
        //      1.  left_eye_x , left_eye_y
        //      2.  right_eye_x , right_eye_y
        //      3.  nose_x , nose_y
        //      4.  left_mouth_x , left_mouth_y
        //      5.  right_mouth_x , right_mouth_y
        double[][] pointsArray = new double[5][2]; // 保存人脸关键点 - Save facial key points
        int i = 0;
        for (SeetaPointF point : pointFS) {
            pointsArray[i][0] = point.getX();
            pointsArray[i][1] = point.getY();
            i++;
        }
        return pointsArray;
    }

    /**
     * bgr转图片
     * @return 图片
     */
    public static BufferedImage toBufferedImage(SeetaImageData seetaImageData) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(seetaImageData.width, seetaImageData.height, type);
        image.getRaster().setDataElements(0, 0, seetaImageData.width, seetaImageData.height, seetaImageData.data);
        return image;
    }


    /**
     * 将DetectionRectangle转换为SeetaRect
     * @param detectionRectangle
     * @return
     */
    public static SeetaRect convertToSeetaRect(DetectionRectangle detectionRectangle){
        SeetaRect seetaRect = new SeetaRect();
        seetaRect.x = detectionRectangle.getX();
        seetaRect.y = detectionRectangle.getY();
        seetaRect.width = detectionRectangle.getWidth();
        seetaRect.height = detectionRectangle.getHeight();
        return seetaRect;
    }

    /**
     * 将PointList转换为SeetaPointF[]
     * @param pointList
     * @return
     */
    public static SeetaPointF[] convertToSeetaPointF(List<Point> pointList){
        return pointList.stream()
                .map(p -> {
                    SeetaPointF sp = new SeetaPointF();
                    sp.x = p.getX();
                    sp.y = p.getY();
                    return sp;
                })
                .toArray(SeetaPointF[]::new);
    }

    /**
     * 将SeetaAntiSpoofing.Status转换为LivenessStatus
     * @param status
     * @return
     */
    public static LivenessStatus convertToLivenessStatus(FaceAntiSpoofing.Status status){
        if(status == null){
            return LivenessStatus.UNKNOWN;
        }
        switch (status) {
            case REAL:
                return LivenessStatus.LIVE;
            case SPOOF:
                return LivenessStatus.NON_LIVE;
            case FUZZY:
                return LivenessStatus.UNKNOWN;
            case DETECTING:
                return LivenessStatus.DETECTING;
            default:
                return LivenessStatus.UNKNOWN;  // 默认返回未知
        }
    }


    /**
     * 转为GenderType
     * @param gender
     * @return
     */
    public static GenderType convertToGenderType(GenderPredictor.GENDER gender){
        if(gender == null){
            return GenderType.UNKNOWN;
        }
        switch (gender) {
            case MALE:
                return GenderType.MALE;
            case FEMALE:
                return GenderType.FEMALE;
            default:
                return GenderType.UNKNOWN;  // 默认返回未知
        }
    }

    /**
     * 转为EyeStatus
     * @param eyeState
     * @return
     */
    public static EyeStatus convertToEyeStatus(EyeStateDetector.EYE_STATE eyeState){
        if(eyeState == null){
            return EyeStatus.UNKNOWN;
        }
        switch (eyeState) {
            case EYE_OPEN:
                return EyeStatus.OPEN;
            case EYE_CLOSE:
                return EyeStatus.CLOSED;
            case EYE_RANDOM:
                return EyeStatus.NON_EYE_REGION;
            default:
                return EyeStatus.UNKNOWN;  // 默认返回未知
        }
    }

    public static DetectionResponse convertToFaceAttributeResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList, List<FaceAttribute> faceAttributeList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            FaceInfo faceInfo = new FaceInfo();
            if(seetaPointFSList != null && seetaPointFSList.size() > 0){
                SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
                List<Point> keyPoints = Arrays.stream(seetaPointFS)
                        .map(p -> new Point(p.x, p.y))
                        .collect(Collectors.toList());
                faceInfo.setKeyPoints(keyPoints);
            }
            if(faceAttributeList != null && faceAttributeList.size() > 0){
                faceInfo.setFaceAttribute(faceAttributeList.get(i));
            }
            detectionInfoList.add(new DetectionInfo(rectangle, 0, faceInfo));
        }
        return new DetectionResponse(detectionInfoList);
    }

    /**
     * 转换为FaceDetectedResult
     * @param seetaResult
     * @return
     */
    public static DetectionResponse convertToDetectionResponse(SeetaRect[] seetaResult, List<SeetaPointF[]> seetaPointFSList, List<LivenessStatus> livenessStatusList){
        if(Objects.isNull(seetaResult) || seetaResult.length == 0){
            return null;
        }
        DetectionResponse detectionResponse = new DetectionResponse();
        List<DetectionInfo> detectionInfoList = new ArrayList<DetectionInfo>();
        for(int i = 0; i < seetaResult.length; i++){
            SeetaRect rect  = seetaResult[i];
            SeetaPointF[] seetaPointFS = seetaPointFSList.get(i);
            //过滤置信度
            /*if(config.getConfidenceThreshold() > 0){
                continue;
            }*/
            DetectionRectangle rectangle = new DetectionRectangle(rect.x, rect.y, rect.width, rect.height);
            List<Point> keyPoints = Arrays.stream(seetaPointFS)
                    .map(p -> new Point(p.x, p.y))
                    .collect(Collectors.toList());
            FaceInfo faceInfo = new FaceInfo(keyPoints);
            faceInfo.setLivenessStatus(new LivenessResult(livenessStatusList.get(i)));
            DetectionInfo detectionInfo = new DetectionInfo(rectangle, 0, faceInfo);
            detectionInfoList.add(detectionInfo);
        }
        detectionResponse.setDetectionInfoList(detectionInfoList);
        return detectionResponse;
    }

}
