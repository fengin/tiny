package cn.fengin.tiny.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库初始化工具
 * 用于创建数据库表和初始化数据
 *
 * @author fengin
 * @since 1.0.0
 */
public class DbInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DbInitializer.class);
    
    /**
     * 初始化数据库
     */
    public static void initialize() {
        try {
            // 读取初始化SQL脚本
            List<String> sqlStatements = loadSqlStatements();
            
            // 执行SQL语句
            executeSqlStatements(sqlStatements);
            
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static List<String> loadSqlStatements() throws Exception {
        List<String> sqlStatements = new ArrayList<>();
        try (InputStream is = DbInitializer.class.getResourceAsStream("/db/init.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sb.append(line);
                if (line.endsWith(";")) {
                    sqlStatements.add(sb.toString());
                    sb.setLength(0);
                }
            }
        }
        return sqlStatements;
    }
    
    private static void executeSqlStatements(List<String> sqlStatements) {
        DbUtil.executeTransaction(conn -> {
            try (Statement stmt = conn.createStatement()) {
                for (String sql : sqlStatements) {
                    stmt.execute(sql);
                }
            }
        });
    }
} 