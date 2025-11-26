package cn.smartjavaai.face.vector.exception;


/**
 * 向量数据库异常
 * @author smartjavaai
 */
public class VectorDBException extends RuntimeException {

    /**
     * 构造函数
     * @param message 异常信息
     */
    public VectorDBException(String message) {
        super(message);
    }

    /**
     * 构造函数
     * @param message 异常信息
     * @param cause 原始异常
     */
    public VectorDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
