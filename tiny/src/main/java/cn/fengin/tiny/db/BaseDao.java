package cn.fengin.tiny.db;

import java.util.List;
import java.util.Optional;

/**
 * 基础DAO接口
 * 定义通用的数据库操作方法
 *
 * @author fengin
 * @since 1.0.0
 */
public interface BaseDao<T, ID> {
    /**
     * 保存实体
     */
    void save(T entity);
    
    /**
     * 根据ID更新实体
     */
    boolean update(T entity);
    
    /**
     * 根据ID删除实体
     */
    void deleteById(ID id);
    
    /**
     * 根据ID查询实体
     */
    Optional<T> findById(ID id);
    
    /**
     * 查询所有实体
     */
    List<T> findAll();
    
    /**
     * 统计实体数量
     */
    long count();
} 