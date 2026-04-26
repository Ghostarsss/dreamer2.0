package com.dreamer.adminservice.service;

import cn.dev33.satoken.util.SaResult;

public interface INoticeService {

    SaResult addNotice(String content);

    SaResult listNotice();

    SaResult removeNotice(Long noticeId);
}
