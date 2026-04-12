package com.dreamer.userservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.entity.dto.LoginDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.common.entity.pojo.UserMessage;
import com.dreamer.userservice.mapper.UserMapper;
import com.dreamer.userservice.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.dreamer.common.constant.MessageConstant.REGISTER_MESSAGE_TYPE;
import static com.dreamer.common.message.UserMessage.*;
import static com.dreamer.userservice.constant.UserConstant.USER_IS_BANNED;
import static com.dreamer.userservice.message.RegisterMessage.PASSWORD_ERROR;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public SaResult register(User user) {

        log.info("注册用户：{}", user);
        //注册用户
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        save(user);

        Long userId = user.getId();

        //rabbit 发送注册成功消息
        //封装通知内容
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(userId);
        userMessage.setType(REGISTER_MESSAGE_TYPE);
        userMessage.setCreateTime(LocalDateTime.now());
        userMessage.setContent("亲爱的用户 "+user.getUsername()+" 欢迎您来到 dreamer 大家庭，这里有许多有趣的功能等待您探索🎉");
        rabbitTemplate.convertAndSend("auth.register.message.queue",userMessage);

        //登录
        StpUtil.login(userId);
        //缓存 user 对象信息
        StpUtil.getSession().set("userId", userId);
        return SaResult.ok();
    }

    @Override
    public SaResult login(LoginDto loginDto) {

        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        //获取用户信息，判断用户是否存在
        User user = query().eq("email", email).one();

        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        //用户是否被封禁
        if (user.getStatus().equals(USER_IS_BANNED)) {
            return SaResult.error(USER_IS_BANNED_MESSAGE);
        }

        //密码校验
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(password, user.getPassword());
        if (!matches) {
            return SaResult.error(PASSWORD_ERROR);
        }


        Long userId = user.getId();
        //先令之前的 token 失效，否则多个账号会同时登录
        StpUtil.logout(userId);
        //登录成功
        StpUtil.login(userId);
        //缓存 user 对象信息
        StpUtil.getSession().set("userId", userId);
        //添加角色权限
        return SaResult.ok(LOGIN_SUCCESS);
    }
}
