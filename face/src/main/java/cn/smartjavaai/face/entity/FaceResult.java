package cn.smartjavaai.face.entity;

import lombok.Data;

/**
 * 人脸查询结果
 * @author dwj
 */
@Data
public class FaceResult {

    private String key;
    private float similar;

    public FaceResult() {
    }

    public FaceResult(String key, float similar) {
        this.key = key;
        this.similar = similar;
    }
}
