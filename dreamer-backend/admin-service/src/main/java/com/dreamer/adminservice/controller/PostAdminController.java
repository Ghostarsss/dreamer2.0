package com.dreamer.adminservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.service.IPostAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/posts")
public class PostAdminController {

    private final IPostAdminService postAdminService;

    /**
     * 查询待审核文章
     * @param page
     * @return
     */
    @GetMapping
    public SaResult pagePendingPosts(Integer page) {
        return postAdminService.pagePendingPosts(page);
    }

    /**
     * 管理员审核文章
     * @param postId
     * @param param
     * @return
     */
    @PutMapping("/{postId}")
    public SaResult checkPost(@PathVariable Long postId, @RequestBody HashMap<String,Integer> param) {

        Integer status = param.get("status");
        return postAdminService.checkPost(postId, status);
    }

}
