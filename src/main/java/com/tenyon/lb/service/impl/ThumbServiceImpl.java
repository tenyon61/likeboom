package com.tenyon.lb.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenyon.lb.common.constant.RedisConstant;
import com.tenyon.lb.common.exception.BusinessException;
import com.tenyon.lb.common.exception.ErrorCode;
import com.tenyon.lb.common.exception.ThrowUtils;
import com.tenyon.lb.domain.dto.thumb.DoThumbDTO;
import com.tenyon.lb.domain.dto.thumb.HotThumb;
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

import java.time.ZoneOffset;

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
                    HotThumb hotThumb = new HotThumb();
                    hotThumb.setThumbId(thumb.getId());
                    // 30天热点点赞数据缓存
                    hotThumb.setExpireTime(LocalDateTimeUtil.now().plusDays(30).toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    redisTemplate.opsForHash().put(RedisConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString(), hotThumb);
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

            // 编程式事务 key=thumb:userid field=blogid
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbDTO.getBlogId();
                HotThumb hotThumb = (HotThumb) redisTemplate.opsForHash().get(RedisConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),
                                blogId.toString());
                // redis中无点赞数据，或者已经过期就要去查询 mysql
                if (hotThumb == null || hotThumb.getExpireTime() < DateUtil.current()) {
                    Thumb thumb = this.lambdaQuery().eq(Thumb::getUserId, loginUser.getId()).eq(Thumb::getBlogId, blogId).one();
                    ThrowUtils.throwIf(thumb == null, ErrorCode.OPERATION_ERROR, "用户未点赞");
                    hotThumb = new HotThumb();
                    hotThumb.setThumbId(thumb.getId());
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                boolean success = update && this.removeById(hotThumb.getThumbId());
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
        HotThumb hotThumb = (HotThumb) redisTemplate.opsForHash().get(RedisConstant.USER_THUMB_KEY_PREFIX + userId.toString(),
                blogId.toString());
        // 查看 redis中的点赞缓存数据
        if (hotThumb == null) {
            //查看 mysql中的点赞数据
            Thumb thumb = this.lambdaQuery().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId).one();
            return thumb != null;
        }
        if (hotThumb.getExpireTime() < DateUtil.current()) {
            // 点赞数据过期
            redisTemplate.opsForHash().delete(RedisConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
            return false;

        }
        return true;
    }

}
