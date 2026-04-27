package com.dreamer.adminservice.feign;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.common.entity.vo.PostVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "post-service")
public interface PostFeignClient {

    @GetMapping("/posts/admin/pending")
    Page<PostVo> pagePendingPosts(@RequestParam("page") Integer page);

    @PutMapping("/posts/admin/check/{postId}")
    SaResult checkPost(@PathVariable Long postId, @RequestParam("status") Integer status);

    @GetMapping("/posts/admin/count")
    Long countPosts();

    @GetMapping("/comments/admin/count")
    Long countComments();
}
