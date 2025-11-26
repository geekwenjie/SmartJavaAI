package cn.smartjavaai.ocr.entity;

import lombok.Data;

/**
 * @author dwj
 */
@Data
public class PlateResult {

    /**
     * 车牌号码
     */
    private String plateNo;

    /**
     * 车牌颜色
     */
    private String plateColor;

    public PlateResult(String plateNo, String plateColor) {
        this.plateNo = plateNo;
        this.plateColor = plateColor;
    }


    @Override
    public String toString() {
        return "PlateResult{" +
                "plateNo='" + plateNo + '\'' +
                ", plateColor='" + plateColor + '\'' +
                '}';
    }
}

