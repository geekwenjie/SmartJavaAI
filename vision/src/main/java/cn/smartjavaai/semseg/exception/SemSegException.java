package cn.smartjavaai.semseg.exception;

/**
 * 语义分割异常
 * @author dwj
 */
public class SemSegException extends RuntimeException{

    public SemSegException() {
        super();
    }

    public SemSegException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SemSegException(String message, Throwable cause) {
        super(message, cause);
    }

    public SemSegException(String message) {
        super(message);
    }

    public SemSegException(Throwable cause) {
        super(cause);
    }

}
