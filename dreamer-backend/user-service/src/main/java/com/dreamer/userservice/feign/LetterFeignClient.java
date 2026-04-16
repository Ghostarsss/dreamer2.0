package com.dreamer.userservice.feign;

import cn.dev33.satoken.util.SaResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "letter-service"
)
public interface LetterFeignClient {

    @GetMapping("/letters/queryOpenLettersByUserId")
    SaResult queryOpenLettersByUserId(@RequestParam String userId);
}
