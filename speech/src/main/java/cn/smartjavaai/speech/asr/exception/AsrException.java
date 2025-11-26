package cn.smartjavaai.speech.asr.exception;

/**
 * 语音识别异常
 * @author dwj
 */
public class AsrException extends RuntimeException{

    public AsrException() {
        super();
    }

    public AsrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AsrException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsrException(String message) {
        super(message);
    }

    public AsrException(Throwable cause) {
        super(cause);
    }

}
