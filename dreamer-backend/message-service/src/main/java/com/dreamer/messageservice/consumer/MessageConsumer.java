package com.dreamer.messageservice.consumer;

import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.messageservice.service.IMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.dreamer.common.constant.RabbitMQConstant.*;
import static com.dreamer.messageservice.key.lockKey.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final IMessageService messageService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 插入消息通知
     *
     * @param messageDto
     */
    @RabbitListener(queues = {AUTH_REGISTER_MESSAGE_QUEUE, LETTER_TO_BE_OPENED_QUEUE
            , POST_TIP_PROTON_MESSAGE_QUEUE, FEEDBACK_SUBMIT_MESSAGE_QUEUE
            , ADMIN_POST_CHECK_MESSAGE_QUEUE})
    public void addMessage(MessageDto messageDto) {
        log.info("收到消息: {}", messageDto);
        messageService.addMessage(messageDto);
    }

    /**
     * 被关注通知
     *
     * @param followingRabbitDto
     */
    @RabbitListener(queues = USER_FOLLOWING_MESSAGE_QUEUE)
    public void followingMessage(FollowingRabbitDto followingRabbitDto) {

        //使用 redis 防止重复关注推送信息
        String lock = FOLLOWING_MESSAGE_KEY + followingRabbitDto.getFollowedId() + "_" + followingRabbitDto.getFansId();
        Boolean isSendFollowingMessage = redisTemplate.opsForValue().setIfAbsent(lock, "", 7, TimeUnit.DAYS);

        if (Boolean.TRUE.equals(isSendFollowingMessage)) {
            messageService.followingMessage(followingRabbitDto);
        }
    }

    /**
     * 被点赞通知
     */
    @RabbitListener(queues = POST_LIKE_MESSAGE_QUEUE)
    public void postOrCommentLikedMessage(MessageDto messageDto) {

        //使用 redis 防止重复点赞推送信息
        long lockKey = messageDto.getCommentId();
        if (messageDto.getPostId() != null) {
            //如果是对文章的评论，则加文章的 lock
            lockKey = messageDto.getPostId();
        }

        String lock = POST_COMMENT_LIKED_MESSAGE_KEY + messageDto.getSendId() + "_" + lockKey;

        Boolean isSendLikedMessage = redisTemplate.opsForValue().setIfAbsent(lock, "", 7, TimeUnit.DAYS);

        if (Boolean.TRUE.equals(isSendLikedMessage)) {
            messageService.postOrCommentLikedMessage(messageDto);
        }
    }

    /**
     * 被评论通知
     */
    @RabbitListener(queues = POST_COMMENT_MESSAGE_QUEUE)
    public void commentedMessage(MessageDto messageDto) {

        //使用 redis 防止重复评论推送信息
//        String lock = POST_COMMENT_MESSAGE_KEY + messageDto.getSendId() + "_" + messageDto.getPostId();
//        Boolean isSendCommentedMessage = redisTemplate.opsForValue().setIfAbsent(lock, "", 1, TimeUnit.MINUTES);

//        if (Boolean.TRUE.equals(isSendCommentedMessage)) {
            messageService.commentedMessage(messageDto);
//        }
    }
}
