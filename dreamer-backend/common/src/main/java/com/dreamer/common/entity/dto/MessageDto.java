package com.dreamer.common.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

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

    /**
     * 接收用户ID
     */
    private Long userId;


    /**
     * 是否已读：0未读 1已读
     */
    private Integer readStatus;

    /**
     * 接收时间
     */
    private LocalDateTime createTime;
}
