package com.dreamer.common.constant;

public class RabbitMQConstant {

    public static final String AUTH_REGISTER_MESSAGE_QUEUE = "auth.register.message.queue";

    public static final String USER_FOLLOWING_MESSAGE_QUEUE = "user.following.message.queue";

    public static final String USER_FOLLOWING_EXP_QUEUE = "user.following.exp.queue";

    public static final String LETTER_TO_BE_OPENED_QUEUE = "letter.toBeOpened.queue";

    public static final String LETTER_TO_BE_OPENED_KEY = "letter.toBeOpened.key";

    public static final String DELAY_EXCHANGE_NAME = "delay-exchange";

    public static final String POST_DELETE_COMMENT_LIKE_QUEUE = "post.delete.comment.like.queue";

    public static final String POST_LIKE_INCREMENT_EXP_QUEUE = "post.like.increment.exp.queue";

    public static final String POST_LIKE_MESSAGE_QUEUE = "post.like.message.queue";

    public static final String POST_LIKE_DB_SYNC_QUEUE = "post.like.db.sync.queue";

    public static final String POST_COMMENT_MESSAGE_QUEUE = "post.comment.message.queue";

    public static final String POST_TIP_PROTON_MESSAGE_QUEUE = "post.tip.proton.message.queue";

    public static final String FEEDBACK_SUBMIT_MESSAGE_QUEUE = "feedback.submit.message.queue";

    public static final String ADMIN_DELETE_USER_POST_QUEUE = "admin.delete.user.post.queue";

    public static final String ADMIN_DELETE_USER_LETTER_QUEUE = "admin.delete.user.letter.queue";

    public static final String ADMIN_DELETE_USER_FOLLOWING_QUEUE = "admin.delete.user.following.queue";

    public static final String ADMIN_DELETE_USER_EXCHANGE = "admin.delete.user.exchange";

    public static final String ADMIN_POST_CHECK_MESSAGE_QUEUE = "admin.post.check.message.queue";

    public static final String POST_PASS_EXP_INCREMENT_QUEUE = "post.pass.exp.increment.queue";
}
