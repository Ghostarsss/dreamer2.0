package com.dreamer.messageservice.entity.vo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVo {

    /**
     * 消息 id
     */
    private String id;

    /**
     * 消息类型：
     * 0注册成功，1公告，2反馈，3警告，4点赞，5投币，6评论，7关注，8等级提升，9信件开启
     */
    private Integer type;

    /**
     * 发送者ID
     */
    private String sendId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读：0未读 1已读
     */
    private Integer readStatus;

    /**
     * 接收时间
     */
    private LocalDateTime createTime;

    /**
     * 发送者头像
     */
    private String avatar;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户等级
     */
    private Integer level;
}
