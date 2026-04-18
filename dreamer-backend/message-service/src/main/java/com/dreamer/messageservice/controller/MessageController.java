package com.dreamer.messageservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.messageservice.service.IMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

import static com.dreamer.common.constant.RabbitMQConstant.USER_FOLLOWING_MESSAGE_QUEUE;
import static com.dreamer.messageservice.key.lockKey.FOLLOWING_MESSAGE_KEY;

@RestController
@Slf4j
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private IMessageService messageService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 插入消息通知
     * @param messageDto
     */
    @RabbitListener(queues = {RabbitMQConstant.AUTH_REGISTER_MESSAGE_QUEUE,RabbitMQConstant.LETTER_TO_BE_OPENED_QUEUE})
    public void addMessage(MessageDto messageDto) {
        log.info("收到消息: {}", messageDto);
        messageService.addMessage(messageDto);
    }

    /**
     * 查询该用户未读消息数
     * @param isRead
     * @return
     */
    @GetMapping("/count")
    public SaResult messageCount(@RequestParam("isRead") String isRead) {
        return messageService.messageCount(isRead);
    }

    /**
     * 查询消息
     * @return
     */
    @GetMapping
    public SaResult listMessage() {
        return messageService.listMessage();
    }

    /**
     * 被关注通知
     * @param followingRabbitDto
     */
    @RabbitListener(queues = USER_FOLLOWING_MESSAGE_QUEUE)
    public void followingMessage(FollowingRabbitDto followingRabbitDto) {

        //使用 redis 防止重复关注推送信息
        String lock = FOLLOWING_MESSAGE_KEY + followingRabbitDto.getFollowedId() + "_" + followingRabbitDto.getFansId();
        Boolean isSendFollowingMessage = redisTemplate.opsForValue().setIfAbsent(lock, "", 24, TimeUnit.HOURS);

        if (Boolean.TRUE.equals(isSendFollowingMessage)) {
            messageService.followingMessage(followingRabbitDto);
        }
    }

}
