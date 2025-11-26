package cn.smartjavaai.objectdetection.exception;

/**
 * 目标检测异常
 * @author dwj
 * @date 2025/4/4
 */
public class DetectionException extends RuntimeException{

    public DetectionException() {
        super();
    }

    public DetectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DetectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DetectionException(String message) {
        super(message);
    }

    public DetectionException(Throwable cause) {
        super(cause);
    }

}
