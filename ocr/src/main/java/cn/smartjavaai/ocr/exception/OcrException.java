package cn.smartjavaai.ocr.exception;

/**
 * OCR异常
 * @author dwj
 * @date 2025/4/4
 */
public class OcrException extends RuntimeException{

    public OcrException() {
        super();
    }

    public OcrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }

    public OcrException(String message) {
        super(message);
    }

    public OcrException(Throwable cause) {
        super(cause);
    }

}
