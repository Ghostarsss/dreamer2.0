package com.dreamer.postservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.postservice.constant.LikesConstant;
import com.dreamer.common.constant.PostConstant;
import com.dreamer.postservice.entity.pojo.Comment;
import com.dreamer.postservice.entity.pojo.Likes;
import com.dreamer.postservice.entity.pojo.Post;
import com.dreamer.postservice.key.RedisKey;
import com.dreamer.postservice.mapper.LikesMapper;
import com.dreamer.postservice.message.CommentMessage;
import com.dreamer.postservice.message.PostMessage;
import com.dreamer.postservice.service.ICommentService;
import com.dreamer.postservice.service.ILikesService;
import com.dreamer.postservice.service.IPostService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.dreamer.postservice.message.LikesMessage.LIKE_SUCCESS_MESSAGE;
import static com.dreamer.postservice.message.LikesMessage.UNLIKE_SUCCESS_MESSAGE;

@Service
@RequiredArgsConstructor
public class LikesServiceImpl extends ServiceImpl<LikesMapper, Likes> implements ILikesService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    @Lazy
    @Autowired
    private ICommentService commentService;
    private final RedissonClient redissonClient;

    @Lazy
    @Autowired
    private IPostService postService;

    @Override
    public void delLikesByPostId(long postId) {
        lambdaUpdate().eq(Likes::getPostId, postId).remove();

        //文章点赞缓存删除
        String postLikesKey = RedisKey.POST_LIKES_REDIS_KEY + postId;
        redisTemplate.delete(postLikesKey);
    }


    @Override
    public SaResult likePost(Long postId) {

        //判断文章是否存在，是否已审核通过
        Post post = postService.lambdaQuery().eq(Post::getId, postId)
                .select(Post::getUserId)
                .eq(Post::getStatus, PostConstant.POST_STATUS_PASS_REVIEW)
                .one();
        if (post == null) {
            return SaResult.error(PostMessage.POST_NOT_EXIST);
        }

        Long postUserId = post.getUserId();

        long userId = StpUtil.getLoginIdAsLong();
        //封装异步消息 Dto
        MessageDto messageDto = MessageDto.builder()
                .sendId(userId)
                .postId(postId)
                .userId(postUserId)
                .build();

        //redis 原子性判断是否已点赞
        String postLikeDbSyncQueue = RabbitMQConstant.POST_LIKE_DB_SYNC_QUEUE;
        String likeRedisKey = RedisKey.POST_LIKES_REDIS_KEY + postId;
        Long add = redisTemplate.opsForSet().add(likeRedisKey, String.valueOf(userId));
        if (add == 1) {

            //数据库实现异步更新点赞表
            messageDto.setIsLike(LikesConstant.IS_LIKED);
            rabbitTemplate.convertAndSend(postLikeDbSyncQueue, messageDto);

            //用户给自己点赞不触发异步
            if (userId != postUserId) {

                //被赞的文章作者异步加经验
                String postLikeIncrementEXPQueue = RabbitMQConstant.POST_LIKE_INCREMENT_EXP_QUEUE;
                rabbitTemplate.convertAndSend(postLikeIncrementEXPQueue, messageDto);

                //异步通知文章作者被点赞消息
                String postLikeMessageQueue = RabbitMQConstant.POST_LIKE_MESSAGE_QUEUE;
                rabbitTemplate.convertAndSend(postLikeMessageQueue, messageDto);
            }

            return SaResult.ok(LIKE_SUCCESS_MESSAGE);

        } else {
            //取消点赞
            redisTemplate.opsForSet().remove(likeRedisKey, String.valueOf(userId));

            //数据库实现异步更新点赞表
            messageDto.setIsLike(LikesConstant.NOT_LIKED);
            rabbitTemplate.convertAndSend(postLikeDbSyncQueue, messageDto);

            return SaResult.ok(UNLIKE_SUCCESS_MESSAGE);
        }
    }

    @Override
    public void likePostDbSync(@NonNull MessageDto messageDto) {

        Long postId = messageDto.getPostId();
        Long sendId = messageDto.getSendId();
        Likes likes = new Likes();
        likes.setPostId(postId);
        likes.setUserId(sendId);
        likes.setCreateTime(LocalDateTime.now());
        //判断点赞还是取消点赞
        if (messageDto.getIsLike() == LikesConstant.IS_LIKED) {
            //点赞操作
            save(likes);

            postService.lambdaUpdate().eq(Post::getId, postId)
                    .setSql("like_count = like_count + 1")
                    .update();

        } else if (messageDto.getIsLike() == LikesConstant.NOT_LIKED) {
            //取消点赞操作
            lambdaUpdate().eq(Likes::getUserId, sendId)
                    .eq(Likes::getPostId, postId)
                    .remove();

            postService.lambdaUpdate().eq(Post::getId, postId)
                    .setSql("like_count = like_count - 1")
                    .update();
        }
    }

    @Override
    @Transactional
    public SaResult likeComment(Long commentId) {

        long userId = StpUtil.getLoginIdAsLong();

        //加锁，防止高并发点赞/取消点赞
        String lockKey = RedisKey.POST_LIKES_REDIS_KEY + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {

            boolean tryLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!tryLock) {
                return SaResult.error(SystemMessage.OPERATION_FREQUENT);
            }

            //查评论信息
            Comment comment = commentService.lambdaQuery().eq(Comment::getId, commentId)
                    .select(Comment::getUserId, Comment::getPostId).one();
            if (comment == null) {
                return SaResult.error(CommentMessage.POST_COMMENT_UNEXIST_MESSAGE);
            }

            //判断点赞状态
            boolean remove = lambdaUpdate().eq(Likes::getUserId, userId)
                    .eq(Likes::getCommentId, commentId)
                    .remove();
            String redisKey = RedisKey.POST_COMMENT_LIKES_REDIS_KEY + commentId;
            if (remove) {
                //取消点赞
                commentService.lambdaUpdate().eq(Comment::getId, commentId)
                        .setSql("like_count = like_count - 1")
                        .update();
                redisTemplate.opsForSet().remove(redisKey, String.valueOf(userId));
                return SaResult.ok(UNLIKE_SUCCESS_MESSAGE);
            }

            //点赞文章评论
            Likes likes = new Likes();
            likes.setCommentId(commentId);
            likes.setPostId(comment.getPostId());
            likes.setUserId(userId);
            likes.setCreateTime(LocalDateTime.now());

            save(likes);
            redisTemplate.opsForSet().add(redisKey, String.valueOf(userId));

            //更新指定评论点赞数量
            commentService.lambdaUpdate().eq(Comment::getId, commentId)
                    .setSql("like_count = like_count + 1")
                    .update();

            //如果点赞的是自己，不触发异步消息推送
            if (userId != comment.getUserId()) {
                //异步通知被点赞用户
                MessageDto messageDto = MessageDto.builder()
                        .sendId(userId)
                        .userId(comment.getUserId())
                        .commentId(commentId)
                        .build();
                rabbitTemplate.convertAndSend(RabbitMQConstant.POST_LIKE_MESSAGE_QUEUE, messageDto);
            }

            return SaResult.ok(LIKE_SUCCESS_MESSAGE);


        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {

            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


    }
}
