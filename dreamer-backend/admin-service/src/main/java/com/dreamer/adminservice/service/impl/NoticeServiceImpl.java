package com.dreamer.adminservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.feign.MessageFeignClient;
import com.dreamer.adminservice.service.INoticeService;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.message.SystemMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.dreamer.adminservice.message.NoticeMessage.NOTICE_ADD_SUCCESS;
import static com.dreamer.adminservice.message.NoticeMessage.NOTICE_REMOVE_SUCCESS;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements INoticeService {

    private final MessageFeignClient messageFeignClient;

    @Override
    public SaResult addNotice(String content) {

        //远程添加消息模板（公告）
        long adminId = StpUtil.getLoginIdAsLong();
        MessageDto messageDto = MessageDto.builder()
                .sendId(adminId)
                .content(content)
                .build();
        SaResult saResult = messageFeignClient.addNotice(messageDto);
        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        return SaResult.ok(NOTICE_ADD_SUCCESS);
    }

    @Override
    public SaResult listNotice() {

        SaResult saResult = messageFeignClient.listNotice();
        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        return SaResult.data(saResult.getData());
    }

    @Override
    public SaResult removeNotice(Long noticeId) {

        SaResult saResult = messageFeignClient.removeNotice(noticeId);
        if (saResult.getCode() == 500) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        return SaResult.ok(NOTICE_REMOVE_SUCCESS);
    }
}
