package com.dreamer.adminservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.adminservice.feign.MessageFeignClient;
import com.dreamer.adminservice.feign.UserFeignClient;
import com.dreamer.adminservice.service.IUserAdminService;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.pojo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements IUserAdminService {

    private final UserFeignClient userFeignClient;
    private final MessageFeignClient messageFeignClient;

    @Override
    public SaResult pageUsers(Integer page) {

        //远程分页查询用户信息
        Page<User> userPage = userFeignClient.listUsers(page);

        return SaResult.data(userPage);
    }

    @Override
    public SaResult changeRole(Long userId, Integer role) {

        SaResult saResult = userFeignClient.changeRole(userId, role);

        return SaResult.ok(saResult.getMsg());
    }

    @Override
    public SaResult editPassword(Long userId, String password) {

        userFeignClient.editPassword(userId, password);

        return SaResult.ok("用户密码修改成功");

    }

    @Override
    public SaResult banUser(Long userId) {

        SaResult saResult = userFeignClient.banUser(userId);
        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        return SaResult.ok(saResult.getMsg());
    }

    @Override
    public SaResult delUser(Long userId) {

        SaResult saResult = userFeignClient.delUser(userId);

        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

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
