package cn.fengin.tiny.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 数据库操作工具类
 * 提供通用的数据库操作方法
 *
 * @author fengin
 * @since 1.0.0
 */
public class DbUtil {
    private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);
    
    /**
     * 执行查询操作
     *
     * @param sql SQL语句
     * @param params 参数
     * @param mapper 结果映射函数
     * @return 查询结果列表
     */
    public static <T> List<T> query(String sql, Object[] params, Function<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                results.add(mapper.apply(rs));
            }
        } catch (Exception e) {
            logger.error("Query failed: {}", sql, e);
            throw new RuntimeException("Query failed", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return results;
    }
    
    /**
     * 执行更新操作
     *
     * @param sql SQL语句
     * @param params 参数
     * @return 影响的行数
     */
    public static int update(String sql, Object[] params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = ConnectionPool.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (Exception e) {
            logger.error("Update failed: {}", sql, e);
            throw new RuntimeException("Update failed", e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    /**
     * 执行事务
     *
     * @param action 事务操作
     */
    public static void executeTransaction(TransactionAction action) {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            action.execute(conn);
            
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Transaction rollback failed", ex);
                }
            }
            logger.error("Transaction failed", e);
            throw new RuntimeException("Transaction failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Failed to reset auto-commit", e);
                }
                ConnectionPool.getInstance().releaseConnection(conn);
            }
        }
    }
    
    private static void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
    
    private static void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("Failed to close ResultSet", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Failed to close PreparedStatement", e);
            }
        }
        if (conn != null) {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }
    
    /**
     * 事务操作接口
     */
    @FunctionalInterface
    public interface TransactionAction {
        void execute(Connection conn) throws SQLException;
    }
} 