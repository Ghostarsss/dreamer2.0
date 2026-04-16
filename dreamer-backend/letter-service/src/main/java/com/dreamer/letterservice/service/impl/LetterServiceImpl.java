package com.dreamer.letterservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.letterservice.constant.LetterConstant;
import com.dreamer.letterservice.entity.dto.FutureLetterDto;
import com.dreamer.letterservice.entity.pojo.FutureLetter;
import com.dreamer.letterservice.feign.UserFeignClient;
import com.dreamer.letterservice.mapper.LetterMapper;
import com.dreamer.letterservice.service.ILetterService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.dreamer.letterservice.constant.LetterConstant.LETTER_IS_NOT_OPENED;
import static com.dreamer.letterservice.key.LockKey.LETTER_SUBMIT_LOCK;
import static com.dreamer.letterservice.message.LetterMessage.LETTER_SUBMIT_SUCCESS;
import static com.dreamer.letterservice.message.LetterMessage.LETTER_UPLOAD_IMAGES_ERROR;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl extends ServiceImpl<LetterMapper, FutureLetter> implements ILetterService {

    private final RedissonClient redissonClient;
    private final UserFeignClient userFeignClient;

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
    public SaResult addLetter(FutureLetterDto futureLetterDto) {

        long userId = StpUtil.getLoginIdAsLong();

        //加锁
        String lockKey = LETTER_SUBMIT_LOCK + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {

            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //查询该用户是否存在未到期信件
            boolean exists = lambdaQuery().eq(FutureLetter::getUserId, userId)
                    .gt(FutureLetter::getOpenTime, LocalDate.now())
                    .eq(FutureLetter::getIsOpen, LETTER_IS_NOT_OPENED)
                    .exists();
            if (exists) {
                return SaResult.error(SystemMessage.SYSTEM_ERROR);
            }

            //若上传图片，则耗费 50 粒「质子」（用户等级必须达到 20 级）
            if (!StrUtil.isEmpty(futureLetterDto.getImg())) {
                SaResult uploadImagesWithProtonCost = userFeignClient.uploadImagesWithProtonCost(userId);
                if (uploadImagesWithProtonCost.getCode() == 500) {
                    return SaResult.error(LETTER_UPLOAD_IMAGES_ERROR);
                }
            }

            //封装信件并保存
            FutureLetter futureLetter = BeanUtil.copyProperties(futureLetterDto, FutureLetter.class);
            futureLetter.setUserId(userId);
            futureLetter.setCreateTime(LocalDateTime.now());
            boolean save = save(futureLetter);
            if (!save) {
                return SaResult.error(SystemMessage.SYSTEM_ERROR);
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
}
