package com.dreamer.common.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户消息表（继承消息模板）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_message")
public class UserMessage extends MessageTemplate {

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