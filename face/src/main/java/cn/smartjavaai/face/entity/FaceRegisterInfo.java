package cn.smartjavaai.face.entity;

import lombok.Data;

/**
 * 人脸注册信息
 * @author dwj
 * @date 2025/5/29
 */
@Data
public class FaceRegisterInfo {

    /**
     * 向量ID
     */
    private String id;

    /**
     * 元数据，可以存储人脸相关的其他信息（JSON格式）
     */
    private String metadata;

    public FaceRegisterInfo(String id, String metadata) {
        this.id = id;
        this.metadata = metadata;
    }

    public FaceRegisterInfo() {
    }
}
