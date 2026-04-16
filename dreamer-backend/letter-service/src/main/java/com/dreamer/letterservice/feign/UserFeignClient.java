package com.dreamer.letterservice.feign;

import cn.dev33.satoken.util.SaResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service"
)
public interface UserFeignClient {

    @PutMapping("/vp/letter-upload-images")
    public SaResult uploadImagesWithProtonCost(@RequestParam("userId") long userId);

}
