package com.dreamer.userservice.handler;

import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import cn.dev33.satoken.exception.NotRoleException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotRoleException.class)
    public SaResult notRoleExceptionHandler() {
        return SaResult.error("该用户没有权限执行此操作");
    }
}
