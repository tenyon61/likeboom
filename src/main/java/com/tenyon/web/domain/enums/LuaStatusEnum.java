package com.tenyon.web.domain.enums;

import lombok.Getter;

/**
 * lua脚本执行状态
 *
 * @author tenyon
 * @date 2025/4/21
 */
@Getter
public enum LuaStatusEnum {
    // 成功
    SUCCESS(1L),
    // 失败
    FAIL(-1L),
    ;

    private final long value;

    LuaStatusEnum(long value) {
        this.value = value;
    }
}
