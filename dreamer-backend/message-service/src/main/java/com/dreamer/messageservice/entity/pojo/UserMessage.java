package com.dreamer.messageservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户消息表
 */
@Data
@TableName("user_message")
public class UserMessage{

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息模板ID
     */
    private Long messageId;

    /**
     * 是否已读：0未读 1已读
     */
    private Integer readStatus;

    /**
     * 接收时间
     */
    private LocalDateTime createTime;
}