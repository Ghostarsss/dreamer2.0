package com.dreamer.userservice.service.impl;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.userservice.mapper.UserMapper;
import com.dreamer.userservice.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public SaResult register(User user) {

        log.info("注册用户：{}",user);
        //注册用户
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        save(user);
        return SaResult.ok();
    }
}
