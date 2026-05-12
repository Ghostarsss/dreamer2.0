package com.dreamer.postservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.postservice.entity.dto.CommentDto;
import com.dreamer.postservice.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.*;

import static com.dreamer.common.constant.RabbitMQConstant.POST_DELETE_COMMENT_LIKE_QUEUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final ICommentService commentService;

    /**
     * 添加评论
     * @param commentDto
     * @return
     */
    @PostMapping
    public SaResult commentPostByPostId(@RequestBody CommentDto commentDto) {
        return commentService.commentPostByPostId(commentDto);
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @DeleteMapping("/{commentId}")
    public SaResult removeCommentByCommentId(@PathVariable Long commentId) {
        return commentService.removeCommentByCommentId(commentId);
    }

    /**
     * 查询一级评论
     * @param postId
     * @return
     */
    @GetMapping("/post/{postId}")
    public SaResult listCommentsByPostId(@PathVariable Long postId,@RequestParam(required = false) Long cursor) {
        return commentService.listCommentsByPostId(postId,cursor);
    }

    /**
     * 查询二级评论
     * @param commentId
     * @param cursor
     * @return
     */
    @GetMapping("/reply/{commentId}")
    public SaResult listCommentsByCommentId(@PathVariable Long commentId, @RequestParam(required = false) Long cursor) {
        return commentService.listCommentsByCommentId(commentId, cursor);
    }

    /**
     * 查询所有评论
     * @param postId
     * @return
     */
    @GetMapping("/{postId}")
    public SaResult listAllCommentsByPostId(@PathVariable Long postId) {
        return commentService.listAllCommentsByPostId(postId);
    }

    /**
     * 管理员统计总文章数
     * @return
     */
    @GetMapping("/admin/count")
    Long countComments() {
        return commentService.countComments();
    }
}
