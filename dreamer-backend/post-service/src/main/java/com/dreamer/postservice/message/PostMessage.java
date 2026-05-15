package com.dreamer.postservice.message;

public class PostMessage {

    public static final String POST_SAVE_SUCCESS_MESSAGE = "文章提交成功，等待管理员审核...";
    public static final String POST_NOT_EXIST = "文章不存在";
    public static final String POST_DELETE_SUCCESS_MESSAGE = "文章删除成功";
    public static final String POST_UPDATE_SUCCESS_MESSAGE = "文章修改已提交审核";
    public static final String POST_PROTON_EXCEED_MAX = "支持「质子」数超过最大值";
    public static final String POST_IS_TIP_PROTON = "您已打赏过文章，无法重复操作";
    public static final String POST_PROTON_EXCEED_UPDATE = "文章「质子」支持数大于 20，无法修改";
    public static final String POST_DELETE_AUTH_ERROR = "您没有权限删除";
}
