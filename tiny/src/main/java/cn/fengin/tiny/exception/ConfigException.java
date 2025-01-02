package cn.fengin.tiny.exception;

/**
 * 配置相关异常
 *
 * @author fengin
 * @since 1.0.0
 */
public class ConfigException extends TinyException {
    public static final int ERROR_CODE = 1000;

    public ConfigException(String message) {
        super(ERROR_CODE, message);
    }

    public ConfigException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
} 