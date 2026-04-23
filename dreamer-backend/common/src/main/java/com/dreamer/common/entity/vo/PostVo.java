package com.dreamer.common.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVo {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 发布文章的用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 计算后的 level 等级
     */
    private Integer level;

    /**
     * 文章正文
     */
    private String content;

    /**
     * 文章状态：0待审核，1通过，2驳回
     */
    private Integer status;

    /**
     * 文章支持数（投质子数）
     */
    private Integer protonCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否点赞(0为未点赞，1为点赞)
     */
    private Integer isLike;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 修改次数
     */
    private Integer editCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
