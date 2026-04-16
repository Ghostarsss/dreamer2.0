package com.dreamer.userservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.userservice.entity.pojo.UserFollow;

public interface IFollowingService extends IService<UserFollow> {

    SaResult followByUserId(String userId);

    SaResult unFollowByUserId(String userId);

    SaResult listMeFollowing(Long cursor, Integer offset);

    SaResult listMeFans(Long cursor, Integer offset);

    SaResult removeFansByUserId(long userId);
}
