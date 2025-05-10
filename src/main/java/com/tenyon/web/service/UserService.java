package com.tenyon.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenyon.web.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {
    User getLoginUser(HttpServletRequest request);
}
