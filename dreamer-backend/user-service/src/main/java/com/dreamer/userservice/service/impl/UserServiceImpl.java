package com.dreamer.userservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.constant.ScrollConstant;
import com.dreamer.common.entity.base.BaseEntity;
import com.dreamer.common.entity.dto.LoginDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.dto.UserDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.common.utils.PasswordUtil;
import com.dreamer.common.entity.vo.UserVo;
import com.dreamer.userservice.entity.pojo.UserFollow;
import com.dreamer.userservice.feign.LetterFeignClient;
import com.dreamer.userservice.key.LockKey;
import com.dreamer.userservice.mapper.UserMapper;
import com.dreamer.userservice.service.IFollowingService;
import com.dreamer.userservice.service.IUserService;
import com.dreamer.userservice.service.IVPService;
import com.dreamer.userservice.utils.AliOSSUtil;
import com.dreamer.userservice.utils.LevelUtil;
import com.dreamer.userservice.utils.MailUtil;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.dreamer.common.constant.MessageConstant.REGISTER_MESSAGE_TYPE;
import static com.dreamer.common.constant.UserConstant.*;
import static com.dreamer.common.message.CodeMessage.*;
import static com.dreamer.common.message.SystemMessage.OPERATION_FREQUENT;
import static com.dreamer.common.message.SystemMessage.SYSTEM_ERROR;
import static com.dreamer.common.message.UserMessage.*;
import static com.dreamer.userservice.key.RedisKey.EMAIL_REDIS_CODE_COOLDOWN_KEY;
import static com.dreamer.userservice.key.RedisKey.EMAIL_REDIS_VERIFY_CODE_KEY;
import static com.dreamer.userservice.message.RegisterMessage.PASSWORD_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final RabbitTemplate rabbitTemplate;
    private final AliOSSUtil aliOSSUtil;
    private final StringRedisTemplate redisTemplate;
    private final MailUtil mailUtil;
    private final RedissonClient redissonClient;
    private final UserMapper userMapper;
    private final LetterFeignClient letterFeignClient;
    private final IVPService vpService;

    @Autowired
    @Lazy
    private IFollowingService followingService;

    @Override
    public SaResult register(User user) {

        log.info("注册用户：{}", user);
        //注册用户
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setBio(DEFAULT_BIO);
        save(user);

        Long userId = user.getId();

        //rabbit 发送注册成功消息
        //封装通知内容
        MessageDto messageDto = MessageDto.builder().createTime(now).content("亲爱的用户 " + user.getUsername() + " 欢迎您来到 dreamer 大家庭，这里有许多有趣的功能等待您探索🎉").type(REGISTER_MESSAGE_TYPE).userId(userId).build();

        rabbitTemplate.convertAndSend(RabbitMQConstant.AUTH_REGISTER_MESSAGE_QUEUE, messageDto);

        //登录
        StpUtil.login(userId);
        //缓存 user 对象信息
        StpUtil.getSession().set("userId", userId);
        StpUtil.getSession().set("role", REGULAR_USER_ROLE_INTEGER);
        return SaResult.ok(StpUtil.getTokenValue());
    }

    @Override
    public SaResult login(LoginDto loginDto) {

        String email = loginDto.getEmail();
        String password = loginDto.getPassword();

        //获取用户信息，判断用户是否存在
        User user = query().eq("email", email).one();

        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        //用户是否被封禁
        if (user.getStatus().equals(USER_IS_BANNED)) {
            return SaResult.error(USER_IS_BANNED_MESSAGE);
        }

        //密码校验
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(password, user.getPassword());
        if (!matches) {
            return SaResult.error(PASSWORD_ERROR);
        }


        Long userId = user.getId();
        //先令之前的 token 失效，否则多个账号会同时登录
        StpUtil.logout(userId);
        //登录成功
        StpUtil.login(userId);
        //缓存 user 对象信息
        StpUtil.getSession().set("userId", userId);
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("role", user.getRole());
        return SaResult.ok(StpUtil.getTokenValue());
    }

    @Override
    public SaResult getMe() {

        long userId = StpUtil.getLoginIdAsLong();
        User user = getById(userId);

        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        UserVo userVo = BeanUtil.copyProperties(user, UserVo.class);

        //计算用户 level 等级
        Integer exp = user.getExp();
        int level = LevelUtil.calculateLevel(exp);
        userVo.setLevel(level);

        //查询用户关注数和被关注数
        long fansCount = followingService.count(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getFollowedUserId, userId));
        long followingCount = followingService.count(new LambdaQueryWrapper<UserFollow>().eq(UserFollow::getUserId, userId));
        userVo.setFollowingCount(followingCount);
        userVo.setFansCount(fansCount);

        //添加网站总浏览量
        redisTemplate.opsForValue().increment("statistics:view", 1);
        return SaResult.data(userVo);
    }

    @Override
    public SaResult updateAvatars(MultipartFile avatar) {

        //为防止恶意上传头像，每次修改头像需要耗费 10颗质子
        long userId = StpUtil.getLoginIdAsLong();

        SaResult saResult = vpService.deductProton(userId, 10);
        if (saResult.getCode() != 200) {
            return SaResult.error(saResult.getMsg());
        }

        try {
            String avatarUrl = aliOSSUtil.addAli(avatar);

            //修改用户头像数据库
            boolean update = lambdaUpdate().set(User::getAvatar, avatarUrl).eq(User::getId, userId).update();

            if (update) {
                log.error("用户id: {} ,头像更新失败", userId);
            }

            return SaResult.ok(avatarUrl);
        } catch (Exception e) {
            log.error("用户上传头像失败: {}", e.getMessage());
            return SaResult.error(UPLOAD_AVATAR_ERROR);
        }
    }

    @Override
    public SaResult sendVerifyCode() {
        User user = getById(StpUtil.getSession().getLong("userId"));

        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        String email = user.getEmail();
        log.info("email: {} 请求验证码，修改用户信息", email);

        //redis验证码限制 5分钟 生效时间
        String key = EMAIL_REDIS_VERIFY_CODE_KEY + email;
        String cooldownKey = EMAIL_REDIS_CODE_COOLDOWN_KEY + email;  //防止前端绕过 60 秒冷却进行验证码发送

        //加锁
        String lockKey = LockKey.UPDATE_USER_KEY + email;
        RLock lock = redissonClient.getLock(lockKey);


        try {
            //尝试获取锁
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(OPERATION_FREQUENT);
            }

            // 生成验证码
            String code = String.valueOf(100000 + new Random().nextInt(900000));

            if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey)) && Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return SaResult.error(OPERATION_FREQUENT);
            }

            redisTemplate.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);

            try {
                // 发送邮件
                mailUtil.sendMail(email, "修改账号验证码", "您的验证码是：" + code + "，5分钟内有效。");
                // 邮件发送成功后再写入 Redis
                redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
                return SaResult.error(EMAIL_VERIFY_CODE_SEND_ERROR);
            }

            return SaResult.ok(EMAIL_VERIFY_CODE_SEND_SUCCESS);
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
    public SaResult updateMe(UserDto userDto) {

        Long userId = StpUtil.getSession().getLong("userId");
        User user = getById(userId);
        String email = user.getEmail();
        String password = userDto.getPassword();
        log.info("用户数据更新: {}", userDto);

        User updateUser = BeanUtil.copyProperties(userDto, User.class);
        updateUser.setId(userId);
        updateUser.setUpdateTime(LocalDateTime.now());
        updateUser.setPassword(null);

        //如果有密码需要修改
        if (!userDto.getPassword().isEmpty()) {
            //校验验证码

            String verifyCode = userDto.getVerifyCode();
            String redisKey = EMAIL_REDIS_VERIFY_CODE_KEY + email;

            String code = redisTemplate.opsForValue().get(redisKey);
            if (StrUtil.isBlank(code)) {
                return SaResult.error(EMAIL_VERIFY_CODE_EXCEEDED);
            }
            if (!verifyCode.equals(code)) {
                return SaResult.error(EMAIL_VERIFY_CODE_ERROR);
            }

            //清除验证码和冷却时间
            String cooldownKey = EMAIL_REDIS_CODE_COOLDOWN_KEY + email;
            redisTemplate.delete(List.of(redisKey, cooldownKey));

            //判断 dto 数据是否合法

            if (StrUtil.isBlank(userDto.getAvatar()) || (!PasswordUtil.isValidPassword(password)) || StrUtil.isBlank(userDto.getUsername()) || (!userDto.getEmail().equals(email))) {
                return SaResult.error(USER_FORMAT_ERROR);
            }

            //加密密码
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String encode = bCryptPasswordEncoder.encode(password);
            updateUser.setPassword(encode);

            //退出登录
            StpUtil.logout(userId);
        }


        //更新用户数据
        if (StrUtil.isBlank(updateUser.getBio())) {
            updateUser.setBio(DEFAULT_BIO);
        }
        int updated = userMapper.updateById(updateUser);
        if (updated == 0) {
            log.error("用户信息更新失败: {}", updateUser);
            return SaResult.error(SYSTEM_ERROR);
        }

        return SaResult.ok(USER_UPDATE_SUCCESS);
    }

    @Override
    public SaResult queryUserById(Long userId) {

        User user = getById(userId);
        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }
        UserVo userVo = BeanUtil.copyProperties(user, UserVo.class);

        //计算用户等级
        int level = LevelUtil.calculateLevel(user.getExp());
        userVo.setLevel(level);

        return SaResult.data(userVo);
    }

    @Override
    public SaResult queryOpenLettersByUserId(String userId) {

        //查询用户是否存在和封禁
        User user = getById(userId);
        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }
        if (user.getStatus().equals(USER_IS_BANNED)) {
            return SaResult.error(USER_IS_BANNED_MESSAGE);
        }

        //openfeign 远程调用信封服务，查询信件
        return letterFeignClient.queryOpenLettersByUserId(userId);
    }

    @Override
    public SaResult isBaned(long userId) {

        User user = lambdaQuery().eq(User::getId, userId)
                .select(User::getStatus)
                .one();

        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        return SaResult.data(user.getId());
    }

    @Override
    public SaResult batchQueryUserByUserId(List<String> userIds) {

        if (userIds == null) {
            return SaResult.error();
        }

        //批量查所有用户
        String join = StringUtil.join(userIds, ',');
        List<User> users = lambdaQuery().in(User::getId, userIds)
                .last("order by field (id, " + join + ")")
                .list();

        List<UserVo> userVos = BeanUtil.copyToList(users, UserVo.class);
        userVos.forEach(u -> {
            int level = LevelUtil.calculateLevel(u.getExp());
            u.setLevel(level);
        });

        return SaResult.data(userVos);
    }

    @Override
    public SaResult listAdmin() {

        Stream<Long> adminUserIds = lambdaQuery().in(User::getRole, List.of(USER_ADMIN_ROLE_INTEGER, USER_SUPER_ADMIN_ROLE_INTEGER))
                .ne(User::getStatus, USER_IS_BANNED)
                .select(User::getId)
                .list()
                .stream()
                .map(User::getId);

        return SaResult.data(adminUserIds);
    }

    @Override
    public Page<User> listUsers(Integer page) {

        Page<User> userPage = new Page<>(page, ScrollConstant.SCROLL_LIMIT);
        Page<User> pageResult = page(userPage, new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime, User::getId));

        return pageResult;
    }

    @Override
    public SaResult changeRole(Long userId, Integer role) {

        StpUtil.getSessionByLoginId(userId).set("role", role);

        lambdaUpdate().eq(User::getId, userId)
                .set(User::getRole, role)
                .update();

        return SaResult.ok("用户角色修改成功");
    }

    @Override
    public void editPassword(Long userId, String password) {

        //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(password);

        //更新密码
        lambdaUpdate().eq(User::getId, userId)
                .set(User::getPassword, encode)
                .set(BaseEntity::getUpdateTime, LocalDateTime.now())
                .update();

    }

    @Override
    public SaResult banUser(Long userId) {

        User user = lambdaQuery().eq(User::getId, userId)
                .select(User::getStatus)
                .one();
        if (user == null) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        if (user.getStatus().equals(USER_IS_BANNED)) {
            //解封操作
            lambdaUpdate().eq(User::getId, userId)
                    .set(User::getStatus, USER_IS_UNBANNED)
                    .update();
            return SaResult.ok("用户已解封");
        } else {
            //封禁操作
            lambdaUpdate().eq(User::getId, userId)
                    .set(User::getStatus, USER_IS_BANNED)
                    .update();
            //踢出登录
            StpUtil.logout(userId);
            return SaResult.ok("用户已封禁");
        }
    }

    @Override
    public SaResult delUser(Long userId) {

        boolean remove = lambdaUpdate().eq(User::getId, userId)
                .remove();
        if (!remove) {
            return SaResult.error(USER_NOT_EXISTS);
        }

        //rabbitmq 异步删除该用户的文章、评论、信封、关注者、粉丝
        rabbitTemplate.convertAndSend(RabbitMQConstant.ADMIN_DELETE_USER_EXCHANGE, "", userId);

        return SaResult.ok("用户删除成功");
    }

    @Override
    public Long countUsers() {

        return count();
    }
}
