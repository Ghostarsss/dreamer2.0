package com.dreamer.postservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

        //删除数据库评论
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

        //封装评论
        LocalDateTime now = LocalDateTime.now();
        Comment comment = BeanUtil.copyProperties(commentDto, Comment.class);
        comment.setUserId(userId);
        comment.setCreateTime(now);

        Long postUserId = postService.lambdaQuery().eq(Post::getId, comment.getPostId())
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
        if (comment.getParentId() == null) {
            //若为一级评论，则评论的是文章
            comment.setReplyUserId(postUserId);

            messageDto.setUserId(postUserId);

        } else {
            //二级评论
            Long parentCommentUserId = lambdaQuery().eq(Comment::getId, comment.getParentId())
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
        //文章评论数+1
        postService.lambdaUpdate().eq(Post::getId, comment.getPostId())
                .setSql("comment_count = comment_count + 1").update();

        //异步通知被评论用户(用户评论自己时不能给自己发通知)
        if (userId != postUserId) {
            String postCommentMessageQueue = RabbitMQConstant.POST_COMMENT_MESSAGE_QUEUE;
            rabbitTemplate.convertAndSend(postCommentMessageQueue, messageDto);
        }

        return SaResult.ok(POST_COMMENT_SUCCESS_MESSAGE);
    }

    @Override
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

        //判断该评论是否属于该用户或者文章作者
        boolean exists = lambdaQuery().eq(Comment::getUserId, userId)
                .eq(Comment::getId, commentId)
                .exists();
        if (!exists && userId != postUserId) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        //删除业务
        lambdaUpdate().eq(Comment::getId, commentId).remove();

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
            commentScrollResult.setCursor(minId);
        } else {
            // 没有数据，游标设为 -1，告诉前端结束
            commentScrollResult.setCursor(-1L);
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
            cursor = Long.MAX_VALUE;
        }

        //判断父评论是否存在
        boolean exists = lambdaQuery().eq(Comment::getId, commentId)
                .exists();
        if (!exists) {
            return SaResult.error(POST_COMMENT_UNEXIST_MESSAGE);
        }

        //查 子评论
        List<Comment> comments = lambdaQuery().eq(Comment::getParentId, commentId)
                .lt(Comment::getId, cursor)
                .orderByDesc(Comment::getLikeCount, Comment::getCreateTime)
                .last("limit " + ScrollConstant.SCROLL_LIMIT)
                .list();

        //封装滚动返回值
        ScrollResult<CommentVo> commentVoScrollResult = new ScrollResult<>();

        if (CollectionUtil.isNotEmpty(comments)) {
            //若有数据
            long minId = comments.get(comments.size() - 1).getId();
            commentVoScrollResult.setCursor(minId);

        } else {
            //若滚动到底,cursor设置为-1
            commentVoScrollResult.setCursor(-1L);
            commentVoScrollResult.setList(Collections.emptyList());
            return SaResult.data(commentVoScrollResult);

        }

        //若有数据，查询评论对应用户信息
        List<CommentVo> commentVos = getCommentVos(comments);
        commentVoScrollResult.setList(commentVos);

        return SaResult.data(commentVoScrollResult);
    }

    /**
     * 封装评论
     * @param comments
     * @return
     */
    private @NonNull List<CommentVo> getCommentVos(List<Comment> comments) {
        List<Long> commentUserIds = comments.stream().map(Comment::getUserId).toList();
        SaResult saResult = userFeignClient.batchQueryUserByUserId(commentUserIds);
        Map<Long, UserVo> userVoMap = BeanUtil.copyToList((List<?>) saResult.getData(), UserVo.class)
                .stream().collect(Collectors.toMap(UserVo::getId, u -> u));

        List<CommentVo> commentVos = BeanUtil.copyToList(comments, CommentVo.class);
        commentVos.forEach(c -> {
            UserVo userVo = userVoMap.get(c.getUserId());
            c.setAvatar(userVo.getAvatar());
            c.setRole(userVo.getRole());
            c.setUsername(userVo.getUsername());
            c.setLevel(userVo.getLevel());
        });
        return commentVos;
    }
}
