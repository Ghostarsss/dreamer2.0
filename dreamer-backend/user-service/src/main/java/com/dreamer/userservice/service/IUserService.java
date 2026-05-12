package com.dreamer.userservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.LoginDto;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService extends IService<User> {

    SaResult register(User user);

    SaResult login(LoginDto loginDto);

    SaResult getMe();

    SaResult updateAvatars(MultipartFile avatar);

    SaResult sendVerifyCode();

    SaResult updateMe(UserDto userDto);

    SaResult queryUserById(Long userId);

    SaResult queryOpenLettersByUserId(String userId);

    SaResult isBaned(long userId);

    SaResult batchQueryUserByUserId(List<String> userIds);

    SaResult listAdmin();

    Page<User> listUsers(Integer page);

    SaResult changeRole(Long userId, Integer role);

    void editPassword(Long userId, String password);

    SaResult banUser(Long userId);

    SaResult delUser(Long userId);

    Long countUsers();
}
