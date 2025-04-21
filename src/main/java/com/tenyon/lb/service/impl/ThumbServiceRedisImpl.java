package com.tenyon.lb.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenyon.lb.common.constant.RedisConstant;
import com.tenyon.lb.common.exception.ErrorCode;
import com.tenyon.lb.common.exception.ThrowUtils;
import com.tenyon.lb.common.utils.RedisUtils;
import com.tenyon.lb.domain.dto.thumb.DoThumbDTO;
import com.tenyon.lb.domain.entity.Thumb;
import com.tenyon.lb.domain.entity.User;
import com.tenyon.lb.domain.enums.LuaStatusEnum;
import com.tenyon.lb.mapper.ThumbMapper;
import com.tenyon.lb.service.ThumbService;
import com.tenyon.lb.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 点赞服务
 *
 * @author tenyon
 * @date 2025/4/18
 */
@Slf4j
@RequiredArgsConstructor
@Service("thumbService")
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbDTO doThumbDTO) {
        if (doThumbDTO == null || doThumbDTO.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser();
        Long blogId = doThumbDTO.getBlogId();

        String timeSlice = getTimeSlice();
        // Redis Key  
        String tempThumbKey = RedisUtils.getTempThumbKey(timeSlice);
        String userThumbKey = RedisUtils.getUserThumbKey(loginUser.getId());

        // 执行 Lua 脚本  
        long result = redisTemplate.execute(
                RedisConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );

        ThrowUtils.throwIf(LuaStatusEnum.FAIL.getValue() == result, ErrorCode.OPERATION_ERROR,"用户已点赞");

        // 更新成功才执行
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    @Override
    public Boolean undoThumb(DoThumbDTO doThumbDTO) {
        if (doThumbDTO == null || doThumbDTO.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser();

        Long blogId = doThumbDTO.getBlogId();
        // 计算时间片  
        String timeSlice = getTimeSlice();
        // Redis Key  
        String tempThumbKey = RedisUtils.getTempThumbKey(timeSlice);
        String userThumbKey = RedisUtils.getUserThumbKey(loginUser.getId());

        // 执行 Lua 脚本  
        long result = redisTemplate.execute(
                RedisConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUser.getId(),
                blogId
        );
        // 根据返回值处理结果
        ThrowUtils.throwIf(result == LuaStatusEnum.FAIL.getValue(), ErrorCode.OPERATION_ERROR,"用户未点赞");
        return LuaStatusEnum.SUCCESS.getValue() == result;
    }

    private String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20  
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisUtils.getUserThumbKey(userId), blogId.toString());
    }

}