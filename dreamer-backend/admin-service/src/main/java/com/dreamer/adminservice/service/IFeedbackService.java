package com.dreamer.adminservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.adminservice.entity.dto.FeedbackDto;
import com.dreamer.adminservice.entity.pojo.Feedback;

public interface IFeedbackService extends IService<Feedback> {

    SaResult submitFeedback(FeedbackDto feedbackDto);

    SaResult pageFeedback(Integer page);

    SaResult replyFeedback(Long feedbackId, String reply);
}
