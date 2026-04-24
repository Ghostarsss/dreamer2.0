package com.dreamer.gateway.utils;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.dreamer.common.entity.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.dreamer.common.constant.UserConstant.*;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        //查询用户
        int userRole = StpUtil.getSession().getInt("role");
        List<String> list = new ArrayList<String>();

        //添加用户角色
        if (userRole == USER_ADMIN_ROLE_INTEGER) {
            list.add(USER_ADMIN_ROLE);
        } else if (userRole == USER_SUPER_ADMIN_ROLE_INTEGER) {
            list.add(USER_SUPER_ADMIN_ROLE);
        }

        return list;
    }

}
