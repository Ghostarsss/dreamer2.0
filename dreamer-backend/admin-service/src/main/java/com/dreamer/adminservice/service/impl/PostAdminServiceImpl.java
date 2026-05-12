package com.dreamer.adminservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dreamer.adminservice.feign.PostFeignClient;
import com.dreamer.adminservice.service.IPostAdminService;
import com.dreamer.common.constant.MessageConstant;
import com.dreamer.common.constant.RabbitMQConstant;
import com.dreamer.common.entity.dto.MessageDto;
import com.dreamer.common.entity.vo.PostVo;
import com.dreamer.common.message.SystemMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.dreamer.adminservice.message.PostMessage.POST_CHECK_SUCCESS;
import static com.dreamer.common.constant.PostConstant.POST_STATUS_PASS_REVIEW;

@Service
@RequiredArgsConstructor
public class PostAdminServiceImpl implements IPostAdminService {

    private final PostFeignClient postFeignClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public SaResult pagePendingPosts(Integer page) {

        Page<PostVo> postVoPage = postFeignClient.pagePendingPosts(page);
        if (postVoPage == null) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        return SaResult.data(postVoPage);
    }

    @Override
    public SaResult checkPost(Long postId, Integer status) {

        SaResult saResult = postFeignClient.checkPost(postId, status);
        if (saResult.getCode() == 500) {
            return SaResult.error(saResult.getMsg());
        }

        //远程查询 post 用户 id
        SaResult postResult = postFeignClient.queryPostByPostId(postId);
        PostVo postVo = BeanUtil.copyProperties(postResult.getData(), PostVo.class);

        //异步发送审核结果通知给用户
        MessageDto messageDto = MessageDto.builder()
                .userId(Long.valueOf(postVo.getUserId()))
                .postId(postId)
                .status(status)
                .content(status.equals(POST_STATUS_PASS_REVIEW) ? "您的文章审核通过" : "您的文章被驳回")
                .type(MessageConstant.POST_CHECK_MESSAGE_TYPE)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConstant.ADMIN_POST_CHECK_MESSAGE_QUEUE, messageDto);

        //若文章审核通过，异步增加作者经验值
        if (status.equals(POST_STATUS_PASS_REVIEW)) {
            messageDto.setType(MessageConstant.LEVEL_MESSAGE_TYPE);
            rabbitTemplate.convertAndSend(RabbitMQConstant.POST_PASS_EXP_INCREMENT_QUEUE, messageDto);
        }

        return SaResult.ok(POST_CHECK_SUCCESS);
    }
}
