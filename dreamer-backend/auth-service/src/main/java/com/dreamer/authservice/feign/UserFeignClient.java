package com.dreamer.authservice.feign;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service"
)
public interface UserFeignClient {

    @PostMapping("/users/register")
    public SaResult register(@RequestBody User user);
}
