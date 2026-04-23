package com.dreamer.postservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.MessageConstant;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.constant.ScrollConstant;
import com.dreamer.common.constant.UserConstant;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.result.ScrollResult;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.common.message.UserMessage;
import com.dreamer.postservice.constant.LikesConstant;
import com.dreamer.postservice.constant.PostConstant;
import com.dreamer.postservice.entity.pojo.Post;
import com.dreamer.common.entity.vo.PostVo;
import com.dreamer.postservice.feign.UserFeignClient;
import com.dreamer.postservice.key.LockKey;
import com.dreamer.postservice.key.RedisKey;
import com.dreamer.postservice.mapper.PostMapper;
import com.dreamer.postservice.message.PostMessage;
import com.dreamer.postservice.service.ILikesService;
import com.dreamer.postservice.service.IPostService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import com.dreamer.common.entity.vo.UserVo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.dreamer.common.constant.RabbitMQConstant.POST_DELETE_COMMENT_LIKE_QUEUE;
import static com.dreamer.postservice.constant.PostConstant.POST_STATUS_PASS_REVIEW;
import static com.dreamer.postservice.message.PostMessage.POST_UPDATE_SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    private final UserFeignClient userFeignClient;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ILikesService likesService;

    @Override
    public SaResult addPost(String content) {

        long userId = StpUtil.getLoginIdAsLong();

        //加锁，防止高并发
        String lockKey = LockKey.POST_SAVE_LOCK + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //尝试获取锁
            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //判断该用户是否被封禁
            SaResult isBaned = userFeignClient.isBaned(userId);
            if (isBaned.getCode() == 500) {
                return SaResult.error(isBaned.getMsg());
            }
            if (UserConstant.USER_IS_BANNED.equals(isBaned.getData())) {
                return SaResult.error(UserMessage.USER_IS_BANNED_MESSAGE);
            }

            //文章进入审核
            LocalDateTime now = LocalDateTime.now();
            Post post = new Post();
            post.setContent(content);
            post.setUserId(userId);
            post.setCreateTime(now);
            post.setUpdateTime(now);

            boolean save = save(post);
            if (!save) {
                log.error("用户提交文章失败: {}", post);
                return SaResult.error(SystemMessage.SYSTEM_ERROR);
            }
            return SaResult.ok(PostMessage.POST_SAVE_SUCCESS_MESSAGE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public SaResult delPost(long postId) {

        //判断文章是否属于该用户
        long userId = StpUtil.getLoginIdAsLong();
        boolean exists = lambdaQuery().eq(Post::getId, postId)
                .eq(Post::getUserId, userId)
                .exists();
        if (!exists) {
            return SaResult.error(PostMessage.POST_NOT_EXIST);
        }

        //逻辑删除文章
        boolean remove = lambdaUpdate().eq(Post::getId, postId)
                .eq(Post::getUserId, userId)
                .remove();
        if (!remove) {
            log.error("文章删除失败，用户id：{}，文章 id：{}", userId, postId);
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        //rabbitMQ 异步删除文章评论和点赞
        rabbitTemplate.convertAndSend(POST_DELETE_COMMENT_LIKE_QUEUE, postId);

        return SaResult.ok(PostMessage.POST_DELETE_SUCCESS_MESSAGE);
    }

    @Override
    public SaResult listMyPost(Long cursor) {

        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }

        long userId = StpUtil.getLoginIdAsLong();

        List<Post> posts = lambdaQuery().eq(Post::getUserId, userId)
                .lt(Post::getId, cursor)
                .orderByDesc(Post::getId)
                .last("limit " + ScrollConstant.SCROLL_LIMIT)
                .list();

        List<PostVo> postVos = BeanUtil.copyToList(posts, PostVo.class);

        //判断当前用户是否点赞
        for (PostVo postVo : postVos) {
            String likeKey = RedisKey.POST_LIKES_REDIS_KEY + postVo.getId();

            Boolean member = redisTemplate.opsForSet().isMember(likeKey, String.valueOf(userId));
            if (Boolean.TRUE.equals(member)) {
                postVo.setIsLike(LikesConstant.IS_LIKED);
            } else {
                postVo.setIsLike(LikesConstant.NOT_LIKED);
            }
        }

        return SaResult.data(postVos);
    }

    @Override
    public SaResult updatePostByPostId(Long postId, String content) {

        //判断当前用户是否是文章作者
        long userId = StpUtil.getLoginIdAsLong();

        //更新文章（更新后需审核）
        boolean update = lambdaUpdate().eq(Post::getUserId, userId)
                .eq(Post::getId, postId)
                .set(Post::getContent, content)
                .set(Post::getStatus, PostConstant.POST_STATUS_PENDING_REVIEW)
                .setSql("edit_count = edit_count + 1")
                .update();
        if (!update) {
            return SaResult.error(PostMessage.POST_NOT_EXIST);
        }

        return SaResult.ok(POST_UPDATE_SUCCESS_MESSAGE);
    }

    @Override
    public SaResult queryPostByPostId(Long postId) {

        //feign 查询文章用户信息
        Long userId = lambdaQuery().eq(Post::getId, postId)
                .select(Post::getUserId)
                .one()
                .getUserId();
        SaResult userResult = userFeignClient.queryUserById(userId.toString());
        if (userResult.getCode() == 500) {
            return SaResult.error(userResult.getMsg());
        }
        UserVo userInfo = BeanUtil.copyProperties(userResult.getData(), UserVo.class);

        //查询文章信息
        Post post = lambdaQuery().eq(Post::getId, postId)
                .one();

        //用户和文章拼接并封装
        PostVo postVo = BeanUtil.copyProperties(post, PostVo.class);
        postVo.setAvatar(userInfo.getAvatar());
        postVo.setUsername(userInfo.getUsername());
        postVo.setLevel(userInfo.getLevel());

        return SaResult.data(postVo);
    }

    @Override
    public SaResult listNewPosts(Long cursor) {

        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }

        List<Post> posts = lambdaQuery().lt(Post::getId, cursor)
                .eq(Post::getStatus, POST_STATUS_PASS_REVIEW)
                .last("limit " + ScrollConstant.SCROLL_LIMIT)
                .orderByDesc(Post::getId).list();
        List<PostVo> postVos = BeanUtil.copyToList(posts, PostVo.class);

        //查询文章用户信息
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        //feign 批量查询用户信息
        SaResult usersResult = userFeignClient.batchQueryUserByUserId(userIds);
        if (usersResult.getCode() == 500) {
            return SaResult.error(usersResult.getMsg());
        }

        List<UserVo> userVos = BeanUtil.copyToList((List<?>) usersResult.getData(), UserVo.class);
        Map<Long, UserVo> collect = userVos.stream().collect(Collectors.toMap(UserVo::getId, userVo -> userVo));
        //postVo 拼接用户信息
        for (PostVo postVo : postVos) {

            UserVo userVo = collect.get(postVo.getUserId());
            if (userVo != null) {
                //判断当前用户是否点赞
                String likeRedisKey = RedisKey.POST_LIKES_REDIS_KEY + postVo.getId();
                Boolean member = redisTemplate.opsForSet().isMember(likeRedisKey, userVo.getId().toString());
                if (Boolean.TRUE.equals(member)) {
                    postVo.setIsLike(PostConstant.POST_IS_LIKED);
                }

                postVo.setAvatar(userVo.getAvatar());
                postVo.setUsername(userVo.getUsername());
                postVo.setLevel(userVo.getLevel());
            }
        }

        return SaResult.data(postVos);
    }

    @Override
    public SaResult listHotPosts(Long cursor, Integer offset) {

        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }
        if (offset == null) {
            offset = 0;
        }

        //缓存查询 hot 文章信息
        String postHotIdKey = RedisKey.POST_HOT_ID_KEY;

        if (!redisTemplate.hasKey(postHotIdKey)) {
            //如果缓存过期，更新缓存

            List<Post> posts = lambdaQuery().lt(Post::getId, cursor)
                    .ge(Post::getProtonCount, PostConstant.POST_HOT_MIN_PROTON_COUNT)
                    .eq(Post::getStatus, POST_STATUS_PASS_REVIEW)
                    .orderByDesc(Post::getProtonCount, Post::getId).list();
            if (posts.isEmpty()) {
                return SaResult.data(List.of());
            }

            List<PostVo> postVos = BeanUtil.copyToList(posts, PostVo.class);

            //查询文章用户信息
            List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
            //feign 批量查询用户信息
            SaResult usersResult = userFeignClient.batchQueryUserByUserId(userIds);
            if (usersResult.getCode() == 500) {
                return SaResult.error(usersResult.getMsg());
            }

            List<UserVo> userVos = BeanUtil.copyToList((List<?>) usersResult.getData(), UserVo.class);
            Map<Long, UserVo> collect = userVos.stream().collect(Collectors.toMap(UserVo::getId, userVo -> userVo));
            //postVo 拼接用户信息
            HashSet<ZSetOperations.TypedTuple<String>> typedTuples = new HashSet<>();
            for (PostVo postVo : postVos) {

                UserVo userVo = collect.get(postVo.getUserId());
                if (userVo != null) {
                    //判断当前用户是否点赞
                    String likeRedisKey = RedisKey.POST_LIKES_REDIS_KEY + postVo.getId();
                    Boolean member = redisTemplate.opsForSet().isMember(likeRedisKey, userVo.getId());
                    if (Boolean.TRUE.equals(member)) {
                        postVo.setIsLike(PostConstant.POST_IS_LIKED);
                    }

                    postVo.setAvatar(userVo.getAvatar());
                    postVo.setUsername(userVo.getUsername());
                    postVo.setLevel(userVo.getLevel());
                }

                //更新缓存
                String postId = postVo.getId().toString();
                Integer protonCount = postVo.getProtonCount();
                ZSetOperations.TypedTuple<String> stringTypedTuple = new DefaultTypedTuple<>(postId, protonCount.doubleValue());
                typedTuples.add(stringTypedTuple);

                String hotPostKey = RedisKey.POST_HOT_KEY + postId;
                String jsonStr = JSONUtil.toJsonStr(postVo);
                redisTemplate.opsForValue().set(hotPostKey, jsonStr);
                redisTemplate.expire(hotPostKey, 14, TimeUnit.DAYS);

            }
            redisTemplate.opsForZSet().add(postHotIdKey, typedTuples);
            //设置过期时间
            redisTemplate.expire(postHotIdKey, 14, TimeUnit.DAYS);
            return SaResult.data(postVos);

        }

        //缓存存在，查询缓存
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(postHotIdKey
                , 0, cursor, offset, ScrollConstant.SCROLL_LIMIT);

        //解析缓存
        ArrayList<Long> hotPostIds = new ArrayList<>(typedTuples.size());
        long maxProtonCount = 0;
        int newOffset = 1;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            Long postId = Long.valueOf(tuple.getValue());
            Double score = tuple.getScore();
            hotPostIds.add(postId);

            //更新查询参数
            if (maxProtonCount == score) {
                newOffset++;
            } else {
                maxProtonCount = score.longValue();
                newOffset = 1;
            }
        }

        //去文章缓存里根据 postId 查文章
        List<String> postKeyList = hotPostIds.stream().map(p -> RedisKey.POST_HOT_KEY + p).toList();
        List<String> postVoJsons = redisTemplate.opsForValue().multiGet(postKeyList);
        List<PostVo> postVos = postVoJsons.stream().map(s -> JSONUtil.toBean(s, PostVo.class)).toList();

        //封装
        ScrollResult<PostVo> postVoScrollResult = new ScrollResult<>();
        postVoScrollResult.setList(postVos);
        postVoScrollResult.setCursor(maxProtonCount);
        postVoScrollResult.setOffset(newOffset);

        return SaResult.data(postVoScrollResult);
    }

    @Override
    public SaResult listFollowingPost(Long cursor) {

        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }

        //查询关注了的用户
        long userId = StpUtil.getLoginIdAsLong();
        SaResult saResult = userFeignClient.listFollowingByUserId(userId, null, null);
        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        //取出关注的用户 id
        Object data = saResult.getData();
        ScrollResult<?> scrollResult = BeanUtil.toBean(data, ScrollResult.class);
        List<UserVo> userVos = BeanUtil.copyToList(scrollResult.getList(), UserVo.class);
        List<Long> followingUserId = userVos.stream().map(UserVo::getId).toList();

        //查询关注作者的作品
        List<Post> posts = lambdaQuery().in(Post::getUserId, followingUserId)
                .eq(Post::getStatus, POST_STATUS_PASS_REVIEW)
                .lt(Post::getId, cursor)
                .orderByDesc(Post::getId)
                .last("limit " + ScrollConstant.SCROLL_LIMIT)
                .list();

        //封装 postVo
        List<PostVo> postVos = BeanUtil.copyToList(posts, PostVo.class);
        Map<Long, UserVo> collect = userVos.stream().collect(Collectors.toMap(UserVo::getId, u -> u));
        for (PostVo postVo : postVos) {

            UserVo userVo = collect.get(postVo.getUserId());
            if (userVo != null) {
                //判断当前用户是否点赞
                String likeRedisKey = RedisKey.POST_LIKES_REDIS_KEY + postVo.getId();
                Boolean member = redisTemplate.opsForSet().isMember(likeRedisKey, userVo.getId());
                if (Boolean.TRUE.equals(member)) {
                    postVo.setIsLike(PostConstant.POST_IS_LIKED);
                }

                postVo.setAvatar(userVo.getAvatar());
                postVo.setLevel(userVo.getLevel());
                postVo.setUsername(userVo.getUsername());
            }
        }

        //封装返回值
        ScrollResult<PostVo> postVoScrollResult = new ScrollResult<>();
        if (!postVos.isEmpty()) {
            cursor = postVos.get(postVos.size() - 1).getId();
        }
        postVoScrollResult.setList(postVos);
        postVoScrollResult.setCursor(cursor);

        return SaResult.data(postVoScrollResult);
    }

    @Override
    @GlobalTransactional
    public SaResult protonPost(Long postId, Integer protons) {

        long userId = StpUtil.getLoginIdAsLong();

        //判断文章是否存在
        Long postUserId = lambdaQuery().eq(Post::getId, postId)
                .select(Post::getUserId)
                .eq(Post::getStatus, POST_STATUS_PASS_REVIEW)
                .one()
                .getUserId();
        if (postUserId == null) {
            return SaResult.error(PostMessage.POST_NOT_EXIST);
        }

        //加锁，防止高并发
        String lockKey = LockKey.POST_TIP_PROTON_LOCK + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {

            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //判断该用户曾经是否投过质子
            String redisKey = RedisKey.POST_PROTON_KEY + postId;
            Boolean member = redisTemplate.opsForSet().isMember(redisKey, String.valueOf(userId));
            if (Boolean.TRUE.equals(member)) {
                return SaResult.error(PostMessage.POST_IS_TIP_PROTON);
            }

            //判断质子数是否合法
            if (protons > PostConstant.POST_PROTON_MAX || protons <= 0) {
                return SaResult.error(PostMessage.POST_PROTON_EXCEED_MAX);
            }

            //判断该有用户的「质子」余额是否足够，并扣除
            SaResult saResult = userFeignClient.deductProton(userId, protons);
            if (saResult.getCode() == 500) {
                return SaResult.error(saResult.getMsg());
            }

            //判断文章是否属于自己,是否已通过审核,再更新
            boolean update = lambdaUpdate().eq(Post::getId, postId)
                    .ne(Post::getUserId, userId)
                    .eq(Post::getStatus, POST_STATUS_PASS_REVIEW)
                    .setSql("proton_count = proton_count + " + protons)
                    .update();
            if (!update) {
                throw new RuntimeException();
            }

            //redis 记录该用户已打赏
            redisTemplate.opsForSet().add(redisKey, String.valueOf(userId));

            //异步发送打赏信息
            String username = StpUtil.getSession().getString("username");
            MessageDto messageDto = MessageDto.builder()
                    .type(MessageConstant.PROTON_MESSAGE_TYPE)
                    .sendId(userId)
                    .userId(postUserId)
                    .content("梦想家 " + username + " 打赏了您的文章 " + protons + " 个「质子」")
                    .build();
            rabbitTemplate.convertAndSend(RabbitMQConstant.POST_TIP_PROTON_MESSAGE_QUEUE, messageDto);

            return SaResult.ok(saResult.getMsg());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
