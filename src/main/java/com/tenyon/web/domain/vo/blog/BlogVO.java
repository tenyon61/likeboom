package com.tenyon.web.domain.vo.blog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Schema(description = "博客视图")
@Data
public class BlogVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面")
    private String coverImg;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer thumbCount;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "是否已点赞")
    private Boolean hasThumb;

    @Serial
    private static final long serialVersionUID = 1L;
}