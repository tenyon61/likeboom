package com.tenyon.lb.common.utils;

import com.tenyon.lb.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * redis 操作
 *
 * @author tenyon
 * @date 2025/4/16
 */
@Slf4j
public class RedisUtils {

    /**
     * 获取用户点赞的 key
     * @param userId 用户Id
     * @return key
     */
    public static String getUserThumbKey(Long userId) {
        return RedisConstant.USER_THUMB_KEY_PREFIX + userId;
    }

    /**
     * 获取 临时点赞记录 key
     * @param time 时间str
     * @return key
     */
    public static String getTempThumbKey(String time) {
        return RedisConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }

}