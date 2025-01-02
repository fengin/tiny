package cn.fengin.tiny.exception;

/**
 * 安全相关异常
 *
 * @author fengin
 * @since 1.0.0
 */
public class HttpException extends TinyException {
    public static final int ERROR_CODE = 400;

    public HttpException(int httpCode,String message){
        super(httpCode, message);
    }
    public HttpException(int httpCode,String message, Throwable cause) {
        super(httpCode, message, cause);
    }
} 