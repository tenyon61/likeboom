package com.tenyon.lb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenyon.lb.domain.dto.thumb.DoThumbDTO;
import com.tenyon.lb.domain.entity.Thumb;

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

//    Boolean hasThumb(Long blogId, Long userId);
}