package com.dreamer.postservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.postservice.entity.dto.CommentDto;
import com.dreamer.postservice.entity.pojo.Comment;

public interface ICommentService extends IService<Comment> {

    void delCommentsByPostId(long postId);

    SaResult commentPostByPostId(CommentDto commentDto);

    SaResult removeCommentByCommentId(Long commentId);

    SaResult listCommentsByPostId(Long postId, Long cursor);

    SaResult listCommentsByCommentId(Long commentId, Long cursor);

    Long countComments();

    SaResult listAllCommentsByPostId(Long postId);

    Long countCommentsByPostId(Long postId);
}
