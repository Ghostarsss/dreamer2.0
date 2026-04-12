package com.dreamer.authservice.service.impl;

import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.authservice.feign.UserFeignClient;
import com.dreamer.authservice.utils.MailUtil;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.authservice.mapper.RegisterMapper;
import com.dreamer.authservice.service.IRegisterService;
import com.dreamer.common.utils.AliOSSUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.dreamer.authservice.constant.EmailConstant.*;
import static com.dreamer.authservice.key.LockKey.REGISTER_KEY;
import static com.dreamer.authservice.key.RedisKey.EMAIL_REDIS_CODE_COOLDOWN_KEY;
import static com.dreamer.authservice.key.RedisKey.EMAIL_REDIS_VERIFY_CODE_KEY;
import static com.dreamer.authservice.message.CodeMessage.*;
import static com.dreamer.authservice.message.EmailMessage.*;
import static com.dreamer.common.message.SystemMessage.OPERATION_FREQUENT;
import static com.dreamer.common.message.SystemMessage.SYSTEM_ERROR;
import static com.dreamer.common.message.UserMessage.REGISTER_SUCCESS;
import static com.dreamer.common.message.UserMessage.UPLOAD_AVATAR_ERROR;

@Service
@Slf4j
public class RegisterServiceImpl extends ServiceImpl<RegisterMapper, User> implements IRegisterService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private AliOSSUtil aliOSSUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public SaResult sendVerifyCode(String email) {
        //判断 email 是否合法
        //校验邮箱是否已注册
        boolean emailIsExists = lambdaQuery()
                .eq(User::getEmail, email)
                .exists();

        if (emailIsExists) {
            return SaResult.error(EMAIL_ALREADY_REGISTERED);
        }

        //校验邮箱格式
        if (email == null || email.isBlank()) {
            return SaResult.error(EMAIL_IS_NULL);
        }

        if (!email.matches(EMAIL_REGEX)) {
            return SaResult.error(EMAIL_FORMAT_ERROR);
        }


        //redis验证码限制 5分钟 生效时间
        String key = EMAIL_REDIS_VERIFY_CODE_KEY + email;
        String cooldownKey = EMAIL_REDIS_CODE_COOLDOWN_KEY + email;  //防止前端绕过 60 秒冷却进行验证码发送

        // 生成验证码
        String code = String.valueOf(100000 + new Random().nextInt(900000));

        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey)) || Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return SaResult.error(OPERATION_FREQUENT);
        }

        redisTemplate.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);

        try {
            // 发送邮件
            mailUtil.sendMail(email, "登录验证码", "您的验证码是：" + code + "，5分钟内有效。");
            // 邮件发送成功后再写入 Redis
            redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error(EMAIL_VERIFY_CODE_SEND_ERROR);
        }

        return SaResult.ok(EMAIL_VERIFY_CODE_SEND_SUCCESS);
    }


    @Override
    public SaResult uploadAvatar(MultipartFile avatar) {

        try {
            return SaResult.ok(aliOSSUtil.addAli(avatar));
        } catch (Exception e) {
            log.error("用户上传头像失败: {}", e.getMessage());
            return SaResult.error(UPLOAD_AVATAR_ERROR);
        }

    }

    @Override
    public SaResult register(UserDto userDto) {

        log.info("用户注册: {}", userDto);

        String email = userDto.getEmail();
        String redisKey = EMAIL_REDIS_VERIFY_CODE_KEY + email;
        String lockKey = REGISTER_KEY + email;

        //加锁，防止用户重复注册
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //尝试获取锁
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);

            if (!tryLock) {
                return SaResult.error(OPERATION_FREQUENT);
            }

            //校验验证码
            String verifyCode = redisTemplate.opsForValue().get(redisKey);
            if (verifyCode == null) {
                return SaResult.error(EMAIL_VERIFY_CODE_EXCEEDED);
            }
            if (!verifyCode.equals(userDto.getVerifyCode())) {
                return SaResult.error(EMAIL_VERIFY_CODE_ERROR);
            }

            //注册用户
            //openfeign远程同步调用
            User user = BeanUtil.copyProperties(userDto, User.class);

            //springboot-security 密码加密
            String password = userDto.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String encode = bCryptPasswordEncoder.encode(password);

            user.setPassword(encode);

            userFeignClient.register(user);

            //删除 redis 的验证码和其冷却时间
            redisTemplate.delete(List.of(redisKey, EMAIL_REDIS_CODE_COOLDOWN_KEY + email));

            return SaResult.ok(REGISTER_SUCCESS);
        } catch (Exception e) {
            log.error("用户注册报错: {}", e.getMessage());
            return SaResult.error(SYSTEM_ERROR);
        } finally {
            //释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
