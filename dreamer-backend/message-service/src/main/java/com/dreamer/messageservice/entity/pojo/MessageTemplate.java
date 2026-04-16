package com.dreamer.messageservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息模板表
 */
@Data
@TableName("message_template")
public class MessageTemplate {

    /**
     * 消息模板ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息类型：
     * 0注册成功，1公告，2反馈，3警告，4点赞，5投币，6评论，7关注，8等级提升，9信件创建，10信件开启
     */
    private Integer type;

    /**
     * 发送者ID
     */
    private Long sendId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否广播：0否 1是
     */
    private Integer isBroadcast;

    private LocalDateTime createTime;
}