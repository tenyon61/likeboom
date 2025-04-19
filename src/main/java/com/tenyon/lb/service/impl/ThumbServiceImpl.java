package com.tenyon.lb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
                boolean exists = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .exists();
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
                return update && this.save(thumb);
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
                Thumb thumb = this.lambdaQuery()
                        .eq(Thumb::getUserId, loginUser.getId())
                        .eq(Thumb::getBlogId, blogId)
                        .one();
                if (thumb == null) {
                    throw new RuntimeException("用户未点赞");
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();

                return update && this.removeById(thumb.getId());
            });
        }
    }

}
