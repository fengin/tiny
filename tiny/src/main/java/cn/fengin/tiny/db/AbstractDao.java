package cn.fengin.tiny.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 抽象DAO实现
 * 提供通用的数据库操作实现
 *
 * @author fengin
 * @since 1.0.0
 */
public abstract class AbstractDao<T, ID> implements BaseDao<T, ID> {

    protected abstract T mapRow(ResultSet rs) throws SQLException;
    
    /**
     * 执行更新SQL
     */
    protected void execute(String sql) {
        DbUtil.update(sql, new Object[]{});
    }
    
    /**
     * 执行更新SQL
     */
    protected void update(String sql, Object... params) {
         DbUtil.update(sql, params);
    }
    
    /**
     * 查询对象列表
     */
    protected List<T> queryForList(String sql, Object... params) {
        return DbUtil.query(sql, params, rs -> {
            try {
                return mapRow(rs);
            } catch (SQLException e) {
                throw new RuntimeException("Error mapping row", e);
            }
        });
    }
    
    /**
     * 查询单个对象
     */
    protected Optional<T> queryForObject(String sql, Object... params) {
        List<T> results = queryForList(sql, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 查询long值
     */
    protected long queryForLong(String sql, Object... params) {
        List<Long> result = DbUtil.query(sql, params,
            rs -> {
                try {
                    return rs.getLong(1);
                } catch (SQLException e) {
                    throw new RuntimeException("Error getting long value", e);
                }
            }
        );
        return result.isEmpty() ? 0L : result.get(0);
    }
    
    @Override
    public void save(T entity) {
        DbUtil.update(getInsertSql(), getInsertParams(entity));
    }
    
    @Override
    public boolean update(T entity) {
        return DbUtil.update(getUpdateSql(), getUpdateParams(entity))>0;
    }
    
    @Override
    public void deleteById(ID id) {
        DbUtil.update(getDeleteSql(), new Object[]{id});
    }
    
    @Override
    public Optional<T> findById(ID id) {
        List<T> results = DbUtil.query(getSelectByIdSql(), 
            new Object[]{id}, 
            rs -> {
                try {
                    return mapRow(rs);
                } catch (SQLException e) {
                    throw new RuntimeException("Error mapping row", e);
                }
            }
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Override
    public List<T> findAll() {
        return DbUtil.query(getSelectAllSql(), null, rs -> {
            try {
                return mapRow(rs);
            } catch (SQLException e) {
                throw new RuntimeException("Error mapping row", e);
            }
        });
    }
    
    @Override
    public long count() {
        List<Long> result = DbUtil.query(getCountSql(), null,
           rs -> {
                try {
                    return rs.getLong(1);
                } catch (SQLException e) {
                    throw new RuntimeException("Error getting count", e);
                }
            }
        );
        return result.get(0);
    }
    
    // 以下方法可以由子类覆盖实现
    protected String getInsertSql() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected String getUpdateSql() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected String getDeleteSql() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected String getSelectByIdSql() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected String getSelectAllSql() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected String getCountSql() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected Object[] getInsertParams(T entity) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    protected Object[] getUpdateParams(T entity) {
        throw new UnsupportedOperationException("Not implemented");
    }
} 