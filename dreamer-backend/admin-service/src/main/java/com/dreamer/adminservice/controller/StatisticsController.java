package com.dreamer.adminservice.controller;

import cn.dev33.satoken.util.SaResult;
import com.dreamer.adminservice.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    /**
     * 查询统计数据
     * @return
     */
    @GetMapping
    public SaResult getStatistics() {
        return statisticsService.getStatistics();
    }

}
