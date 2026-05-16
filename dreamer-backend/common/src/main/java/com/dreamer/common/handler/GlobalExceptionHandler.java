package com.dreamer.common.handler;

import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dreamer.common.message.SystemMessage.SYSTEM_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public SaResult handleException(Exception e) {
        return SaResult.error(SYSTEM_ERROR);
    }
}
