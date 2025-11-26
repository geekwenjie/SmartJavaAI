package cn.smartjavaai.pose.exception;

/**
 * 动作检测异常
 * @author dwj
 */
public class PoseException extends RuntimeException{

    public PoseException() {
        super();
    }

    public PoseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PoseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PoseException(String message) {
        super(message);
    }

    public PoseException(Throwable cause) {
        super(cause);
    }

}
