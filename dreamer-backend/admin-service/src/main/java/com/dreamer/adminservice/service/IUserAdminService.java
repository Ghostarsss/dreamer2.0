package com.dreamer.adminservice.service;

import cn.dev33.satoken.util.SaResult;

public interface IUserAdminService {

    SaResult pageUsers(Integer page);

    SaResult changeRole(Long userId, Integer role);

    SaResult editPassword(Long userId, String password);

    SaResult banUser(Long userId);

    SaResult delUser(Long userId);

    SaResult adminNotifyUser(Long userId, String content);
}
