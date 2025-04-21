package com.tenyon.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenyon.web.domain.dto.thumb.DoThumbDTO;
import com.tenyon.web.domain.entity.Thumb;

/**
 * 点赞服务
 *
 * @author tenyon
 * @date 2025/4/18
 */
public interface ThumbService extends IService<Thumb> {

    /**
     * 点赞
     *
     * @param doThumbDTO 点赞请求
     * @return Boolean
     */
    Boolean doThumb(DoThumbDTO doThumbDTO);

    /**
     * 取消点赞
     *
     * @param doThumbDTO 点赞请求
     * @return Boolean
     */
    Boolean undoThumb(DoThumbDTO doThumbDTO);

    /**
     * 是否点赞
     *
     * @param blogId 博客id
     * @param userId 用户id
     * @return Boolean
     */
    Boolean hasThumb(Long blogId, Long userId);
}