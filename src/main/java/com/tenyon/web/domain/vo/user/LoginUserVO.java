package com.tenyon.web.domain.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "已登录用户信息（脱敏）")
public class LoginUserVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户昵称")
    private String name;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "令牌")
    private String token;

}