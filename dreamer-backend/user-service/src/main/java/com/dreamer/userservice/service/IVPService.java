package com.dreamer.userservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.EXPRabbitDto;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.pojo.User;

public interface IVPService extends IService<User> {

    SaResult sign();

    SaResult isSign();

    SaResult buyEXPByProton(Integer proton);

    SaResult uploadImagesWithProtonCost(long userId);

    void followingEXP(EXPRabbitDto expRabbitDto);

    void postLikedEXP(MessageDto messageDto);

    SaResult deductProton(Long userId, Integer protons);

    void postPassEXP(MessageDto messageDto);
}
