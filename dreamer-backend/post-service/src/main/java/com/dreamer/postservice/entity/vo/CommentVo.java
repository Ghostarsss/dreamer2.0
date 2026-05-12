package com.dreamer.postservice.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVo {

    /**
     * 评论ID
     */
    private String id;

    /**
     * 评论用户ID
     */
    private String userId;

    /**
     * 文章ID
     */
    private String postId;

    /**
     * 文章作者ID
     */
    private String postUserId;

    /**
     * 父评论ID，NULL表示一级评论
     */
    private String parentId;

    /**
     * 若是二级评论，显示回复对象 userid
     */
    private String replyUserId;

    /**
     * 若是二级评论，显示回复对象 username
     */
    private String replyToUsername;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论时间，由后端插入
     */
    private LocalDateTime createTime;


    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户角色：1普通用户，2管理员，3超级管理员
     */
    private Integer role;

    /**
     * 计算后的 level 等级
     */
    private Integer level;

    /**
     * 当前用户是否点赞
     */
    private Integer isLike;

}
