package cn.smartjavaai.zeroshot.exception;

/**
 * 零样本目标检测异常
 * @author dwj
 */
public class ZeroDetException extends RuntimeException{

    public ZeroDetException() {
        super();
    }

    public ZeroDetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ZeroDetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZeroDetException(String message) {
        super(message);
    }

    public ZeroDetException(Throwable cause) {
        super(cause);
    }

}
