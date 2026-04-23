package com.dreamer.messageservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.messageservice.entity.pojo.MessageTemplate;

public interface IMessageService extends IService<MessageTemplate> {

    void addMessage(MessageDto messageDto);

    SaResult messageCount(String isRead);

    SaResult listMessage();

    void followingMessage(FollowingRabbitDto followingRabbitDto);

    void postOrCommentLikedMessage(MessageDto messageDto);

    void commentedMessage(MessageDto messageDto);
}
