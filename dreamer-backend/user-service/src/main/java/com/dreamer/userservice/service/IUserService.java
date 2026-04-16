package com.dreamer.userservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.EXPRabbitDto;
import com.dreamer.common.entity.dto.LoginDto;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService extends IService<User> {

    SaResult register(User user);

    SaResult login(LoginDto loginDto);

    SaResult getMe();

    SaResult updateAvatars(MultipartFile avatar);

    SaResult sendVerifyCode();

    SaResult updateMe(UserDto userDto);

    SaResult queryUserById(Integer userId);

    SaResult queryOpenLettersByUserId(String userId);

    void followingEXP(EXPRabbitDto expRabbitDto);
}
