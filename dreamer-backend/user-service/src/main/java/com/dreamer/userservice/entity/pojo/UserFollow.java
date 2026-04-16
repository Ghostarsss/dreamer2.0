package com.dreamer.userservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户关注关系表 实体类
 */
@Data
@TableName("user_follow")
public class UserFollow {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关注者ID
     */
    private Long userId;

    /**
     * 被关注者ID
     */
    private Long followedUserId;

    /**
     * 关注时间
     */
    private LocalDateTime createTime;
}