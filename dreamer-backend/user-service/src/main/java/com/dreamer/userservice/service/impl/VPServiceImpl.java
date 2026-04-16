package com.dreamer.userservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.userservice.constant.VPConstant;
import com.dreamer.userservice.key.RedisKey;
import com.dreamer.userservice.mapper.VPMapper;
import com.dreamer.userservice.message.VPMessage;
import com.dreamer.userservice.service.IVPService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.dreamer.common.constant.EXPConstant.TINY_EXP;
import static com.dreamer.userservice.key.LockKey.SIGN_KEY;
import static com.dreamer.userservice.message.VPMessage.USER_SIGN_AGAIN_MESSAGE;
import static com.dreamer.userservice.message.VPMessage.USER_SIGN_SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class VPServiceImpl extends ServiceImpl<VPMapper, User> implements IVPService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    @Override
    public SaResult sign() {

        long userId = StpUtil.getLoginIdAsLong();

        //加锁，防止重复签到
        String signLockKey = SIGN_KEY + userId;
        RLock lock = redissonClient.getLock(signLockKey);

        try {
            //尝试获取锁
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //判断当前用户是否已经签到
            LocalDate localDate = LocalDate.now();
            int year = localDate.getYear();
            int month = localDate.getMonthValue();
            int dayOfMonth = localDate.getDayOfMonth();
            int offset = dayOfMonth - 1;  //本月的日期映射到 redis 的偏移量（bitmap 是从 0 开始的，所以要-1）
            String redisKey = RedisKey.USER_SIGN_KEY + userId + ":" + year + ":" + month;
            Boolean isSign = redisTemplate.opsForValue().getBit(redisKey, offset);
            if (isSign) {
                return SaResult.error(USER_SIGN_AGAIN_MESSAGE);
            }

            //获得 质子 和 EXP
            Random random = new Random();
            int randomProton = random.nextInt(10) + 1;  //签到获得随机质子数 1～10
            boolean update = lambdaUpdate().eq(User::getId, userId)
                    .setSql("proton = proton + " + randomProton + ", exp = exp + " + TINY_EXP)
                    .update();
            if (!update) {
                return SaResult.error(SystemMessage.SYSTEM_ERROR);
            }

            //redis 插入签到数据
            redisTemplate.opsForValue().setBit(redisKey, offset, true);

            return SaResult.ok(USER_SIGN_SUCCESS_MESSAGE + randomProton + " 粒「质子」");

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public SaResult isSign() {

        long userId = StpUtil.getLoginIdAsLong();

        //查询
        LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int dayOfMonth = localDate.getDayOfMonth();
        int offset = dayOfMonth - 1;

        String redisKey = RedisKey.USER_SIGN_KEY + userId + ":" + year + ":" + month;
        Boolean isSign = redisTemplate.opsForValue().getBit(redisKey, offset);
        if (isSign) {
            return SaResult.ok(VPConstant.USER_IS_SIGNED);
        } else {
            return SaResult.ok(VPConstant.USER_NOT_SIGNED);
        }
    }

    @Override
    @Transactional
    public SaResult buyEXPByProton(Integer proton) {

        long userId = StpUtil.getLoginIdAsLong();

        //判断 proton 是否合法
        if (proton == null || proton <= 0) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }


        //购买成功，扣除质子数，增加经验值
        int incrEXP = proton * 10;
        boolean update = lambdaUpdate().eq(User::getId, userId)
                .ge(User::getProton, proton)
                .setSql("proton = proton - {0}, exp = exp + {1}", proton, incrEXP)
                .update();

        if (!update) {
            return SaResult.error(VPMessage.BALANCE_IS_INSUFFICIENT_MESSAGE);
        }
        return SaResult.ok(VPMessage.BUY_EXP_BY_PROTON_SUCCESS);
    }

    @Override
    public SaResult uploadImagesWithProtonCost(long userId) {

        //用户等级达到 20 级，扣减「原子」
        boolean update = lambdaUpdate().eq(User::getId, userId)
                .ge(User::getProton, 50)
                .ge(User::getExp,36100)
                .setSql("proton = proton - 100")
                .update();
        if (!update) {
            return SaResult.error();
        }
        return SaResult.ok();
    }
}
