package com.dreamer.letterservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.letterservice.constant.LetterConstant;
import com.dreamer.letterservice.entity.dto.FutureLetterDto;
import com.dreamer.letterservice.entity.pojo.FutureLetter;
import com.dreamer.letterservice.feign.UserFeignClient;
import com.dreamer.letterservice.mapper.LetterMapper;
import com.dreamer.letterservice.service.ILetterService;
import com.dreamer.letterservice.utils.AliOSSUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.dreamer.common.constant.RabbitMQConstant.DELAY_EXCHANGE_NAME;
import static com.dreamer.common.constant.RabbitMQConstant.LETTER_TO_BE_OPENED_KEY;
import static com.dreamer.letterservice.constant.LetterConstant.*;
import static com.dreamer.letterservice.key.LockKey.LETTER_SUBMIT_LOCK;
import static com.dreamer.letterservice.message.LetterMessage.LETTER_SUBMIT_SUCCESS;
import static com.dreamer.letterservice.message.LetterMessage.LETTER_UPLOAD_IMAGES_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class LetterServiceImpl extends ServiceImpl<LetterMapper, FutureLetter> implements ILetterService {

    private final RedissonClient redissonClient;
    private final UserFeignClient userFeignClient;
    private final AliOSSUtil aliOSSUtil;
    private final RabbitTemplate rabbitTemplate;
    private final LetterMapper letterMapper;

    @Override
    public SaResult queryOpenLettersByUserId(String userId) {

        //查询公开信件
        List<FutureLetter> letters = lambdaQuery().eq(FutureLetter::getUserId, userId)
                .eq(FutureLetter::getIsOpen, LetterConstant.LETTER_IS_OPENED)
                .eq(FutureLetter::getIsPublic, LetterConstant.LETTER_IS_PUBLIC)
                .list();

        return SaResult.data(letters);
    }

    @Override
    @Transactional
    public SaResult addLetter(FutureLetterDto futureLetterDto) {

        long userId = StpUtil.getLoginIdAsLong();
        log.info("用户:{} 添加了新信件: {}", userId, futureLetterDto);

        //开启时间必须合法
        LocalDate localDateNow = LocalDate.now();
        if (futureLetterDto.getOpenTime().isBefore(localDateNow)) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        //加锁
        String lockKey = LETTER_SUBMIT_LOCK + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {

            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //封装信件并保存
            LocalDateTime now = LocalDateTime.now();
            FutureLetter futureLetter = BeanUtil.copyProperties(futureLetterDto, FutureLetter.class);
            futureLetter.setUserId(userId);
            futureLetter.setCreateTime(now);
            boolean save = save(futureLetter);
            if (!save) {
                return SaResult.error(SystemMessage.SYSTEM_ERROR);
            }


            //计算信件开启需要的计时时间戳(超过 1 年不提供消息推送)，并使用rabbitMQ 异步通信计时开信时间
            LocalDate openTime = futureLetterDto.getOpenTime();
            boolean isBefore = openTime.isBefore(localDateNow.plusYears(1));
            if (isBefore) {
                long openedMilli = LocalDateTimeUtil.toEpochMilli(openTime);
                long nowMilli = LocalDateTimeUtil.toEpochMilli(localDateNow);
                long delayMilli = openedMilli - nowMilli;
                if (delayMilli <= 0) {
                    return SaResult.error(SystemMessage.SYSTEM_ERROR);
                }

                MessageDto messageDto = MessageDto.builder()
                        .type(9)
                        .createTime(LocalDateTime.now())
                        .content("叮～ 您有一封来自过去的信件，记得去查看哟～")
                        .sendId(userId)
                        .userId(userId)
                        .build();
                rabbitTemplate.convertAndSend(DELAY_EXCHANGE_NAME, LETTER_TO_BE_OPENED_KEY, messageDto,
                        message -> {
                            message.getMessageProperties().setHeader("x-delay", delayMilli);
                            return message;
                        });
            }

            return SaResult.ok(LETTER_SUBMIT_SUCCESS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public SaResult uploadImages(MultipartFile img) {

        long userId = StpUtil.getLoginIdAsLong();
        log.info("用户 {} 尝试信封上传图片", userId);

        //扣质子
        SaResult saResult = userFeignClient.uploadImagesWithProtonCost(userId);
        if (saResult.getCode() != 200) {
            return SaResult.error(LETTER_UPLOAD_IMAGES_ERROR);
        }

        try {
            String url = aliOSSUtil.addAli(img);
            return SaResult.ok(url);
        } catch (IOException e) {
            log.error("信封图片上传失败 : {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public SaResult removeLetter(Long letterId) {
        long userId = StpUtil.getLoginIdAsLong();

        FutureLetter letter = getById(letterId);
        //删除信件
        boolean remove = lambdaUpdate().eq(FutureLetter::getUserId, userId)
                .eq(FutureLetter::getIsOpen, LetterConstant.LETTER_IS_OPENED)
                .eq(FutureLetter::getId, letterId)
                .remove();
        if (!remove) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        //阿里云删除图片
        if (!letter.getImg().isEmpty()) {
            aliOSSUtil.removeAli(letter.getImg());
        }

        return SaResult.ok("删除成功");
    }

    @Override
    public SaResult unopenedLetterIsExist() {

        long userId = StpUtil.getLoginIdAsLong();

        boolean exists = lambdaQuery().eq(FutureLetter::getUserId, userId)
                .eq(FutureLetter::getIsOpen, LETTER_IS_NOT_OPENED)
                .le(FutureLetter::getOpenTime,LocalDate.now())
                .exists();
        if (exists) {
            return SaResult.ok(LETTER_UNOPENED_EXIST);
        }
        return SaResult.ok(LETTER_UNOPENED_NOT_EXIST);
    }

    @Override
    public SaResult queryLetterToBeOpened() {

        long userId = StpUtil.getLoginIdAsLong();

        List<FutureLetter> letters = lambdaQuery().eq(FutureLetter::getUserId, userId)
                .le(FutureLetter::getOpenTime, LocalDate.now())
                .eq(FutureLetter::getIsOpen, LETTER_IS_NOT_OPENED)
                .list();

        //修改信件为已读
        lambdaUpdate().eq(FutureLetter::getUserId, userId)
                .le(FutureLetter::getOpenTime, LocalDate.now())
                .set(FutureLetter::getIsOpen, LETTER_IS_OPENED)
                .update();

        return SaResult.data(letters);
    }

    @Override
    public SaResult listOpenedLetters(Integer page, Integer size) {

        //mybatisplus 实现分页查询业务
        LambdaQueryWrapper<FutureLetter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FutureLetter::getIsOpen, LETTER_IS_OPENED)
                .orderByDesc(FutureLetter::getOpenTime);

        Page<FutureLetter> futureLetterPage = new Page<>(page, size);

        Page<FutureLetter> pageResult = letterMapper.selectPage(futureLetterPage, wrapper);

        return SaResult.data(pageResult);
    }

    @Override
    public void delLetterByUserId(Long userId) {

        lambdaUpdate().eq(FutureLetter::getUserId, userId)
                .remove();

    }

    @Override
    public Long countLetters() {

        return count();
    }

}
