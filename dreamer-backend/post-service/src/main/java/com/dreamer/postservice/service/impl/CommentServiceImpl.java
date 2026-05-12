package com.dreamer.postservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.common.constant.PostConstant;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.constant.ScrollConstant;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.result.ScrollResult;
import com.dreamer.common.entity.vo.UserVo;
import com.dreamer.common.message.SystemMessage;
import com.dreamer.postservice.entity.dto.CommentDto;
import com.dreamer.postservice.entity.pojo.Comment;
import com.dreamer.postservice.entity.pojo.Post;
import com.dreamer.postservice.entity.vo.CommentVo;
import com.dreamer.postservice.feign.UserFeignClient;
import com.dreamer.postservice.key.RedisKey;
import com.dreamer.postservice.mapper.CommentMapper;
import com.dreamer.postservice.message.CommentMessage;
import com.dreamer.postservice.message.PostMessage;
import com.dreamer.postservice.service.ICommentService;
import com.dreamer.postservice.service.IPostService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dreamer.postservice.message.CommentMessage.POST_COMMENT_SUCCESS_MESSAGE;
import static com.dreamer.postservice.message.CommentMessage.POST_COMMENT_UNEXIST_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final StringRedisTemplate redisTemplate;
    private final IPostService postService;
    private final RabbitTemplate rabbitTemplate;
    private final UserFeignClient userFeignClient;

    @Override
    public void delCommentsByPostId(long postId) {

        List<Long> commentIds = lambdaQuery()
                .eq(Comment::getPostId, postId)
                .select(Comment::getId)
                .list()
                .stream()
                .map(Comment::getId)
                .toList();

        //删除数据库评论(及其子评论)
        lambdaUpdate()
                .eq(Comment::getPostId, postId)
                .remove();

        //批量删除 Redis 点赞缓存
        if (!commentIds.isEmpty()) {
            List<String> keys = commentIds.stream()
                    .map(id -> RedisKey.POST_COMMENT_LIKES_REDIS_KEY + id)
                    .toList();

            redisTemplate.delete(keys);
        }
    }

    @Override
    @Transactional
    public SaResult commentPostByPostId(CommentDto commentDto) {

        long userId = StpUtil.getLoginIdAsLong();
        Long parentCommentUserId = 0L;

        //封装评论
        LocalDateTime now = LocalDateTime.now();
        Comment comment = BeanUtil.copyProperties(commentDto, Comment.class);
        comment.setContent(StrUtil.trim(commentDto.getContent()));
        comment.setUserId(userId);
        comment.setCreateTime(now);

        Long postUserId = postService.lambdaQuery().eq(Post::getId, comment.getPostId())
                .eq(Post::getStatus, PostConstant.POST_STATUS_PASS_REVIEW)
                .select(Post::getUserId)
                .one()
                .getUserId();
        //判断文章是否存在
        if (postUserId == null) {
            return SaResult.error(POST_COMMENT_UNEXIST_MESSAGE);
        }

        //封装异步通知
        MessageDto messageDto = new MessageDto();
        messageDto.setSendId(userId);
        messageDto.setPostId(commentDto.getPostId());
        messageDto.setContent(commentDto.getContent());
        if (comment.getParentId() == null) {
            //若为一级评论，则评论的是文章
            comment.setReplyUserId(postUserId);

            messageDto.setUserId(postUserId);

        } else {
            //二级评论
            parentCommentUserId = lambdaQuery().eq(Comment::getId, comment.getParentId())
                    .select(Comment::getUserId)
                    .one()
                    .getUserId();

            //判断父评论是否属于对应文章
            boolean exists = lambdaQuery().eq(Comment::getId, comment.getParentId())
                    .eq(Comment::getPostId, comment.getPostId())
                    .exists();

            if (!exists) {
                return SaResult.error(POST_COMMENT_UNEXIST_MESSAGE);
            }

            comment.setReplyUserId(parentCommentUserId);

            messageDto.setUserId(parentCommentUserId);
            messageDto.setPostUserId(postUserId);
        }

        //插入评论
        save(comment);

        // 异步通知被评论用户（用户评论/回复自己时，不发通知）
        // 1. 一级评论，评论自己的帖子 → 不发
        if (comment.getParentId() == null) {
            if (userId == postUserId) {
                return SaResult.ok(POST_COMMENT_SUCCESS_MESSAGE);
            }
        }
        // 2. 二级评论（回复别人），判断是否是 自己回复自己
        if (comment.getParentId() != null) {
            // 回复自己 → 不发
            if (userId == parentCommentUserId) {
                return SaResult.ok(POST_COMMENT_SUCCESS_MESSAGE);
            }
        }

        // 3. 不属于以上情况 → 正常发通知
        String postCommentMessageQueue = RabbitMQConstant.POST_COMMENT_MESSAGE_QUEUE;
        rabbitTemplate.convertAndSend(postCommentMessageQueue, messageDto);

        return SaResult.ok(POST_COMMENT_SUCCESS_MESSAGE);
    }

    @Override
    @Transactional
    public SaResult removeCommentByCommentId(Long commentId) {

        long userId = StpUtil.getLoginIdAsLong();

        //查询评论文章用户
        Comment comment = getById(commentId);
        if (comment == null) {
            return SaResult.error(POST_COMMENT_UNEXIST_MESSAGE);
        }

        Long postUserId = postService.lambdaQuery().select(Post::getUserId)
                .eq(Post::getId, comment.getPostId())
                .one()
                .getUserId();

        //判断该评论是否属于该用户或者文章作者或者管理员
        boolean exists = lambdaQuery().eq(Comment::getUserId, userId)
                .eq(Comment::getId, commentId)
                .exists();
        if (!exists && userId != postUserId && StpUtil.getSession().getInt("role") == 1) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        //删除业务(删除子评论)
        lambdaUpdate().eq(Comment::getId, commentId)
                .or()
                .eq(Comment::getParentId, commentId).remove();

        return SaResult.ok(CommentMessage.POST_COMMENT_REMOVE_MESSAGE);
    }

    @Override
    public SaResult listCommentsByPostId(Long postId, Long cursor) {

        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }

        //判断 post 是否存在
        boolean exists = postService.lambdaQuery().eq(Post::getId, postId)
                .exists();
        if (!exists) {
            return SaResult.error(PostMessage.POST_NOT_EXIST);
        }

        //查询一级评论
        List<Comment> comments = lambdaQuery().eq(Comment::getPostId, postId)
                .isNull(Comment::getParentId)
                .lt(Comment::getId, cursor)
                .orderByDesc(Comment::getLikeCount, Comment::getId)
                .last("limit " + ScrollConstant.SCROLL_LIMIT)
                .list();

        ScrollResult<CommentVo> commentScrollResult = new ScrollResult<>();

        //判断评论是否到底
        if (CollectionUtils.isNotEmpty(comments)) {
            long minId = comments.get(comments.size() - 1).getId();
            commentScrollResult.setCursor(String.valueOf(minId));
        } else {
            // 没有数据，游标设为 -1，告诉前端结束
            commentScrollResult.setCursor("-1");
            commentScrollResult.setList(Collections.emptyList());
            return SaResult.data(commentScrollResult);
        }

        //远程查评论的用户信息
        List<CommentVo> commentVos = getCommentVos(comments);
        commentScrollResult.setList(commentVos);

        return SaResult.data(commentScrollResult);
    }

    @Override
    public SaResult listCommentsByCommentId(Long commentId, Long cursor) {

        if (cursor == null) {
            cursor = 0L;
        }

        //判断父评论是否存在
        boolean exists = lambdaQuery().eq(Comment::getId, commentId)
                .exists();
        if (!exists) {
            return SaResult.error(POST_COMMENT_UNEXIST_MESSAGE);
        }

        //查子评论
        List<Comment> comments = lambdaQuery().eq(Comment::getParentId, commentId)
                .gt(Comment::getId, cursor)
                .orderByAsc(Comment::getLikeCount, Comment::getCreateTime)
                .last("limit " + ScrollConstant.SCROLL_LIMIT)
                .list();

        //封装滚动返回值
        ScrollResult<CommentVo> commentVoScrollResult = new ScrollResult<>();

        if (CollectionUtil.isNotEmpty(comments)) {
            //若有数据
            long minId = comments.get(comments.size() - 1).getId();
            commentVoScrollResult.setCursor(String.valueOf(minId));

        } else {
            //若滚动到底,cursor设置为-1
            commentVoScrollResult.setCursor("-1");
            commentVoScrollResult.setList(Collections.emptyList());
            return SaResult.data(commentVoScrollResult);

        }

        //若有数据，查询评论对应用户信息
        List<CommentVo> commentVos = getCommentVos(comments);

        //拼回复对象的用户名
        //取出所有被评论用户的 userId
        List<String> replyUserIds = comments.stream().map(comment -> comment.getReplyUserId().toString()).distinct().toList();

        //批量查询 user 获取 username
        SaResult replyUserResult = userFeignClient.batchQueryUserByUserId(replyUserIds);
        Map<String, UserVo> userVoMap = BeanUtil.copyToList((List<?>) replyUserResult.getData(), UserVo.class)
                .stream().collect(Collectors.toMap(UserVo::getId, u -> u));

        commentVos.forEach(c -> {
            UserVo userVo = userVoMap.get(c.getReplyUserId());
            c.setReplyToUsername(userVo.getUsername());
        });

        commentVoScrollResult.setList(commentVos);

        return SaResult.data(commentVoScrollResult);
    }

    @Override
    public Long countComments() {

        return count();
    }

    @Override
    public SaResult listAllCommentsByPostId(Long postId) {


        //判断 post 是否存在
        Post post = postService.lambdaQuery().eq(Post::getId, postId)
                .one();
        if (post == null) {
            return SaResult.error(PostMessage.POST_NOT_EXIST);
        }

        //查询评论
        List<Comment> comments = lambdaQuery().eq(Comment::getPostId, postId)
                .orderByDesc(Comment::getLikeCount, Comment::getId)
                .list();

        if (comments.isEmpty()) {
            return SaResult.data(Collections.emptyList());
        }

        //若有数据，查询评论对应用户信息
        List<CommentVo> commentVos = getCommentVos(comments);

        //拼回复对象的用户名
        //取出所有被评论用户的 userId
        List<String> replyUserIds = comments.stream()
                .filter(comment -> comment.getParentId() != null)
                .map(comment -> comment.getReplyUserId().toString())
                .distinct().toList();

        //如果没有子评论就不传
        if (replyUserIds.isEmpty()) {
            return SaResult.data(commentVos);

        }

        //批量查询 user 获取 username
        SaResult replyUserResult = userFeignClient.batchQueryUserByUserId(replyUserIds);
        Map<String, UserVo> userVoMap = BeanUtil.copyToList((List<?>) replyUserResult.getData(), UserVo.class)
                .stream().collect(Collectors.toMap(UserVo::getId, u -> u));

        commentVos.forEach(c -> {
            c.setPostUserId(String.valueOf(post.getUserId()));
            if (c.getParentId() != null) {
                UserVo userVo = userVoMap.get(c.getReplyUserId());
                c.setReplyToUsername(userVo.getUsername());
            }
        });


        return SaResult.data(commentVos);
    }

    @Override
    public Long countCommentsByPostId(Long postId) {

        return count(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
    }

    /**
     * 封装评论
     *
     * @param comments
     * @return
     */
    private @NonNull List<CommentVo> getCommentVos(List<Comment> comments) {
        String userId = StpUtil.getLoginIdAsString();
        List<String> commentUserIds = comments.stream().map(comment -> comment.getUserId().toString()).toList();
        SaResult saResult = userFeignClient.batchQueryUserByUserId(commentUserIds);
        Map<String, UserVo> userVoMap = BeanUtil.copyToList((List<?>) saResult.getData(), UserVo.class)
                .stream().collect(Collectors.toMap(UserVo::getId, u -> u));

        List<CommentVo> commentVos = BeanUtil.copyToList(comments, CommentVo.class);
        commentVos.forEach(c -> {
            UserVo userVo = userVoMap.get(c.getUserId());
            c.setAvatar(userVo.getAvatar());
            c.setRole(userVo.getRole());
            c.setUsername(userVo.getUsername());
            c.setLevel(userVo.getLevel());

            //判断当前用户是否点赞
            String redisKey = RedisKey.POST_COMMENT_LIKES_REDIS_KEY + c.getId();
            Boolean member = redisTemplate.opsForSet().isMember(redisKey, userId);
            if (Boolean.TRUE.equals(member)) {
                c.setIsLike(1);
            } else {
                c.setIsLike(0);
            }
        });
        return commentVos;
    }
}
