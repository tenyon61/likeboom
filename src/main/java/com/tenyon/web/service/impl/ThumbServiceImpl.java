package com.tenyon.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenyon.web.common.constant.RedisConstant;
import com.tenyon.web.common.exception.BusinessException;
import com.tenyon.web.common.exception.ErrorCode;
import com.tenyon.web.common.utils.RedisUtils;
import com.tenyon.web.domain.dto.thumb.DoThumbDTO;
import com.tenyon.web.domain.entity.Blog;
import com.tenyon.web.domain.entity.Thumb;
import com.tenyon.web.domain.entity.User;
import com.tenyon.web.manager.cache.CacheManager;
import com.tenyon.web.mapper.ThumbMapper;
import com.tenyon.web.service.BlogService;
import com.tenyon.web.service.ThumbService;
import com.tenyon.web.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 点赞服务
 *
 * @author tenyon
 * @date 2025/4/18
 */
@Slf4j
@RequiredArgsConstructor
@Service("thumbServiceLocalCache")
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final CacheManager cacheManager;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbDTO doThumbDTO) {
        if (doThumbDTO == null || doThumbDTO.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbDTO.getBlogId();
                boolean exists = hasThumb(blogId, loginUser.getId());
                if (exists) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                // 更新成功才执行
                boolean success = update && this.save(thumb);
                if (success) {
                    String hasKey = RedisUtils.getUserThumbKey(loginUser.getId());
                    String fieldKey = blogId.toString();
                    Long realThumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hasKey, fieldKey, realThumbId);
                    cacheManager.putIfPresent(hasKey, fieldKey, realThumbId);
                }
                return true;
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbDTO doThumbDTO) {
        if (doThumbDTO == null || doThumbDTO.getBlogId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务 key={thumb:userid} field={blogid} value={hotThumb}
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbDTO.getBlogId();
                // 判断是否点赞
                Object thumbIdObj = cacheManager.get(RedisUtils.getUserThumbKey(loginUser.getId()), blogId.toString());
                if (thumbIdObj == null || thumbIdObj.equals(RedisConstant.UN_THUMB_CONSTANT)) {
                    throw new RuntimeException("用户未点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                boolean success = update && this.removeById((Long) thumbIdObj);
                if (success) {
                    String hasKey = RedisUtils.getUserThumbKey(loginUser.getId());
                    String fieldKey = blogId.toString();
                    redisTemplate.opsForHash().delete(hasKey, fieldKey);
                    cacheManager.putIfPresent(hasKey, fieldKey, RedisConstant.UN_THUMB_CONSTANT);
                }
                return success;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        Object thumbIdObj = cacheManager.get(RedisConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = (Long) thumbIdObj;
        return !thumbId.equals(RedisConstant.UN_THUMB_CONSTANT);
    }

}
