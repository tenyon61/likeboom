package com.tenyon.lb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenyon.lb.common.constant.RedisConstant;
import com.tenyon.lb.common.exception.BusinessException;
import com.tenyon.lb.common.exception.ErrorCode;
import com.tenyon.lb.domain.dto.thumb.DoThumbDTO;
import com.tenyon.lb.domain.entity.Blog;
import com.tenyon.lb.domain.entity.Thumb;
import com.tenyon.lb.domain.entity.User;
import com.tenyon.lb.mapper.ThumbMapper;
import com.tenyon.lb.service.BlogService;
import com.tenyon.lb.service.ThumbService;
import com.tenyon.lb.service.UserService;
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
@Service
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

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
                    redisTemplate.opsForHash().put(RedisConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),
                            blogId.toString(),
                            thumb.getId());
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

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbDTO.getBlogId();
                Long thumbId =
                        (Long) redisTemplate.opsForHash().get(RedisConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),
                                blogId.toString());
                if (thumbId == null) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                boolean success = update && this.removeById(thumbId);
                if (success) {
                    redisTemplate.opsForHash().delete(RedisConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),
                            blogId.toString());
                }
                return success;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisConstant.USER_THUMB_KEY_PREFIX + userId.toString(),
                blogId.toString());
    }

}
