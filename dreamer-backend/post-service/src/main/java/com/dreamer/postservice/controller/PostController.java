package com.dreamer.postservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.common.entity.vo.PostVo;
import com.dreamer.postservice.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final IPostService postService;

    /**
     * 发布文章
     *
     * @param params
     * @return
     */
    @PostMapping
    public SaResult addPost(@RequestBody Map<String, String> params) {

        String content = params.get("content");
        return postService.addPost(content);
    }

    /**
     * 删除文章
     *
     * @param postId
     * @return
     */
    @DeleteMapping("/{postId}")
    public SaResult delPost(@PathVariable long postId) {
        return postService.delPost(postId);
    }

    /**
     * 查询我所有的文章
     *
     * @param cursor
     * @return
     */
    @GetMapping("/me")
    public SaResult listMyPost(@RequestParam(required = false) Long cursor) {

        return postService.listMyPost(cursor);
    }

    /**
     * 修改文章
     *
     * @param postId
     * @return
     */
    @PutMapping("/{postId}")
    public SaResult updatePostByPostId(@PathVariable Long postId, @RequestBody HashMap<String, String> params) {

        String content = params.get("content");
        return postService.updatePostByPostId(postId, content);
    }

    /**
     * 查看指定文章
     *
     * @param postId
     * @return
     */
    @GetMapping("/{postId}")
    public SaResult queryPostByPostId(@PathVariable Long postId) {
        return postService.queryPostByPostId(postId);
    }

    /**
     * 查询最新文章
     *
     * @return
     */
    @GetMapping("/new")
    public SaResult listNewPosts(@RequestParam(required = false) Long cursor) {
        return postService.listNewPosts(cursor);
    }

    /**
     * 查询热门文章
     *
     * @return
     */
    @GetMapping("/hot")
    public SaResult listHotPosts(@RequestParam(required = false) Long cursor
            , @RequestParam(required = false) Integer offset) {
        return postService.listHotPosts(cursor, offset);
    }

    /**
     * 查询关注作者的文章
     * @param cursor
     * @return
     */
    @GetMapping("/following")
    public SaResult listFollowingPost(@RequestParam(required = false) Long cursor) {
        return postService.listFollowingPost(cursor);
    }


    /**
     * 投递「质子」支持文章
     * @return
     */
    @PostMapping("/{postId}/protons")
    public SaResult protonPost(@PathVariable Long postId,Integer protons) {
        return postService.protonPost(postId, protons);
    }

    /**
     * 管理员查询待审核文件
     * @param page
     * @return
     */
    @GetMapping("/admin/pending")
    Page<PostVo> pagePendingPosts(@RequestParam("page") Integer page) {
        return postService.pagePendingPosts(page);
    }

    /**
     * 管理员审核文章
     * @param postId
     * @param status
     * @return
     */
    @PutMapping("/admin/check/{postId}")
    SaResult checkPost(@PathVariable Long postId, @RequestParam("status") Integer status) {
        return postService.checkPost(postId, status);
    }

    /**
     * 管理员统计总文章数
     * @return
     */
    @GetMapping("/admin/count")
    public Long countPosts() {
        return postService.countPosts();
    }
}
