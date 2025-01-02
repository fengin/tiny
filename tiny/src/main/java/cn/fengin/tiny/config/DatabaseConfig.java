package cn.fengin.tiny.config;

import lombok.Data;

/**
 * 数据库配置类
 * 采用组合模式设计配置结构，支持多种数据库配置的扩展
 *
 * @author fengin
 * @since 1.0.0
 */
@Data
public class DatabaseConfig {
    private SqliteConfig sqlite = new SqliteConfig();

    /**
     * SQLite数据库配置
     * 参数说明：
     * - path: 数据库文件路径，默认为当前目录下的data.db
     * - poolSize: 数据库连接池大小，默认10
     *   (考虑到SQLite是本地文件数据库，通常不需要太大的连接池)
     */
    @Data
    public static class SqliteConfig {
        private String path = "./data.db";
        private int poolSize = 10;
    }
} 