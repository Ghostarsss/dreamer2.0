package com.dreamer.messageservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.messageservice.entity.pojo.UserMessage;
import com.dreamer.messageservice.mapper.UserMessageMapper;
import com.dreamer.messageservice.service.IUserMessageService;
import org.springframework.stereotype.Service;

@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage> implements IUserMessageService {

}
