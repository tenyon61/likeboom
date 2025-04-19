package com.tenyon.lb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenyon.lb.common.constant.UserConstant;
import com.tenyon.lb.domain.entity.Blog;
import com.tenyon.lb.domain.entity.Thumb;
import com.tenyon.lb.domain.entity.User;
import com.tenyon.lb.domain.vo.blog.BlogVO;
import com.tenyon.lb.mapper.BlogMapper;
import com.tenyon.lb.service.BlogService;
import com.tenyon.lb.service.ThumbService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 博客服务实现
 *
 * @author tenyon
 * @date 2025/4/18
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Resource
    @Lazy
    private ThumbService thumbService;

    @Override
    public BlogVO getBlogVOById(long blogId) {
        Blog blog = this.getById(blogId);
        return getBlogVO(blog);
    }

    @Override
    public BlogVO getBlogVO(Blog blog) {
        BlogVO blogVO = new BlogVO();
        BeanUtils.copyProperties(blog, blogVO);
        if (!StpUtil.isLogin()) {
            return blogVO;
        }
        User user = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, user.getId())
                .eq(Thumb::getBlogId, blog.getId())
                .one();
        blogVO.setHasThumb(thumb != null);
        return blogVO;
    }

    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList) {
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (StpUtil.isLogin()) {
            User user = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
            Set<Long> blogIdSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
            // 获取点赞
            List<Thumb> thumbList = thumbService.lambdaQuery()
                    .eq(Thumb::getUserId, user.getId())
                    .in(Thumb::getBlogId, blogIdSet)
                    .list();

            thumbList.forEach(blogThumb -> blogIdHasThumbMap.put(blogThumb.getBlogId(), true));
        }

        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return blogVO;
                })
                .toList();
    }

}
