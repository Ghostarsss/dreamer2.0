package com.dreamer.postservice.consumer;

import com.dreamer.postservice.service.ICommentService;
import com.dreamer.postservice.service.ILikesService;
import com.dreamer.postservice.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.dreamer.common.constant.RabbitMQConstant.*;

@Component
@RequiredArgsConstructor
public class PostConsumer {

    private final ICommentService commentService;
    private final ILikesService likesService;
    private final IPostService postService;

    /**
     * 异步删除文章评论和点赞
     * @param postId
     */
    @RabbitListener(
            queues = POST_DELETE_COMMENT_LIKE_QUEUE
    )
    public void delCommentsAndLikesByPostId(Long postId) {
        commentService.delCommentsByPostId(postId);
        likesService.delLikesByPostId(postId);
    }

    /**
     * 根据 userId 删除相关文章
     * @param userId
     */
    @RabbitListener(queues = ADMIN_DELETE_USER_POST_QUEUE)
    public void delPostByUserId(Long userId) {
        postService.delPostByUserId(userId);
    }

    /**
     * 根据 userId 删除相关评论
     * @param userId
     */
    @RabbitListener(queues = ADMIN_DELETE_USER_COMMENT_QUEUE)
    public void delCommentByUserId(Long userId) {
        commentService.delCommentByUserId(userId);
    }

}
