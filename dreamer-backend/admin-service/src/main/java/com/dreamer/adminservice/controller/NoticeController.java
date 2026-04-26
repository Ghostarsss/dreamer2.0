package com.dreamer.adminservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.service.INoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notices")
public class NoticeController {

    private final INoticeService noticeService;

    /**
     * 管理员添加公告
     * @param param
     * @return
     */
    @PostMapping
    public SaResult addNotice(@RequestBody HashMap<String, String> param) {
        String content = param.get("content");
        return noticeService.addNotice(content);
    }

    /**
     * 查看历史公告
     * @return
     */
    @GetMapping
    public SaResult listNotice() {
        return noticeService.listNotice();
    }

    /**
     * 删除公告
     * @return
     */
    @DeleteMapping("/{noticeId}")
    public SaResult removeNotice(@PathVariable Long noticeId) {
        return noticeService.removeNotice(noticeId);
    }

}
