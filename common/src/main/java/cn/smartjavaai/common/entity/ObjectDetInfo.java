package cn.smartjavaai.common.entity;

import lombok.Data;

/**
 * 目标检测信息
 * @author dwj
 * @date 2025/5/7
 */
@Data
public class ObjectDetInfo {

    private String className;

    public ObjectDetInfo() {
    }

    public ObjectDetInfo(String className) {
        this.className = className;
    }
}
