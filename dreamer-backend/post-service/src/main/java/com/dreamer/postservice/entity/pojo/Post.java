package com.dreamer.postservice.entity.pojo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章表
 */
@Data
@TableName("post")
public class Post {

    /**
     * 文章ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发布文章的用户ID
     */
    private Long userId;

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
     * 软删除时间
     */
    @TableLogic(value = "null",delval = "now()")
    private LocalDateTime deletedAt;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 修改次数
     */
    private Integer editCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}