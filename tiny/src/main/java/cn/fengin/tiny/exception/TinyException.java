package cn.fengin.tiny.exception;

/**
 * 框架基础异常类
 *
 * @author fengin
 * @since 1.0.0
 */
public class TinyException extends RuntimeException {
    private final int code;
    private final String message;

    public TinyException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public TinyException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
} 