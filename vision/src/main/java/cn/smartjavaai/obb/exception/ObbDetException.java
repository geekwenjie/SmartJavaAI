package cn.smartjavaai.obb.exception;

/**
 * 旋转框检测异常
 * @author dwj
 */
public class ObbDetException extends RuntimeException{

    public ObbDetException() {
        super();
    }

    public ObbDetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ObbDetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObbDetException(String message) {
        super(message);
    }

    public ObbDetException(Throwable cause) {
        super(cause);
    }

}
