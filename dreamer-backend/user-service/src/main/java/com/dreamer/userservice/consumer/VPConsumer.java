package com.dreamer.userservice.consumer;

import com.dreamer.common.entity.dto.EXPRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.userservice.service.IVPService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.dreamer.common.constant.RabbitMQConstant.*;
import static com.dreamer.userservice.key.LockKey.FOLLOWING_USER_MQ_KEY;
import static com.dreamer.userservice.key.LockKey.LIKE_INCREMENT_EXP_KEY;

@Component
@RequiredArgsConstructor
public class VPConsumer {
    private final StringRedisTemplate redisTemplate;
    private final IVPService vpService;

    /**
     * 被关注后获得经验
     * @param expRabbitDto
     */
    @RabbitListener(queues = USER_FOLLOWING_EXP_QUEUE)
    public void followingEXP(EXPRabbitDto expRabbitDto) {

        //redis 防止重复关注刷经验
        String lockKey = FOLLOWING_USER_MQ_KEY + expRabbitDto.getUserId() + "_" + expRabbitDto.getCauseUserId();
        Boolean isIncrementEXP = redisTemplate.opsForValue().setIfAbsent(lockKey, "", 7, TimeUnit.DAYS);
        if (Boolean.TRUE.equals(isIncrementEXP)) {
            vpService.followingEXP(expRabbitDto);
        }
    }

    /**
     * 文章被点赞后获得经验
     */
    @RabbitListener(queues = POST_LIKE_INCREMENT_EXP_QUEUE)
    public void postLikedEXP(MessageDto messageDto) {

        //redis 防止重复关注刷经验
        String lockKey = LIKE_INCREMENT_EXP_KEY + messageDto.getSendId() + "_" + messageDto.getPostId();
        Boolean isIncrementEXP = redisTemplate.opsForValue().setIfAbsent(lockKey, "", 7, TimeUnit.DAYS);
        if (Boolean.TRUE.equals(isIncrementEXP)) {
            vpService.postLikedEXP(messageDto);
        }
    }

    /**
     * 文章审核通过获取经验
     * @param messageDto
     */
    @RabbitListener(queues = POST_PASS_EXP_INCREMENT_QUEUE)
    public void postPassEXP(MessageDto messageDto) {
        vpService.postPassEXP(messageDto);
    }
}
