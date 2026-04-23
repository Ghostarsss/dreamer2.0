package com.dreamer.authservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.LoginDto;
import com.dreamer.authservice.service.ILoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private ILoginService loginService;

    /**
     * 登录
     * @param loginDto
     * @return
     */
    @PostMapping
    public SaResult login(@RequestBody LoginDto loginDto) {
        return loginService.login(loginDto);
    }

    /**
     * 登出
     * @return
     */
    @DeleteMapping("/out")
    public SaResult logout() {
        return loginService.logout();
    }
}
