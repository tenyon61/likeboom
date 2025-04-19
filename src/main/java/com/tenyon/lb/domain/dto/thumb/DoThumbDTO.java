package com.tenyon.lb.domain.dto.thumb;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "点赞请求")
@Data
public class DoThumbDTO {

    @Schema(description = "博客Id")
    private Long blogId;
}
