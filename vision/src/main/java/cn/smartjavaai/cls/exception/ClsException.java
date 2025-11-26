package cn.smartjavaai.cls.exception;

/**
 * 分类模型异常
 * @author dwj
 */
public class ClsException extends RuntimeException{

    public ClsException() {
        super();
    }

    public ClsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ClsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClsException(String message) {
        super(message);
    }

    public ClsException(Throwable cause) {
        super(cause);
    }

}
