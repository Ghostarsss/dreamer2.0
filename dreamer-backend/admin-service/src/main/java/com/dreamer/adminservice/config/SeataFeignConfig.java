package com.dreamer.adminservice.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * feign 拦截器，用户 seata 进行事务回滚
 */
@Configuration
@Slf4j
public class SeataFeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String xid = RootContext.getXID();
        if (StringUtils.isNotBlank(xid)) {
            log.info("feign传递分布式事务xid：{}", xid);
            requestTemplate.header(RootContext.KEY_XID, xid);
        }
    }
}