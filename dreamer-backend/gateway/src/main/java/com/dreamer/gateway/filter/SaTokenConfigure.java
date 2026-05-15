package com.dreamer.gateway.filter;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.constant.UserConstant;
import com.dreamer.common.message.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.dreamer.common.constant.UserConstant.USER_ADMIN_ROLE;
import static com.dreamer.common.constant.UserConstant.USER_SUPER_ADMIN_ROLE;

/**
 * [Sa-Token 权限认证] 配置类
 *
 * @author click33
 */
@Configuration
public class SaTokenConfigure {
    // 注册 Sa-Token全局过滤器

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")    /* 拦截全部path */
                // 开放地址
                .addExclude("/register/**","/posts/new","/posts/hot","/comments/**","/letters/opened","/system/**")
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除/Login 用于开放登录
                    SaRouter.match("/**", "/login", r -> StpUtil.checkLogin());

                    SaRouter.match("/admin/**", r -> StpUtil.checkRoleOr(USER_ADMIN_ROLE, USER_SUPER_ADMIN_ROLE));
                })
                // 异常处理方法：每次setAuth函数出现异常时进入
                .setError(e -> {
                    return SaResult.error(e.getMessage());
                });
    }
}
