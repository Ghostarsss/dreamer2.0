package com.dreamer.userservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.EXPRabbitDto;
import com.dreamer.userservice.service.IUserService;
import com.dreamer.userservice.service.IVPService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static com.dreamer.common.constant.RabbitMQConstant.USER_FOLLOWING_EXP_QUEUE;
import static com.dreamer.userservice.key.LockKey.FOLLOWING_USER_MQ_KEY;

@RestController
@RequestMapping("/vp")
@RequiredArgsConstructor
public class VPController {

    private final IUserService userService;
    private final StringRedisTemplate redisTemplate;
    private final IVPService VPService;

    @RabbitListener(queues = USER_FOLLOWING_EXP_QUEUE)
    public void followingEXP(EXPRabbitDto expRabbitDto) {

        //redis 防止重复关注刷经验
        String lockKey = FOLLOWING_USER_MQ_KEY + expRabbitDto.getUserId() + "_" + expRabbitDto.getCauseUserId();
        Boolean isIncrementEXP = redisTemplate.opsForValue().setIfAbsent(lockKey, "", 24, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(isIncrementEXP)) {
            userService.followingEXP(expRabbitDto);
        }
    }

    /**
     * 签到
     * @return
     */
    @PostMapping("/sign")
    public SaResult sign() {
        return VPService.sign();
    }

    /**
     * 判断是否已签到
     * @return
     */
    @GetMapping("/sign/status")
    public SaResult isSign() {
        return VPService.isSign();
    }

    /**
     * 质子换购用户经验
     * @param proton
     * @return
     */
    @PutMapping("/buy-exp")
    public SaResult buyEXPByProton(Integer proton) {
        return VPService.buyEXPByProton(proton);
    }

    /**
     * 消耗质子，信件可以上传图片
     * @return
     */
    @PutMapping("/letter-upload-images")
    public SaResult uploadImagesWithProtonCost(long userId) {
        return VPService.uploadImagesWithProtonCost(userId);
    }
}
