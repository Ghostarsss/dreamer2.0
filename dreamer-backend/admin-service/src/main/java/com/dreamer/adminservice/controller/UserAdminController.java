package com.dreamer.adminservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.service.IUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserAdminController {

    private final IUserAdminService userAdminService;

    /**
     * 管理员查看所有用户
     * @param page
     * @return
     */
    @GetMapping
    public SaResult pageUsers(Integer page) {
        return userAdminService.pageUsers(page);
    }

    /**
     * 管理员修改用户权限
     * @param userId
     * @param param
     * @return
     */
    @PutMapping("/{userId}/roles")
    public SaResult changeRole(@PathVariable Long userId,@RequestBody HashMap<String, Integer> param) {
        Integer role = param.get("role");
        return userAdminService.changeRole(userId,role);
    }

    /**
     * 修改密码
     * @param userId
     * @return
     */
    @PutMapping("/password-edit/{userId}")
    public SaResult editPassword(@PathVariable Long userId,@RequestBody HashMap<String,String> param) {
        String password = param.get("password");
        return userAdminService.editPassword(userId,password);
    }

    /**
     * 将该用户封禁/取消封禁
     * @param userId
     * @return
     */
    @PutMapping("/{userId}/ban")
    public SaResult banUser(@PathVariable Long userId) {
        return userAdminService.banUser(userId);
    }

    /**
     * 删除用户
     * @param userId
     * @return
     */
    @DeleteMapping("/{userId}")
    public SaResult delUser(@PathVariable Long userId) {
        return userAdminService.delUser(userId);
    }

    /**
     * 管理员通知用户
     * @param userId
     * @return
     */
    @PostMapping("/{userId}/messages")
    public SaResult adminNotifyUser(@PathVariable Long userId,@RequestBody HashMap<String,String> param) {

        String content = param.get("content");
        return userAdminService.adminNotifyUser(userId,content);
    }

}
