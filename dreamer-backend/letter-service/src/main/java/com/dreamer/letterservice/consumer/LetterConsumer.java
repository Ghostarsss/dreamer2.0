package com.dreamer.letterservice.consumer;

import com.dreamer.letterservice.service.ILetterService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dreamer.common.constant.RabbitMQConstant.ADMIN_DELETE_USER_LETTER_QUEUE;

@Component
public class LetterConsumer {

    @Autowired
    private ILetterService letterService;

    /**
     * 根据 userId 删除相关信件
     * @param userId
     */
    @RabbitListener(queues = ADMIN_DELETE_USER_LETTER_QUEUE)
    public void delLetterByUserId(Long userId) {
        letterService.delLetterByUserId(userId);
    }

}
