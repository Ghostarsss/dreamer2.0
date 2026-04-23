package com.dreamer.messageservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.MessageConstant;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.vo.PostVo;
import com.dreamer.common.entity.vo.UserVo;
import com.dreamer.messageservice.entity.pojo.MessageTemplate;
import com.dreamer.messageservice.entity.pojo.UserMessage;
import com.dreamer.messageservice.feign.PostFeignClient;
import com.dreamer.messageservice.feign.UserFeignClient;
import com.dreamer.messageservice.mapper.MessageTemplateMapper;
import com.dreamer.messageservice.mapper.UserMessageMapper;
import com.dreamer.messageservice.service.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplate> implements IMessageService {

    private final UserMessageMapper userMessageMapper;
    private final UserFeignClient userFeignClient;


    @Transactional
    @Override
    public void addMessage(MessageDto messageDto) {

        //把一个 message 类拆成消息模版类
        MessageTemplate messageTemplate = BeanUtil.copyProperties(messageDto, MessageTemplate.class);
        LocalDateTime now = LocalDateTime.now();
        messageTemplate.setCreateTime(now);

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
        userMessage.setUserId(messageDto.getUserId());
        userMessage.setCreateTime(now);
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

    @Override
    public void postOrCommentLikedMessage(MessageDto messageDto) {

        MessageTemplate messageTemplate = new MessageTemplate();
        LocalDateTime now = LocalDateTime.now();
        messageTemplate.setCreateTime(now);
        messageTemplate.setType(MessageConstant.LIKE_MESSAGE_TYPE);
        messageTemplate.setSendId(messageDto.getSendId());

        UserMessage userMessage = new UserMessage();
        userMessage.setCreateTime(now);

        //查询点赞者
        SaResult saResult = userFeignClient.queryUserById(messageDto.getSendId().toString());
        UserVo userVo = BeanUtil.copyProperties(saResult.getData(), UserVo.class);

        //判断点赞是文章点赞还是评论
        Long postId = messageDto.getPostId();
        if (postId != null) {
            //文章点赞
            messageTemplate.setContent(userVo.getUsername() + " 点赞了您的文章");

            //保存
            save(messageTemplate);

            //插入用户消息
            userMessage.setUserId(messageDto.getUserId());
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);
        } else {
            //评论点赞
            messageTemplate.setContent(userVo.getUsername() + "点赞了您的评论");

            //保存
            save(messageTemplate);

            //插入用户消息
            userMessage.setUserId(messageDto.getUserId());
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);
        }
    }

    @Override
    @Transactional
    public void commentedMessage(MessageDto messageDto) {

        Long postUserId = messageDto.getPostUserId();
        Long sendId = messageDto.getSendId();
        Long userId = messageDto.getUserId();
        LocalDateTime now = LocalDateTime.now();

        //远程查询评论者信息
        SaResult userSaResult = userFeignClient.queryUserById(sendId.toString());
        UserVo userVo = BeanUtil.copyProperties(userSaResult.getData(), UserVo.class);


        //封装消息表
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setSendId(sendId);
        messageTemplate.setType(MessageConstant.COMMENT_MESSAGE_TYPE);
        messageTemplate.setCreateTime(now);

        UserMessage userMessage = new UserMessage();
        userMessage.setCreateTime(now);
        userMessage.setUserId(userId);

        //判断是几级评论
        if (postUserId == null) {

            //一级评论,只用给文章作者发送通知
            messageTemplate.setContent("「梦想家」 " + userVo.getUsername() + " 评论了您的文章");
            save(messageTemplate);
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);

        } else {

            //二级评论，发送 文章作者 和 父评论作者

            messageTemplate.setContent("「梦想家」 " + userVo.getUsername() + " 评论了您");
            save(messageTemplate);
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);


            //封装对父评论用户的消息表
            MessageTemplate parentMessageTemplate = new MessageTemplate();
            parentMessageTemplate.setSendId(sendId);
            parentMessageTemplate.setType(MessageConstant.COMMENT_MESSAGE_TYPE);
            parentMessageTemplate.setCreateTime(now);

            UserMessage parentUserMessage = new UserMessage();
            parentUserMessage.setCreateTime(now);
            parentUserMessage.setUserId(postUserId);

            parentMessageTemplate.setContent("「梦想家」 " + userVo.getUsername() + " 在您的文章下评论了他人");
            save(parentMessageTemplate);
            parentUserMessage.setMessageId(parentMessageTemplate.getId());
            userMessageMapper.insert(parentUserMessage);

        }

    }

}
