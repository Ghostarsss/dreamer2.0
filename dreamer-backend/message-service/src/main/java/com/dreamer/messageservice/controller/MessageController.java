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
import org.springframework.web.bind.annotation.*;

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
     * 管理员给指定用户发送通知
     * @return
     */
    @PostMapping("/admin/notify")
    public SaResult adminNotifyUser(@RequestBody MessageDto messageDto) {
        return messageService.adminNotifyUser(messageDto);
    }

    /**
     * 管理员添加公告
     * @return
     */
    @PostMapping("/admin/notices")
    public SaResult addNotice(@RequestBody MessageDto messageDto) {
        return messageService.addNotice(messageDto);
    }

    /**
     * 查看历史公告
     * @return
     */
    @GetMapping("/admin/notices")
     public SaResult listNotice() {
        return messageService.listNotice();
    }

    /**
     * 管理员删除公告
     * @param noticeId
     * @return
     */
    @DeleteMapping("/admin/{noticeId}")
    public SaResult removeNotice(@PathVariable Long noticeId) {
        return messageService.removeNotice(noticeId);
    }

    /**
     * 管理员受理反馈的消息通知
     * @param messageDto
     * @return
     */
    @PostMapping("/admin/reply-feedback")
    SaResult replyFeedbackNotify(@RequestBody MessageDto messageDto) {
        return messageService.replyFeedbackNotify(messageDto);
    }
}
