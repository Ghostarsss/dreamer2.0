package com.dreamer.postservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("comment")
public class Comment {

    /**
     * 评论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 父评论ID，NULL表示一级评论
     */
    private Long parentId;

    /**
     * 被回复用户ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 软删除
     */
    @TableLogic(value = "null",delval = "now()")
    private LocalDateTime deletedAt;

    /**
     * 评论时间，由后端插入
     */
    private LocalDateTime createTime;

}