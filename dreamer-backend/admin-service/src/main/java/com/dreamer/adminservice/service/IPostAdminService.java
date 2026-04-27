package com.dreamer.adminservice.service;

import cn.dev33.satoken.util.SaResult;

public interface IPostAdminService {

    SaResult pagePendingPosts(Integer page);

    SaResult checkPost(Long postId, Integer status);
}
