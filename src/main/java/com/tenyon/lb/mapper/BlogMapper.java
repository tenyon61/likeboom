package com.tenyon.lb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenyon.lb.domain.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 博客操作
 *
 * @author tenyon
 * @date 2025/4/18
 */
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);

}