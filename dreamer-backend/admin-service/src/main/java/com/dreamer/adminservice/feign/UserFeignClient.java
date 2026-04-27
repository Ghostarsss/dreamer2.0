package com.dreamer.adminservice.feign;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.common.entity.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/users/admin/all-users")
    public Page<User> listUsers(@RequestParam("page") Integer page);

    @PutMapping("/users/admin/change-role")
    public SaResult changeRole(@RequestParam("userId") Long userId, @RequestParam("role") Integer role);

    @PutMapping("/users/admin/password-edit")
    void editPassword(@RequestParam("userId") Long userId,@RequestParam("password") String password);

    @PutMapping("/users/admin/ban")
    SaResult banUser(@RequestParam("userId") Long userId);

    @DeleteMapping("/users/admin/delete")
    SaResult delUser(@RequestParam("userId") Long userId);

    @GetMapping("/users/admin/count")
    Long countUsers();
}
