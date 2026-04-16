package com.dreamer.messageservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.MessageConstant;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.messageservice.entity.pojo.MessageTemplate;
import com.dreamer.messageservice.entity.pojo.UserMessage;
import com.dreamer.messageservice.mapper.MessageTemplateMapper;
import com.dreamer.messageservice.mapper.UserMessageMapper;
import com.dreamer.messageservice.service.IMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplate> implements IMessageService {

    @Autowired
    private UserMessageMapper userMessageMapper;


    @Transactional
    @Override
    public void registerMessage(MessageDto messageDto) {

        //把一个 message 类拆成两个数据库分别对应的消息模版类和用户消息类
        MessageTemplate messageTemplate = BeanUtil.copyProperties(messageDto, MessageTemplate.class);
        UserMessage userMessage = BeanUtil.copyProperties(messageDto, UserMessage.class);

        //先插入消息模板，因为要拿到 messageId
        boolean save = save(messageTemplate);
        if (!save) {
            log.error("注册成功消息模板插入失败,用户来自: {}", userMessage.getUserId());
            throw new RuntimeException("用户消息插入失败");
        }

        userMessage.setMessageId(messageTemplate.getId());
        int insert = userMessageMapper.insert(userMessage);
        if (insert == 0) {
            log.error("注册成功用户消息插入失败,用户来自: {}", userMessage.getUserId());
            throw new RuntimeException("用户消息插入失败");
        }
    }

    @Override
    public SaResult messageCount(String isRead) {

        int userId = StpUtil.getSession().getInt("userId");
        int notReadCount = userMessageMapper.messageNotReadCount(userId);
        return SaResult.ok(String.valueOf(notReadCount));
    }

    @Override
    public SaResult listMessage() {

        int userId = StpUtil.getSession().getInt("userId");
        //查询消息
        List<MessageDto> messageDtoList = userMessageMapper.listMessage(userId);

        //消息改为已读
        userMessageMapper.isRead(userId);
        return SaResult.data(messageDtoList);
    }

    @Override
    @Transactional
    public void followingMessage(FollowingRabbitDto followingRabbitDto) {

        LocalDateTime now = LocalDateTime.now();
        //插入消息模板
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setContent(followingRabbitDto.getFansUsername()+ " 关注了你");
        messageTemplate.setType(MessageConstant.FOLLOW_MESSAGE_TYPE);
        messageTemplate.setCreateTime(now);
        save(messageTemplate);

        //插入用户消息
        UserMessage userMessage = new UserMessage();
        userMessage.setCreateTime(now);
        userMessage.setUserId(followingRabbitDto.getFollowedId());
        userMessage.setMessageId(messageTemplate.getId());
        userMessageMapper.insert(userMessage);
    }


}
