package com.dreamer.userservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dreamer.common.entity.dto.LoginDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * 判断当前用户是否被封禁
     * @param userId
     * @return
     */
    @GetMapping("/isBaned")
    public SaResult isBaned(long userId) {
        return userService.isBaned(userId);
    }

    /**
     * 批量查询用户信息
     * @param userIds
     * @return
     */
    @PostMapping("/batch-query-user")
    public SaResult batchQueryUserByUserId(@RequestBody List<Long> userIds) {
        return userService.batchQueryUserByUserId(userIds);
    }

    /**
     * 查询所有管理员
     * @return
     */
    @GetMapping("/all-admin")
    public SaResult listAdmin() {
        return userService.listAdmin();
    }

    /**
     * 管理员分页查询所有用户
     * @param page
     * @return
     */
    @GetMapping("/admin/all-users")
    public Page<User> listUsers(@RequestParam("page") Integer page) {
        return userService.listUsers(page);
    }

    /**
     * 管理员修改用户权限
     * @param userId
     * @param role
     * @return
     */
    @PutMapping("/admin/change-role")
    public SaResult changeRole(@RequestParam("userId") Long userId, @RequestParam("role") Integer role) {
        return userService.changeRole(userId, role);
    }

    /**
     * 管理员修改用户密码
     * @param userId
     * @param password
     */
    @PutMapping("/admin/password-edit")
    public void editPassword(@RequestParam("userId") Long userId,@RequestParam("password") String password) {
        userService.editPassword(userId, password);
    }

    /**
     * 管理员禁用/解除禁用用户
     * @param userId
     * @return
     */
    @PutMapping("/admin/ban")
    SaResult banUser(@RequestParam("userId") Long userId) {
        return userService.banUser(userId);
    }

    /**
     * 管理员删除用户
     * @param userId
     * @return
     */
    @DeleteMapping("/admin/delete")
    SaResult delUser(@RequestParam("userId") Long userId) {
        return userService.delUser(userId);
    }
}
