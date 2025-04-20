package com.tenyon.lb.domain.dto.thumb;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存热点点赞数据
 *
 * @author tenyon
 * @date 2025/4/20
 */
@Schema(description = "热点点赞数据")
@Data
public class HotThumb implements Serializable {

    @Schema(description = "点赞Id")
    private Long thumbId;

    @Schema(description = "过期时间ms")
    private Long expireTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
