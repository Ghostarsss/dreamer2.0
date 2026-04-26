package com.dreamer.adminservice.feign;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.common.entity.dto.MessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@FeignClient(name = "message-service")
public interface MessageFeignClient {

    @PostMapping("/messages/admin/notify")
    public SaResult adminNotifyUser(MessageDto messageDto);

    @PostMapping("/messages/admin/notices")
    public SaResult addNotice(MessageDto messageDto);

    @GetMapping("/messages/admin/notices")
    SaResult listNotice();

    @DeleteMapping("/messages/admin/{noticeId}")
    SaResult removeNotice(@PathVariable Long noticeId);

    @PostMapping("/messages/admin/reply-feedback")
    SaResult replyFeedbackNotify(MessageDto messageDto);
}
