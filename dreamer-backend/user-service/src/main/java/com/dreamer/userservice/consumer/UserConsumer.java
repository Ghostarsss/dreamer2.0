package com.dreamer.userservice.consumer;

import com.dreamer.userservice.service.IFollowingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.dreamer.common.constant.RabbitMQConstant.ADMIN_DELETE_USER_FOLLOWING_QUEUE;

@Component
public class UserConsumer {

    @Autowired
    private IFollowingService followingService;

    @RabbitListener(queues = ADMIN_DELETE_USER_FOLLOWING_QUEUE)
    public void removeFollowingAndFansByUserId(Long userId) {
        followingService.removeFollowingAndFansByUserId(userId);
    }

}
