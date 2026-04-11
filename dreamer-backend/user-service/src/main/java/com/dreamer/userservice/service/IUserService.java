package com.dreamer.userservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.pojo.User;

public interface IUserService extends IService<User> {

    SaResult register(User user);
}
