package com.dreamer.userservice.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dreamer.common.entity.dto.LoginDto;

import static com.dreamer.userservice.constant.UserConstant.USER_SUPER_ADMIN_ROLE;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/me")
    public SaResult getMe() {

        StpUtil.checkRole(USER_SUPER_ADMIN_ROLE);

        return SaResult.ok("测试成功！");
    }

    /**
     * 注册用户
     *
     * @param user
     * @return
     */
    @PostMapping("/register")
    public SaResult register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 获取指定用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/users/{userId}")
    public SaResult getUserById(@PathVariable Integer userId) {
        return null;
    }

    /**
     * 登录账户
     *
     * @param loginDto
     * @return
     */
    @PostMapping("/login")
    public SaResult login(@RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }
}
