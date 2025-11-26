package cn.smartjavaai.speech.tts.exception;

/**
 * tts异常
 * @author dwj
 */
public class TtsException extends RuntimeException{

    public TtsException() {
        super();
    }

    public TtsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TtsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TtsException(String message) {
        super(message);
    }

    public TtsException(Throwable cause) {
        super(cause);
    }

}
