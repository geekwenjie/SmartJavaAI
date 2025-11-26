package cn.smartjavaai.clip.exception;

/**
 * CLIP异常
 * @author dwj
 */
public class ClipException extends RuntimeException{

    public ClipException() {
        super();
    }

    public ClipException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ClipException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClipException(String message) {
        super(message);
    }

    public ClipException(Throwable cause) {
        super(cause);
    }

}
