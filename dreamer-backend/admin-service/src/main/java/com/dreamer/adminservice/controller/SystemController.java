package com.dreamer.adminservice.controller;

import com.dreamer.adminservice.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemController {
    @Autowired
    private IStatisticsService statisticsService;

    /**
     * 查询网站浏览量
     * @return
     */
    @GetMapping("/view")
    public String getView() {
        return statisticsService.getView();
    }
}
