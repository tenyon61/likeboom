package com.tenyon.web.domain.enums.thumb;

import lombok.Getter;

/**
 * 点赞类型枚举
 *
 * @author tenyon
 * @date 2025/4/20
 */
@Getter
public enum ThumbTypeEnum {

    // 点赞
    INCR(1),
    // 取消点赞
    DECR(-1),
    // 无变化
    NON(0),
    ;

    private final int value;

    ThumbTypeEnum(int value) {
        this.value = value;
    }

}
