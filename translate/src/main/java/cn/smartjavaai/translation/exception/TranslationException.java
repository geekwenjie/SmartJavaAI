package cn.smartjavaai.translation.exception;

/**
 * 翻译异常
 * @author lwx
 * @date 2025/6/5
 */
public class TranslationException extends RuntimeException{

    public TranslationException() {
        super();
    }

    public TranslationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(Throwable cause) {
        super(cause);
    }

}
