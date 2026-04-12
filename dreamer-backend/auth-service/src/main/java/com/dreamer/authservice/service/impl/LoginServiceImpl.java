package com.dreamer.authservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dreamer.authservice.feign.UserFeignClient;
import com.dreamer.common.entity.dto.LoginDto;
import com.dreamer.authservice.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.dreamer.common.message.SystemMessage.SYSTEM_ERROR;
import static com.dreamer.common.message.UserMessage.LOGOUT_SUCCESS;

@Service
@Slf4j
public class LoginServiceImpl implements ILoginService {

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public SaResult login(LoginDto loginDto) {

        log.info("用户登录: {}", loginDto);

        //openfeign 远程调用 user-service 实现登录账号逻辑
        return userFeignClient.login(loginDto);
    }

    @Override
    public SaResult logout() {
        try {
            StpUtil.logout(StpUtil.getSession().get("userId"));
            return SaResult.ok(LOGOUT_SUCCESS);
        } catch (Exception e) {
            log.error("用户退出登录失败: {}",e.getMessage());
            return SaResult.error(SYSTEM_ERROR);
        }
    }
}
