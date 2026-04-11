package com.dreamer.authservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.authservice.entity.dto.LoginDto;
import com.dreamer.authservice.service.IRegisterService;
import com.dreamer.common.entity.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/register")
@Slf4j
public class RegisterController {

    @Autowired
    private IRegisterService registerService;

    /**
     * 发送验证码
     * @param loginDto
     * @return
     */
    @PostMapping("/send-verify-code")
    public SaResult sendVerifyCode(@RequestBody LoginDto loginDto) {
        String email = loginDto.getEmail();
        log.info("Email: {} 正在请求验证码",email);
        return registerService.sendVerifyCode(email);
    }

    /**
     * 上传头像
     * @param avatar
     * @return
     */
    @PostMapping("/avatars")
    public SaResult uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        return registerService.uploadAvatar(avatar);
    }

    @PostMapping
    public SaResult register(@RequestBody UserDto userDto) {
        return registerService.register(userDto);
    }
}
