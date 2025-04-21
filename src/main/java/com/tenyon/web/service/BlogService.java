package com.tenyon.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenyon.web.domain.entity.Blog;
import com.tenyon.web.domain.vo.blog.BlogVO;

import java.util.List;

/**
 * 博客服务
 *
 * @author tenyon
 * @date 2025/4/18
 */
public interface BlogService extends IService<Blog> {

    /**
     * 根据id获取博客视图
     *
     * @param blogId 博客id
     * @return BlogVO
     */
    BlogVO getBlogVOById(long blogId);

    /**
     * 获取用户博客视图信息
     *
     * @param blog 博客
     * @return BlogVO
     */
    BlogVO getBlogVO(Blog blog);

    /**
     * 获取用户博客视图列表
     *
     * @param blogList
     * @return
     */
    List<BlogVO> getBlogVOList(List<Blog> blogList);
}