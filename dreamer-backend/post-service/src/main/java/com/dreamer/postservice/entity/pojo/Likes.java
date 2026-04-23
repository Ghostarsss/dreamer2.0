package com.dreamer.postservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞表
 * 支持：文章点赞 / 评论点赞
 */
@Data
@TableName("likes") // 重点：like 是关键字，必须加反引号
public class Likes {

    /**
     * 点赞ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 点赞用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;
}