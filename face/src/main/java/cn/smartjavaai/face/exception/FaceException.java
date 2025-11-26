package cn.smartjavaai.face.exception;

/**
 * 人脸检测异常
 * @author dwj
 * @date 2025/4/4
 */
public class FaceException extends RuntimeException{

    public FaceException() {
        super();
    }

    public FaceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FaceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FaceException(String message) {
        super(message);
    }

    public FaceException(Throwable cause) {
        super(cause);
    }

}
