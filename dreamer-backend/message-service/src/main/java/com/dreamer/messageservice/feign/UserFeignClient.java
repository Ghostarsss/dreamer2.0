package com.dreamer.messageservice.feign;

import cn.dev33.satoken.util.SaResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/users/{userId}")
    public SaResult queryUserById(@PathVariable String userId);

}
