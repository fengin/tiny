package cn.fengin.tiny.db;

import cn.fengin.tiny.config.DatabaseConfig;
import cn.fengin.tiny.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 数据库连接池
 * 管理SQLite连接的创建、获取和释放
 *
 * @author fengin
 * @since 1.0.0
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    
    private static final int MAX_WAIT_MILLIS = 5000;
    private final BlockingQueue<Connection> pool;
    private final String jdbcUrl;
    private final int poolSize;
    
    private static volatile ConnectionPool instance;
    
    private ConnectionPool() {
        DatabaseConfig dbConfig = ApplicationContext.getInstance().getDatabaseConfig();
        DatabaseConfig.SqliteConfig config = dbConfig.getSqlite();
        this.jdbcUrl = "jdbc:sqlite:" + config.getPath();
        this.poolSize = config.getPoolSize();
        this.pool = new ArrayBlockingQueue<>(poolSize);
        initPool();
    }
    
    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }
    
    private void initPool() {
        try {
            Class.forName("org.sqlite.JDBC");
            for (int i = 0; i < poolSize; i++) {
                Connection conn = createConnection();
                if (conn != null) {
                    pool.offer(conn);
                }
            }
            logger.info("Connection pool initialized with {} connections", pool.size());
        } catch (Exception e) {
            logger.error("Failed to initialize connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }
    
    private Connection createConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl);
        } catch (Exception e) {
            logger.error("Failed to create database connection", e);
            return null;
        }
    }
    
    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws Exception {
        Connection conn = pool.poll(MAX_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        if (conn == null) {
            logger.warn("Connection pool exhausted, creating new connection");
            conn = createConnection();
        }
        return conn;
    }
    
    /**
     * 释放数据库连接
     */
    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    if (!pool.offer(conn)) {
                        conn.close();
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to release connection", e);
            }
        }
    }
    
    /**
     * 关闭连接池
     */
    public void shutdown() {
        Connection conn;
        while ((conn = pool.poll()) != null) {
            try {
                conn.close();
            } catch (Exception e) {
                logger.error("Failed to close connection", e);
            }
        }
        logger.info("Connection pool shut down");
    }
} 