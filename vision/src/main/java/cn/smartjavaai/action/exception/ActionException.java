package cn.smartjavaai.action.exception;

/**
 * 动作检测异常
 * @author dwj
 */
public class ActionException extends RuntimeException{

    public ActionException() {
        super();
    }

    public ActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionException(String message) {
        super(message);
    }

    public ActionException(Throwable cause) {
        super(cause);
    }

}
