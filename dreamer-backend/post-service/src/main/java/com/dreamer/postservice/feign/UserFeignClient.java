package com.dreamer.postservice.feign;

import cn.dev33.satoken.util.SaResult;
import org.apache.ibatis.annotations.Update;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "user-service"
)
public interface UserFeignClient {

    @GetMapping("/users/isBaned")
    public SaResult isBaned(@RequestParam("userId") long userId);

    @GetMapping("/users/{userId}")
    public SaResult queryUserById(@PathVariable String userId);

    @PostMapping("/users/batch-query-user")
    public SaResult batchQueryUserByUserId(List<Long> userIds);

    @GetMapping("/follows/{userId}/following")
    public SaResult listFollowingByUserId(@PathVariable Long userId, @RequestParam(required = false) Long cursor
            , @RequestParam(required = false) Integer offset);

    @PutMapping("/vp/deduct-proton/{userId}")
    public SaResult deductProton(@PathVariable Long userId, @RequestParam("protons") Integer protons);
}
