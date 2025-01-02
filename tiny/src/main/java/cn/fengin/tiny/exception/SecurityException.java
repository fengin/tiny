package cn.fengin.tiny.exception;

/**
 * 安全相关异常
 *
 * @author fengin
 * @since 1.0.0
 */
public class SecurityException extends TinyException {
    public static final int ERROR_CODE = 3000;

    public SecurityException(String message) {
        super(ERROR_CODE, message);
    }

    public SecurityException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
} 