package com.dreamer.authservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import org.springframework.web.multipart.MultipartFile;

public interface IRegisterService extends IService<User> {
    SaResult sendVerifyCode(String email);

    SaResult uploadAvatar(MultipartFile avatar);

    SaResult register(UserDto userDto);
}
