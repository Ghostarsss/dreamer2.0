package com.dreamer.userservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/me")
    public SaResult getMe() {
        return SaResult.ok("测试成功！");
    }


    @PostMapping("/register")
    public SaResult register(@RequestBody User user) {
        return userService.register(user);
    }
}
