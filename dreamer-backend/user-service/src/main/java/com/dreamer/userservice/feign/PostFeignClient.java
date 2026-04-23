package com.dreamer.userservice.feign;

import cn.dev33.satoken.util.SaResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "post-service")
public interface PostFeignClient {

    @GetMapping("/posts/{postId}")
    public SaResult queryPostByPostId(@PathVariable Long postId);
}
