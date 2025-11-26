package cn.smartjavaai.instanceseg.exception;

/**
 * 实例分割异常
 * @author dwj
 */
public class InstanceSegException extends RuntimeException{

    public InstanceSegException() {
        super();
    }

    public InstanceSegException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InstanceSegException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstanceSegException(String message) {
        super(message);
    }

    public InstanceSegException(Throwable cause) {
        super(cause);
    }

}
