package com.tenyon.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenyon.web.common.constant.RedisConstant;
import com.tenyon.web.common.constant.UserConstant;
import com.tenyon.web.domain.entity.Blog;
import com.tenyon.web.domain.entity.User;
import com.tenyon.web.domain.vo.blog.BlogVO;
import com.tenyon.web.mapper.BlogMapper;
import com.tenyon.web.service.BlogService;
import com.tenyon.web.service.ThumbService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
        Boolean exist = thumbService.hasThumb(blog.getId(), user.getId());
        blogVO.setHasThumb(exist);
        return blogVO;
    }

    @Override
    public List<BlogVO> getBlogVOList(List<Blog> blogList) {
        List<Object> blogIds = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
        Map<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (StpUtil.isLogin()) {
            User user = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
            List<Object> thumbList =
                    redisTemplate.opsForHash().multiGet(RedisConstant.USER_THUMB_KEY_PREFIX + user.getId().toString(), blogIds);
            for (int i = 0; i < thumbList.size(); i++) {
                if (thumbList.get(i) != null) {
                    blogIdHasThumbMap.put(Long.valueOf(blogIds.get(i).toString()), true);
                }
            }
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
