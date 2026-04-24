package com.dreamer.adminservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.entity.dto.FeedbackDto;
import com.dreamer.adminservice.service.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    @PostMapping
    public SaResult submitFeedback(@RequestBody FeedbackDto feedbackDto) {
        return feedbackService.submitFeedback(feedbackDto);
    }

}
