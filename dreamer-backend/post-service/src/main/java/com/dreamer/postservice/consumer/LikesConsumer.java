package com.dreamer.postservice.consumer;

import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.postservice.service.ILikesService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LikesConsumer {

    @Autowired
    private ILikesService likesService;

    @RabbitListener(queues = RabbitMQConstant.POST_LIKE_DB_SYNC_QUEUE)
    public void likePostDbSync(MessageDto messageDto) {
        likesService.likePostDbSync(messageDto);
    }

}
