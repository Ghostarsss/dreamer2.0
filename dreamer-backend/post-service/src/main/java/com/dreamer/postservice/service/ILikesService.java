package com.dreamer.postservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.postservice.entity.pojo.Likes;

public interface ILikesService extends IService<Likes> {

    void delLikesByPostId(long postId);

    SaResult likePost(Long postId);

    void likePostDbSync(MessageDto messageDto);

    SaResult likeComment(Long commentId);
}
