package com.dreamer.adminservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.entity.dto.FeedbackDto;
import com.dreamer.adminservice.service.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final IFeedbackService feedbackService;

    /**
     * 提交反馈
     * @param feedbackDto
     * @return
     */
    @PostMapping
    public SaResult submitFeedback(@RequestBody FeedbackDto feedbackDto) {
        return feedbackService.submitFeedback(feedbackDto);
    }

    /**
     * 查询反馈
     * @param page
     * @return
     */
    @GetMapping("/admin")
    public SaResult pageFeedback(Integer page) {
        return feedbackService.pageFeedback(page);
    }

    /**
     * 管理员处理反馈
     * @param feedbackId
     * @return
     */
    @PostMapping("/admin/{feedbackId}")
    public SaResult replyFeedback(@PathVariable Long feedbackId, @RequestBody HashMap<String,String> param) {
        String reply = param.get("reply");
        return feedbackService.replyFeedback(feedbackId,reply);
    }

}
