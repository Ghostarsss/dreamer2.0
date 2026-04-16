package com.dreamer.userservice.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dreamer.common.entity.dto.LoginDto;
import org.springframework.web.multipart.MultipartFile;

import static com.dreamer.userservice.constant.UserConstant.USER_SUPER_ADMIN_ROLE;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 查询当前用户信息
     *
     * @return
     */
    @GetMapping("/me")
    public SaResult getMe() {
        return userService.getMe();
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

    /**
     * 修改用户头像
     *
     * @param avatar
     * @return
     */
    @PostMapping("/me/avatars")
    public SaResult updateAvatars(@RequestParam("avatar") MultipartFile avatar) {
        return userService.updateAvatars(avatar);
    }

    /**
     * 修改用户验证码发送
     * @return
     */
    @GetMapping("/me/send-verify-code")
    public SaResult sendVerifyCode() {
        return userService.sendVerifyCode();
    }

    /**
     * 修改用户信息
     * @param userDto
     * @return
     */
    @PutMapping("/me")
    public SaResult updateMe(@RequestBody UserDto userDto) {
        return userService.updateMe(userDto);
    }

    /**
     * 查询指定用户信息
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public SaResult queryUserById(@PathVariable String userId) {
        return userService.queryUserById(Integer.valueOf(userId));
    }

    /**
     * 查询用户公开信件
     * @param userId
     * @return
     */
    @GetMapping("/{userId}/open-letters")
    public SaResult queryOpenLettersByUserId(@PathVariable String userId) {
        return userService.queryOpenLettersByUserId(userId);
    }
}
