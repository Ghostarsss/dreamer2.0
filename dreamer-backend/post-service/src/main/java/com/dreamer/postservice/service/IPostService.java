package com.dreamer.postservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.postservice.entity.pojo.Post;

public interface IPostService extends IService<Post> {
    SaResult addPost(String content);

    SaResult delPost(long postId);

    SaResult listMyPost(Long cursor);

    SaResult updatePostByPostId(Long postId, String content);

    SaResult queryPostByPostId(Long postId);

    SaResult listNewPosts(Long cursor);

    SaResult listHotPosts(Long cursor, Integer offset);

    SaResult listFollowingPost(Long cursor);

    SaResult protonPost(Long postId, Integer protons);

    void delPostByUserId(Long userId);
}
