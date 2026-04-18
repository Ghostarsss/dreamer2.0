package com.dreamer.userservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.EXPConstant;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.constant.ScrollConstant;
import com.dreamer.common.entity.dto.EXPRabbitDto;
import com.dreamer.common.entity.dto.FollowingRabbitDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.common.entity.result.ScrollResult;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.common.message.UserMessage;
import com.dreamer.userservice.constant.UserConstant;
import com.dreamer.userservice.entity.pojo.UserFollow;
import com.dreamer.userservice.entity.vo.UserVo;
import com.dreamer.userservice.key.LockKey;
import com.dreamer.userservice.mapper.FollowingMapper;
import com.dreamer.userservice.message.FollowingMessage;
import com.dreamer.userservice.service.IFollowingService;
import com.dreamer.userservice.service.IUserService;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dreamer.userservice.key.RedisKey.USER_FANS_KEY;
import static com.dreamer.userservice.key.RedisKey.USER_FOLLOWING_KEY;
import static com.dreamer.userservice.message.FollowingMessage.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowingServiceImpl extends ServiceImpl<FollowingMapper, UserFollow> implements IFollowingService {

    private final IUserService userService;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    @Override
    public SaResult followByUserId(String userId) {

        //查询用户是否存在或被封禁
        User user = userService.getById(userId);
        if (user == null) {
            return SaResult.error(UserMessage.USER_NOT_EXISTS);
        }
        if (user.getStatus().equals(UserConstant.USER_IS_BANNED)) {
            return SaResult.error(UserMessage.USER_IS_BANNED_MESSAGE);
        }

        //禁止用户关注自己
        long currentUserId = (long) StpUtil.getSession().get("userId");
        if (userId.equals(String.valueOf(currentUserId))) {
            log.error("该用户尝试关注自己，存在危险行为，用户 id: {}", currentUserId);
            return SaResult.error(PROHIBIT_FOLLOWING_OWN);
        }
        User currentUser = userService.getById(currentUserId);

        //加锁，防止重复关注
        String lockKey = LockKey.FOLLOWING_USER_KEY + currentUserId;
        RLock lock = redissonClient.getLock(lockKey);

        try {

            //获取锁
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //用户是否已被关注
            boolean exists = lambdaQuery().eq(UserFollow::getUserId, currentUserId).eq(UserFollow::getFollowedUserId, userId).exists();
            if (exists) {
                return SaResult.error(NOT_FOLLOWING_AGAIN);
            }

            //关注业务
            UserFollow userFollow = new UserFollow();
            userFollow.setUserId(Long.valueOf(currentUserId));
            userFollow.setFollowedUserId(Long.valueOf(userId));
            userFollow.setCreateTime(LocalDateTime.now());
            boolean save = save(userFollow);
            if (!save) {
                log.error("用户关注失败,关注者: {};被关注者: {}", currentUserId, userId);
                return SaResult.error(SystemMessage.SYSTEM_ERROR);
            }

            //更新 following 缓存
            double currentTimeMillis = System.currentTimeMillis();
            String followingKey = USER_FOLLOWING_KEY + currentUserId;
            String fansKey = USER_FANS_KEY + userId;
            redisTemplate.opsForZSet().add(followingKey, userId, currentTimeMillis);
            redisTemplate.opsForZSet().add(fansKey, String.valueOf(currentUserId), currentTimeMillis);
            redisTemplate.expire(followingKey, 7, TimeUnit.DAYS);
            redisTemplate.expire(fansKey, 7, TimeUnit.DAYS);

            //异步被关注用户消息和加经验
            FollowingRabbitDto followingRabbitDto = FollowingRabbitDto.builder().followedId(Long.valueOf(userId)).fansId(Long.valueOf(currentUserId)).fansUsername(currentUser.getUsername()).build();
            EXPRabbitDto expRabbitDto = EXPRabbitDto.builder().userId(Long.valueOf(userId)).expIncrement(EXPConstant.SMALL_EXP).causeUserId(currentUserId).build();
            rabbitTemplate.convertAndSend(RabbitMQConstant.USER_FOLLOWING_MESSAGE_QUEUE, followingRabbitDto);
            rabbitTemplate.convertAndSend(RabbitMQConstant.USER_FOLLOWING_EXP_QUEUE, expRabbitDto);

            return SaResult.ok(FOLLOWING_SUCCESS);
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
    public SaResult unFollowByUserId(String userId) {
        //查询用户是否存在或被封禁
        User user = userService.getById(userId);
        if (user == null) {
            return SaResult.error(UserMessage.USER_NOT_EXISTS);
        }
        if (user.getStatus().equals(UserConstant.USER_IS_BANNED)) {
            return SaResult.error(UserMessage.USER_IS_BANNED_MESSAGE);
        }

        int currentUserId = StpUtil.getSession().getInt("userId");

        //取关业务
        lambdaUpdate().eq(UserFollow::getUserId, currentUserId).eq(UserFollow::getFollowedUserId, userId).remove();

        //更新 redis 缓存
        redisTemplate.opsForZSet().remove(USER_FOLLOWING_KEY + currentUserId, userId);
        redisTemplate.opsForZSet().remove(USER_FANS_KEY + userId, String.valueOf(currentUserId));

        return SaResult.ok(UNFOLLOWING_MESSAGE);
    }

    @Override
    public SaResult listMeFollowing(Long cursor, Integer offset) {

        //判断查询参数是否存在
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        if (offset == null) {
            offset = 0;
        }

        //从 redis 缓存中查询数据
        long currentUserId = StpUtil.getSession().getLong("userId");
        String followingKey = USER_FOLLOWING_KEY + currentUserId;
        Boolean hashKey = redisTemplate.hasKey(followingKey);

        //若缓存为空，重新查数据库并更新缓存
        if (!hashKey) {
            //查询出关注列表中的用户 id 和关注时间
            Map<Long, Long> followedUserAndTime = lambdaQuery().eq(UserFollow::getUserId, currentUserId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(UserFollow::getFollowedUserId, f -> {
                        return f.getCreateTime()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
                    }));

            for (Map.Entry<Long, Long> f : followedUserAndTime.entrySet()) {
                redisTemplate.opsForZSet().add(followingKey, f.getKey().toString(), f.getValue());
            }

            redisTemplate.expire(followingKey, 7, TimeUnit.DAYS);
        }

        //查缓存
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(followingKey
                , 0, cursor, offset, ScrollConstant.SCROLL_LIMIT);

        //取出关注用户 id
        ArrayList<String> userIds = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int newOffset = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {

            String userId = typedTuple.getValue();
            Double timeScore = typedTuple.getScore();
            userIds.add(userId);

            //实现新 offset 的计算，返回给前端
            if (timeScore == minTime) {
                newOffset++;
            } else {
                newOffset = 1;
                minTime = timeScore.longValue();
            }
        }

        ScrollResult<UserVo> userVoScrollResult = new ScrollResult<>();

        if (userIds.isEmpty()) {
            return SaResult.data(userVoScrollResult);
        }

        //数据库查询用户信息
        String join = String.join(",", userIds);
        List<User> users = userService.lambdaQuery().in(User::getId, userIds)
                .select(User::getId, User::getUsername, User::getAvatar)
                .last("order by field ( id ," + join + ")")
                .list();
        List<UserVo> userVoList = BeanUtil.copyToList(users, UserVo.class);

        //封装返回
        userVoScrollResult.setList(userVoList);
        userVoScrollResult.setOffset(newOffset);
        userVoScrollResult.setCursor(minTime);

        return SaResult.data(userVoScrollResult);
    }

    @Override
    public SaResult listMeFans(Long cursor, Integer offset) {

        //判断前端是否传参
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        if (offset == null) {
            offset = 0;
        }

        //查询缓存是否存在
        long currentUserId = StpUtil.getSession().getLong("userId");
        String fansKey = USER_FANS_KEY + currentUserId;
        Boolean hasKey = redisTemplate.hasKey(fansKey);

        if (!hasKey) {
            //缓存不存在

            //查数据库
            Map<Long, Long> fansIdAndTime = lambdaQuery().eq(UserFollow::getFollowedUserId, currentUserId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(UserFollow::getUserId, f -> {
                        return f.getCreateTime()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
                    }));

            //存缓存
            for (Map.Entry<Long, Long> entry : fansIdAndTime.entrySet()) {

                redisTemplate.opsForZSet().add(fansKey, entry.getKey().toString(), entry.getValue());
                redisTemplate.expire(fansKey, 7, TimeUnit.DAYS);
            }


        }

        //查缓存
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                fansKey,
                0,
                cursor,
                offset,
                ScrollConstant.SCROLL_LIMIT
        );

        //解析缓存内容
        long maxTime = 0;
        int newOffset = 1;
        ArrayList<Long> fansIds = new ArrayList<>(typedTuples.size());
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {

            String fansId = typedTuple.getValue();
            Double followingTime = typedTuple.getScore();
            fansIds.add(Long.valueOf(fansId));

            //更新 cursor 和 offset
            if (followingTime == maxTime) {
                newOffset++;
            } else {
                newOffset = 1;
                maxTime = followingTime.longValue();
            }

        }

        ScrollResult<UserVo> fansScrollResult = new ScrollResult<>();
        fansScrollResult.setCursor(maxTime);
        fansScrollResult.setOffset(newOffset);

        if (fansIds.isEmpty()) {
            return SaResult.data(fansScrollResult);
        }

        //滚动查询粉丝数据
        String join = StringUtil.join(fansIds, ',');
        List<User> fans = userService.lambdaQuery().select(User::getId, User::getUsername, User::getAvatar)
                .in(User::getId, fansIds)
                .last("order by field ( id ," + join + ")")
                .list();
        List<UserVo> userVoList = BeanUtil.copyToList(fans, UserVo.class);

        //封装返回数据
        fansScrollResult.setList(userVoList);

        return SaResult.data(fansScrollResult);
    }

    @Override
    public SaResult removeFansByUserId(long userId) {

        long currentUserId = StpUtil.getSession().getLong("userId");

        //数据库移除关注
        lambdaUpdate().eq(UserFollow::getFollowedUserId, currentUserId)
                .eq(UserFollow::getUserId, userId)
                .remove();

        //更新缓存
        String fansKey = USER_FANS_KEY + currentUserId;
        String followingKey = USER_FOLLOWING_KEY + userId;

        redisTemplate.opsForZSet().remove(fansKey, String.valueOf(userId));
        redisTemplate.opsForZSet().remove(followingKey, String.valueOf(currentUserId));

        return SaResult.ok(FollowingMessage.REMOVE_FANS_SUCCESS);
    }

    @Override
    public SaResult listFollowingByUserId(Long userId, Long cursor, Integer offset) {

        //判断滚动查询参数是否存在
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        if (offset == null) {
            offset = 0;
        }

        //查询缓存
        String followingKey = USER_FOLLOWING_KEY + userId;
        Boolean hasKey = redisTemplate.hasKey(followingKey);

        //若不存在，查数据库并更新缓存
        if (!hasKey) {

            Map<Long, Long> userIdAndTime = lambdaQuery()
                    .eq(UserFollow::getUserId, userId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(UserFollow::getFollowedUserId, f -> {
                        return f.getCreateTime()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
                    }));

            //更新缓存
            for (Map.Entry<Long, Long> entry : userIdAndTime.entrySet()) {
                redisTemplate.opsForZSet().add(followingKey, entry.getKey().toString(), entry.getValue());
            }
        }

        //查询缓存
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                followingKey, 0, cursor, offset, ScrollConstant.SCROLL_LIMIT
        );

        //解析缓存
        double maxTime = 0;
        int newOffset = 1;
        ArrayList<Long> userIds = new ArrayList<>(typedTuples.size());
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {

            String followingUserId = tuple.getValue();
            Double score = tuple.getScore();
            userIds.add(Long.valueOf(followingUserId));

            //更新 cursor 和 offset
            if (maxTime == score) {
                newOffset++;
            } else {
                newOffset = 1;
                maxTime = score;
            }
        }

        ScrollResult<UserVo> userVoScrollResult = new ScrollResult<>();

        if (userIds.isEmpty()) {
            return SaResult.data(userVoScrollResult);
        }

        //查询用户数据库
        String join = StringUtil.join(userIds, ",");
        List<User> users = userService.lambdaQuery().in(User::getId, userIds)
                .select(User::getId, User::getUsername, User::getAvatar)
                .last("order by field ( id ," + join + ")")
                .list();
        List<UserVo> userVoList = BeanUtil.copyToList(users, UserVo.class);

        //封装返回
        userVoScrollResult.setList(userVoList);
        userVoScrollResult.setOffset(newOffset);
        userVoScrollResult.setCursor((long) maxTime);

        return SaResult.data(userVoScrollResult);
    }

    @Override
    public SaResult listFansByUserId(Long userId, Long cursor, Integer offset) {

        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        if (offset == null) {
            offset = 0;
        }

        //查询缓存是否存在
        String fansKey = USER_FANS_KEY + userId;
        Boolean hasKey = redisTemplate.hasKey(fansKey);

        if (!hasKey) {
            //缓存不存在，查询数据库
            Map<Long, Long> fansIdAndTime = lambdaQuery().eq(UserFollow::getFollowedUserId, userId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(UserFollow::getUserId, f -> {
                        return LocalDateTimeUtil.toEpochMilli(f.getCreateTime());
                    }));

            //更新数据库
            for (Map.Entry<Long, Long> entry : fansIdAndTime.entrySet()) {
                redisTemplate.opsForZSet().add(fansKey, entry.getKey().toString(), entry.getValue());
            }
        }

        //查询缓存
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                fansKey, 0, cursor, offset, ScrollConstant.SCROLL_LIMIT
        );

        //解析缓存
        long maxTime = 0;
        int newOffset = 1;
        ArrayList<Long> fansIds = new ArrayList<>(typedTuples.size());
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            String fansId = tuple.getValue();
            Double score = tuple.getScore();
            fansIds.add(Long.valueOf(fansId));

            //更新 cursor 和偏移量
            if (maxTime == score) {
                newOffset++;
            } else {
                maxTime = score.longValue();
                newOffset = 1;
            }
        }

        //若缓存中没有数据
        ScrollResult<UserVo> fansVoScrollResult = new ScrollResult<>();
        if (fansIds.isEmpty()) {
            return SaResult.data(fansVoScrollResult);
        }

        //查询数据库获取粉丝数据
        String join = StringUtil.join(fansIds, ',');
        List<User> users = userService.lambdaQuery()
                .select(User::getId, User::getUsername, User::getAvatar)
                .last("order by field (id , " + join + ")")
                .list();

        //封装后返回
        List<UserVo> fansVo = BeanUtil.copyToList(users, UserVo.class);
        fansVoScrollResult.setList(fansVo);
        fansVoScrollResult.setOffset(newOffset);
        fansVoScrollResult.setCursor(maxTime);
        return SaResult.data(fansVoScrollResult);
    }
}