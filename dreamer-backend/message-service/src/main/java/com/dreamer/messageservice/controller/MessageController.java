package com.dreamer.messageservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.messageservice.service.IMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    /**
     * 注册成功通知
     * @param messageDto
     */
    @RabbitListener(queues = "auth.register.message.queue")
    public void registerMessage(MessageDto messageDto) {
        log.info("收到注册消息: {}", messageDto);
        messageService.registerMessage(messageDto);
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

    @GetMapping
    public SaResult listMessage() {
        return messageService.listMessage();
    }
}
