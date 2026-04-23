package com.dreamer.postservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.postservice.service.ILikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikesController {

    private final ILikesService likesService;

    /**
     * 点赞/取消点赞文章
     * @param postId
     * @return
     */
    @PutMapping("/post/{postId}")
    public SaResult likePost(@PathVariable Long postId) {
        return likesService.likePost(postId);
    }

    /**
     * 点赞/取消点赞评论
     * @param commentId
     * @return
     */
    @PutMapping("/comment/{commentId}")
    public SaResult likeComment(@PathVariable Long commentId) {
        return likesService.likeComment(commentId);
    }
}
