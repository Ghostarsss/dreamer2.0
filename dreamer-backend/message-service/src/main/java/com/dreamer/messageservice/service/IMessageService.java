package com.dreamer.messageservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.pojo.MessageTemplate;

public interface IMessageService extends IService<MessageTemplate> {

    void registerMessage(MessageDto messageDto);

    SaResult messageCount(String isRead);

    SaResult listMessage();
}
