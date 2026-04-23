package com.dreamer.postservice.consumer;

import com.dreamer.postservice.service.ICommentService;
import com.dreamer.postservice.service.ILikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.dreamer.common.constant.RabbitMQConstant.POST_DELETE_COMMENT_LIKE_QUEUE;

@Component
@RequiredArgsConstructor
public class PostConsumer {

    private ICommentService commentService;
    private ILikesService likesService;

    /**
     * 异步删除文章评论和点赞
     * @param postId
     */
    @RabbitListener(
            queues = POST_DELETE_COMMENT_LIKE_QUEUE
    )
    public void delCommentsAndLikesByPostId(long postId) {
        commentService.delCommentsByPostId(postId);
        likesService.delLikesByPostId(postId);
    }

}
