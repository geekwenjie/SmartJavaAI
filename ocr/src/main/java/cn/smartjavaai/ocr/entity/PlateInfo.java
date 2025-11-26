package cn.smartjavaai.ocr.entity;

import cn.smartjavaai.common.entity.DetectionRectangle;
import cn.smartjavaai.ocr.enums.PlateType;
import lombok.Data;

/**
 * 车牌识别信息
 * @author dwj
 */
@Data
public class PlateInfo {

    /**
     * 车牌类型
     */
    private PlateType plateType;

    /**
     * 车牌号码
     */
    private String plateNumber;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 检测位置信息
     */
    private DetectionRectangle detectionRectangle;

    /**
     * 车牌4角坐标
     */
    private OcrBox box;

    /**
     * 检测得分
     */
    private float score;


}
