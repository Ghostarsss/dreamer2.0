package com.dreamer.authservice.service;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.LoginDto;

public interface ILoginService {
    SaResult login(LoginDto loginDto);

    SaResult logout();
}
