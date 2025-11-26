package cn.smartjavaai.common.entity;

import lombok.Data;

import java.util.Arrays;
import java.util.Optional;

/**
 * 通用响应封装类，用于统一接口返回结构
 * @author dwj
 * @date 2025/6/4
 */
@Data
public class R<T> {

    private Integer code;
    private String message;
    private T data;


    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = 0;
        r.message = "成功";
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 0;
        r.message = "成功";
        r.data = data;
        return r;
    }

    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        r.data = null;
        return r;
    }

    public static <T> R<T> fail(Status status) {
        R<T> r = new R<>();
        r.code = status.code;
        r.message = status.message;
        r.data = null;
        return r;
    }


    public enum Status {
        SUCCESS(0, "成功"),
        INVALID_IMAGE(1, "图像无效"),
        FILE_NOT_FOUND(2, "文件不存在"),
        NO_FACE_DETECTED(3, "未检测到人脸"),
        PARAM_ERROR(4, "参数错误"),
        INVALID_VIDEO(5, "视频无效"),
        NO_OBJECT_DETECTED(6, "未检测到目标"),
        Unknown(-1,  "未知错误");

        private final int code;
        private final String message;



        Status(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public static Status valueOf(int val) {
            Optional<Status> search = Arrays.stream(values()).filter((status) -> {
                return status.code == val;
            }).findFirst();
            return (Status)search.orElse(Unknown);
        }
    }

    public boolean isSuccess() {
        return code != null && code.equals(Status.SUCCESS.code);
    }

}
