package com.dreamer.adminservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.adminservice.entity.dto.FeedbackDto;
import com.dreamer.adminservice.entity.pojo.Feedback;
import com.dreamer.adminservice.feign.MessageFeignClient;
import com.dreamer.adminservice.key.RedisKey;
import com.dreamer.adminservice.mapper.FeedbackMapper;
import com.dreamer.adminservice.service.IFeedbackService;
import com.dreamer.common.constant.MessageConstant;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.constant.ScrollConstant;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.common.message.UserMessage;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.dreamer.adminservice.constant.FeedbackConstant.FEEDBACK_IS_REPLY_STATUS;
import static com.dreamer.adminservice.constant.FeedbackConstant.FEEDBACK_NOT_REPLY_STATUS;
import static com.dreamer.adminservice.message.FeedbackMessage.FEEDBACK_REPLY_SUCCESS;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {

    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redissonClient;
    private final MessageFeignClient messageFeignClient;


    @Override
    public SaResult submitFeedback(FeedbackDto feedbackDto) {

        if (!StpUtil.isLogin()) {
            return SaResult.error(UserMessage.USER_NOT_LOGIN);
        }

        long userId = StpUtil.getLoginIdAsLong();

        //加锁，防止恶意重复提交
        String redisKey = RedisKey.FEEDBACK_SUBMIT_KEY + userId;
        RLock lock = redissonClient.getLock(redisKey);

        try {
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            Feedback feedback = BeanUtil.copyProperties(feedbackDto, Feedback.class);
            LocalDateTime now = LocalDateTime.now();
            feedback.setCreateTime(now);
            feedback.setUserId(userId);
            feedback.setUpdateTime(now);

            save(feedback);

            //异步通知管理员处理反馈
            MessageDto messageDto = MessageDto.builder()
                    .type(MessageConstant.FEEDBACK_MESSAGE_TYPE)
                    .sendId(userId)
                    .content("反馈已提交，管理员请尽快处理")
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConstant.FEEDBACK_SUBMIT_MESSAGE_QUEUE, messageDto);

            return SaResult.ok("反馈已提交，管理员会尽快处理");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public SaResult pageFeedback(Integer page) {

        Page<Feedback> feedbackPage = new Page<>(page, ScrollConstant.SCROLL_LIMIT);
        lambdaQuery().orderByDesc(Feedback::getCreateTime, Feedback::getId)
                .page(feedbackPage);

        return SaResult.data(feedbackPage);
    }

    @Override
    @GlobalTransactional
    public SaResult replyFeedback(Long feedbackId, String reply) {

        //处理反馈
        boolean update = lambdaUpdate().eq(Feedback::getId, feedbackId)
                .eq(Feedback::getStatus, FEEDBACK_NOT_REPLY_STATUS)
                .set(Feedback::getReply, reply)
                .set(Feedback::getStatus, FEEDBACK_IS_REPLY_STATUS)
                .set(Feedback::getUpdateTime,LocalDateTime.now())
                .update();
        if (!update) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        //远程通知用户反馈已受理
        Long userId = getById(feedbackId).getUserId();
        MessageDto messageDto = MessageDto.builder()
                .sendId(StpUtil.getLoginIdAsLong())
                .userId(userId)
                .content("「管理员」" + StpUtil.getSession().getString("username")
                        + " 受理了您的反馈："
                        + reply)
                .build();
        SaResult saResult = messageFeignClient.replyFeedbackNotify(messageDto);
        if (saResult.getCode() == 500) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        return SaResult.ok(FEEDBACK_REPLY_SUCCESS);
    }
}
