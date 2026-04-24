package com.dreamer.adminservice.feign;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.MessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "message-service")
public interface MessageFeignClient {

    @PostMapping("/messages/admin/notify")
    public SaResult adminNotifyUser(MessageDto messageDto);

}
