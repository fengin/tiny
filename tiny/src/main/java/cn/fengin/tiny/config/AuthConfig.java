package cn.fengin.tiny.config;

import lombok.Data;

/**
 * 认证配置类
 * 包含用户认证和系统间调用认证的配置
 *
 * @author fengin
 * @since 1.0.0
 */
@Data
public class AuthConfig {
    private UserAuthConfig userAuthConfig = new UserAuthConfig();
    private ApiAuthConfig apiAuthConfig = new ApiAuthConfig();

    /**
     * 用户认证配置
     * 参数说明：
     * - username: 管理员用户名
     * - password: 管理员密码
     * - jwtSecret: JWT密钥
     * - tokenExpireHours: Token过期时间（小时）
     */
    @Data
    public static class UserAuthConfig {
        private String username = "admin";
        private String password = "admin123";
        private int tokenExpireHours = 24;
    }

    /**
     * 系统间调用认证配置
     * 参数说明：
     * - apiKey: 系统间调用的API密钥
     */
    @Data
    public static class ApiAuthConfig {
        private String key = "your-system-api-key";
    }
} 