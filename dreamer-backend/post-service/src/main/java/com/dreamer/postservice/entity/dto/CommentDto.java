package com.dreamer.postservice.entity.dto;

import lombok.Data;

@Data
public class CommentDto {

    /**
     * 父评论 id，一级评论为 null
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 文章 id
     */
    private Long postId;

}
