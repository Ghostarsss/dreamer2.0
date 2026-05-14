package com.dreamer.messageservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.MessageConstant;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.vo.UserVo;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.messageservice.entity.pojo.MessageTemplate;
import com.dreamer.messageservice.entity.pojo.UserMessage;
import com.dreamer.messageservice.entity.vo.MessageVo;
import com.dreamer.messageservice.feign.UserFeignClient;
import com.dreamer.messageservice.mapper.MessageTemplateMapper;
import com.dreamer.messageservice.mapper.UserMessageMapper;
import com.dreamer.messageservice.service.IMessageService;
import com.dreamer.messageservice.service.IUserMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dreamer.common.constant.MessageConstant.*;
import static com.dreamer.messageservice.message.MessageMessage.NOTIFY_SEND_ERROR;
import static com.dreamer.messageservice.message.MessageMessage.NOTIFY_SEND_SUCCESS;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageTemplateMapper, MessageTemplate> implements IMessageService {

    private final IUserMessageService userMessageService;
    private final UserFeignClient userFeignClient;
    private final UserMessageMapper userMessageMapper;


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
        userMessage.setCreateTime(now);

        //反馈服务的接收者是全体管理员
        if (Objects.equals(messageDto.getType(), MessageConstant.FEEDBACK_MESSAGE_TYPE)) {

            //远程查询所有管理员并插入
            SaResult saResult = userFeignClient.listAdmin();
            List<Long> adminUserIds = BeanUtil.copyToList((List<?>) saResult.getData(), Long.class);
            ArrayList<UserMessage> userMessages = new ArrayList<>(adminUserIds.size());
            adminUserIds.forEach(u -> {
                UserMessage userMessage2 = BeanUtil.copyProperties(userMessage, UserMessage.class);
                userMessage2.setUserId(u);
                userMessages.add(userMessage2);
            });

            userMessageService.saveBatch(userMessages);


        }

        userMessage.setUserId(messageDto.getUserId());
        userMessageService.save(userMessage);

    }

    @Override
    public SaResult messageCount() {

        long userId = StpUtil.getLoginIdAsLong();
        int notReadCount = userMessageMapper.messageNotReadCount(userId);
        return SaResult.ok(String.valueOf(notReadCount));
    }

    @Override
    public SaResult listMessage() {

        long userId = StpUtil.getLoginIdAsLong();
        //查询消息
        List<MessageVo> userMessageDtoList = userMessageMapper.listMessage(userId);
        List<MessageTemplate> list = lambdaQuery()
                .eq(MessageTemplate::getIsBroadcast, MessageConstant.IS_BROADCAST_TYPE)
                .list();
        List<MessageVo> broadcastMessageDtoList = BeanUtil.copyToList(list, MessageVo.class);

        //用户消息和广播消息排序
        List<MessageVo> messageVoList = Stream.concat(userMessageDtoList.stream(), broadcastMessageDtoList.stream())
                .sorted(Comparator.comparing(MessageVo::getCreateTime).reversed())
                .toList();

        //远程批量查询消息用户
        List<String> userIds = messageVoList.stream().map(MessageVo::getSendId).filter(Objects::nonNull).distinct().toList();
        SaResult saResult = userFeignClient.batchQueryUserByUserId(userIds);
        List<UserVo> userVos = BeanUtil.copyToList((List<?>) saResult.getData(), UserVo.class);
        Map<String, UserVo> collect = userVos.stream().collect(Collectors.toMap(UserVo::getId, u -> u));

        //封装 message 返回前端
        for (MessageVo messageVo : messageVoList) {
            String sendId = messageVo.getSendId();
            if (sendId != null) {
                UserVo userVo = collect.get(sendId);
                if (userVo != null) {
                    messageVo.setAvatar(userVo.getAvatar());
                    messageVo.setUsername(userVo.getUsername());
                    messageVo.setLevel(userVo.getLevel());
                } else {
                    messageVo.setUsername("该用户被封禁");
                }
            }
        }

        //消息改为已读
        userMessageMapper.isRead(userId);
        return SaResult.data(messageVoList);
    }

    @Override
    @Transactional
    public void followingMessage(FollowingRabbitDto followingRabbitDto) {

        LocalDateTime now = LocalDateTime.now();
        //插入消息模板
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setContent("「" + followingRabbitDto.getFansUsername() + "」关注了你");
        messageTemplate.setType(MessageConstant.FOLLOW_MESSAGE_TYPE);
        messageTemplate.setSendId(followingRabbitDto.getFansId());
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
            messageTemplate.setContent("「" + userVo.getUsername() + "」点赞了您的文章");

            //保存
            save(messageTemplate);

            //插入用户消息
            userMessage.setUserId(messageDto.getUserId());
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);
        } else {
            //评论点赞
            messageTemplate.setContent("「" + userVo.getUsername() + "」点赞了您的评论");

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
            messageTemplate.setContent("「" + userVo.getUsername() + "」评论了您的文章:「" + messageDto.getContent() + "」");
            save(messageTemplate);
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);

        } else {

            //二级评论，发送 文章作者 和 父评论作者

            messageTemplate.setContent("「" + userVo.getUsername() + "」评论了您:「" + messageDto.getContent() + "」");
            save(messageTemplate);
            userMessage.setMessageId(messageTemplate.getId());
            userMessageMapper.insert(userMessage);


            //封装对父评论用户的消息表
            //如果是自己的文章下就不发
            if (messageDto.getPostUserId() == messageDto.getUserId()) {
                MessageTemplate parentMessageTemplate = new MessageTemplate();
                parentMessageTemplate.setSendId(sendId);
                parentMessageTemplate.setType(MessageConstant.COMMENT_MESSAGE_TYPE);
                parentMessageTemplate.setCreateTime(now);

                UserMessage parentUserMessage = new UserMessage();
                parentUserMessage.setCreateTime(now);
                parentUserMessage.setUserId(postUserId);

                parentMessageTemplate.setContent("「" + userVo.getUsername() + "」在您的文章下评论了他人" + messageDto.getContent());
                save(parentMessageTemplate);
                parentUserMessage.setMessageId(parentMessageTemplate.getId());
                userMessageMapper.insert(parentUserMessage);
            }
        }

    }

    @Override
    @Transactional
    public SaResult adminNotifyUser(MessageDto messageDto) {

        LocalDateTime now = LocalDateTime.now();
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setCreateTime(now);
        messageTemplate.setType(ADMIN_NOTIFY_MESSAGE_TYPE);
        messageTemplate.setSendId(messageDto.getUserId());
        messageTemplate.setContent(messageDto.getContent());

        save(messageTemplate);

        UserMessage userMessage = new UserMessage();
        userMessage.setCreateTime(now);
        userMessage.setMessageId(messageTemplate.getId());
        userMessage.setUserId(messageDto.getSendId());

        boolean saveUserMessage = userMessageService.save(userMessage);
        if (!saveUserMessage) {
            log.error("管理员通知保存失败 ：{}", messageDto);
            return SaResult.error(NOTIFY_SEND_ERROR);
        }

        return SaResult.ok(NOTIFY_SEND_SUCCESS);
    }

    @Override
    public SaResult addNotice(MessageDto messageDto) {

        MessageTemplate messageTemplate = BeanUtil.copyProperties(messageDto, MessageTemplate.class);
        messageTemplate.setType(NOTIFY_MESSAGE_TYPE);
        messageTemplate.setIsBroadcast(IS_BROADCAST_TYPE);
        messageTemplate.setCreateTime(LocalDateTime.now());

        boolean save = save(messageTemplate);
        if (!save) {
            log.error("管理员发布公告失败：{}", messageTemplate);
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        return SaResult.ok();
    }

    @Override
    public SaResult listNotice() {

        List<MessageTemplate> messageTemplates = lambdaQuery().eq(MessageTemplate::getIsBroadcast, IS_BROADCAST_TYPE)
                .orderByDesc(MessageTemplate::getCreateTime, MessageTemplate::getId)
                .list();

        return SaResult.data(messageTemplates);
    }

    @Override
    public SaResult removeNotice(Long noticeId) {

        boolean remove = removeById(noticeId);
        if (!remove) {
            return SaResult.error();
        }

        return SaResult.ok();
    }

    @Override
    public SaResult replyFeedbackNotify(MessageDto messageDto) {

        LocalDateTime now = LocalDateTime.now();
        MessageTemplate messageTemplate = BeanUtil.copyProperties(messageDto, MessageTemplate.class);
        messageTemplate.setCreateTime(now);
        messageTemplate.setType(FEEDBACK_MESSAGE_TYPE);

        boolean save = save(messageTemplate);
        if (!save) {
            return SaResult.error();
        }

        UserMessage userMessage = new UserMessage();
        userMessage.setCreateTime(now);
        userMessage.setMessageId(messageTemplate.getId());
        userMessage.setUserId(messageDto.getUserId());

        int saveUserMessage = userMessageMapper.insert(userMessage);
        if (saveUserMessage == 0) {
            log.error("管理员通知保存失败 ：{}", messageDto);
            return SaResult.error();
        }

        return SaResult.ok();
    }

    @Override
    public SaResult clearMessage() {

        long userId = StpUtil.getLoginIdAsLong();
        userMessageMapper.delete(new LambdaUpdateWrapper<UserMessage>().eq(UserMessage::getUserId, userId));

        return SaResult.ok("消息已清空");
    }


}
