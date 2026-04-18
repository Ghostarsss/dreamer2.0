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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplate> implements IMessageService {

    @Autowired
    private UserMessageMapper userMessageMapper;


    @Transactional
    @Override
    public void addMessage(MessageDto messageDto) {

        //把一个 message 类拆成消息模版类
        MessageTemplate messageTemplate = BeanUtil.copyProperties(messageDto, MessageTemplate.class);


        //先插入消息模板，因为要拿到 messageId
        boolean save = save(messageTemplate);
        if (!save) {
            log.error("消息模板插入失败");
            throw new RuntimeException("消息模板插入失败");
        }

        //如果是消息广播，则不需要插入用户消息
        if (messageDto.getIsBroadcast() == MessageConstant.IS_BROADCAST_TYPE) {
            return;
        }
        UserMessage userMessage = BeanUtil.copyProperties(messageDto, UserMessage.class);
        userMessage.setMessageId(messageTemplate.getId());
        int insert = userMessageMapper.insert(userMessage);
        if (insert == 0) {
            log.error("消息插入失败,用户来自: {}", userMessage.getUserId());
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
        List<MessageDto> userMessageDtoList = userMessageMapper.listMessage(userId);
        List<MessageTemplate> list = lambdaQuery()
                .eq(MessageTemplate::getIsBroadcast, MessageConstant.IS_BROADCAST_TYPE)
                .list();
        List<MessageDto> broadcastMessageDtoList = BeanUtil.copyToList(list, MessageDto.class);

        //用户消息和广播消息排序
        List<MessageDto> messageDtoList = Stream.concat(userMessageDtoList.stream(), broadcastMessageDtoList.stream())
                .sorted(Comparator.comparing(MessageDto::getCreateTime).reversed())
                .toList();

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
        messageTemplate.setContent(followingRabbitDto.getFansUsername() + " 关注了你");
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
