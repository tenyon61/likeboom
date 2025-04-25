package com.tenyon.web.common.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * redis 常量类
 *
 * @author tenyon
 * @date 2025/4/18
 */
public interface RedisConstant {

    /**
     * 用户点赞前缀
     */
    String USER_THUMB_KEY_PREFIX = "thumb:";

    /**
     * 临时 点赞记录 key
     */
    String TEMP_THUMB_KEY_PREFIX = "thumb:temp:%s";

    /**
     * 当前未点赞
     */
    Long UN_THUMB_CONSTANT = 0L;


    /**
     * 点赞 Lua 脚本
     * KEYS[1]       -- 临时计数键
     * KEYS[2]       -- 用户点赞状态键
     * ARGV[1]       -- 用户 ID
     * ARGV[2]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    RedisScript<Long> THUMB_SCRIPT = new DefaultRedisScript<>("""  
            local tempThumbKey = KEYS[1]       -- 临时计数键（如 thumb:temp:{timeSlice}）  
            local userThumbKey = KEYS[2]       -- 用户点赞状态键（如 thumb:{userId}）  
            local userId = ARGV[1]             -- 用户 ID  
            local blogId = ARGV[2]             -- 博客 ID  
            
            -- 1. 检查是否已点赞（避免重复操作）  
            if redis.call('HEXISTS', userThumbKey, blogId) == 1 then  
                return -1  -- 已点赞，返回 -1 表示失败  
            end  
            
            -- 2. 获取旧值（不存在则默认为 0）  
            local hashKey = userId .. ':' .. blogId  
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)  
            
            -- 3. 计算新值  
            local newNumber = oldNumber + 1  
            
            -- 4. 原子性更新：写入临时计数 + 标记用户已点赞  
            redis.call('HSET', tempThumbKey, hashKey, newNumber)  
            redis.call('HSET', userThumbKey, blogId, 1)  
            
            return 1  -- 返回 1 表示成功  
            """, Long.class);

    /**
     * 取消点赞 Lua 脚本
     * 参数同上
     * 返回：
     * -1: 未点赞
     * 1: 操作成功
     */
    RedisScript<Long> UNTHUMB_SCRIPT = new DefaultRedisScript<>("""  
            local tempThumbKey = KEYS[1]      -- 临时计数键（如 thumb:temp:{timeSlice}）  
            local userThumbKey = KEYS[2]      -- 用户点赞状态键（如 thumb:{userId}）  
            local userId = ARGV[1]            -- 用户 ID  
            local blogId = ARGV[2]            -- 博客 ID  
            
            -- 1. 检查用户是否已点赞（若未点赞，直接返回失败）  
            if redis.call('HEXISTS', userThumbKey, blogId) ~= 1 then  
                return -1  -- 未点赞，返回 -1 表示失败  
            end  
            
            -- 2. 获取当前临时计数（若不存在则默认为 0）  
            local hashKey = userId .. ':' .. blogId  
            local oldNumber = tonumber(redis.call('HGET', tempThumbKey, hashKey) or 0)  
            
            -- 3. 计算新值并更新  
            local newNumber = oldNumber - 1  
            
            -- 4. 原子性操作：更新临时计数 + 删除用户点赞标记  
            redis.call('HSET', tempThumbKey, hashKey, newNumber)  
            redis.call('HDEL', userThumbKey, blogId)  
            
            return 1  -- 返回 1 表示成功  
            """, Long.class);

}