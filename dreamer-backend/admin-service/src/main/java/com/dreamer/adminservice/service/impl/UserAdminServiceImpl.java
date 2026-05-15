package com.dreamer.adminservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.adminservice.entity.pojo.Feedback;
import com.dreamer.adminservice.feign.MessageFeignClient;
import com.dreamer.adminservice.feign.UserFeignClient;
import com.dreamer.adminservice.service.IFeedbackService;
import com.dreamer.adminservice.service.IUserAdminService;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.pojo.User;
import com.dreamer.common.message.SystemMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements IUserAdminService {

    private final UserFeignClient userFeignClient;
    private final MessageFeignClient messageFeignClient;
    private final IFeedbackService feedbackService;

    @Override
    public SaResult pageUsers(Integer page) {

        //远程分页查询用户信息
        Page<User> userPage = userFeignClient.listUsers(page);

        return SaResult.data(userPage);
    }

    @Override
    public SaResult changeRole(Long userId, Integer role) {
        //只有超级管理员可以进行此操作
        int currentRole = StpUtil.getSession().getInt("role");
        if (currentRole != 3) {
            return SaResult.error(SystemMessage.NO_PERMISSION);
        }

        SaResult saResult = userFeignClient.changeRole(userId, role);

        return SaResult.ok(saResult.getMsg());
    }

    @Override
    public SaResult editPassword(Long userId, String password) {

        //只有超级管理员可以进行此操作
        int role = StpUtil.getSession().getInt("role");
        if (role != 3) {
            return SaResult.error(SystemMessage.NO_PERMISSION);
        }

        userFeignClient.editPassword(userId, password);

        //该用户提出登录
        String tokenValueByLoginId = StpUtil.getTokenValueByLoginId(userId);
        StpUtil.logoutByTokenValue(tokenValueByLoginId);

        return SaResult.ok("用户密码修改成功");

    }

    @Override
    public SaResult banUser(Long userId) {

        //无法禁用超级管理员
        int role = StpUtil.getSessionByLoginId(userId).getInt("role");
        if (role == 3) {
            return SaResult.error("无法禁用超级管理员");
        }

        SaResult saResult = userFeignClient.banUser(userId);
        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        return SaResult.ok(saResult.getMsg());
    }

    @Override
    public SaResult delUser(Long userId) {

        //只有超级管理员可以进行此操作
        int role = StpUtil.getSession().getInt("role");
        if (role != 3) {
            return SaResult.error(SystemMessage.NO_PERMISSION);
        }

        SaResult saResult = userFeignClient.delUser(userId);

        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        //删除该用户的反馈
        feedbackService.remove(new LambdaQueryWrapper<Feedback>().eq(Feedback::getUserId, userId));

        return SaResult.ok(saResult.getMsg());
    }

    @Override
    public SaResult adminNotifyUser(Long userId, String content) {

        String adminName = StpUtil.getSession().getString("username");

        MessageDto messageDto = MessageDto.builder()
                .content("「管理员」" + adminName + " 通知您: " + content)
                .sendId(userId)
                .userId(StpUtil.getLoginIdAsLong())
                .build();

        SaResult saResult = messageFeignClient.adminNotifyUser(messageDto);

        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }
        return SaResult.ok(saResult.getMsg());
    }
}
