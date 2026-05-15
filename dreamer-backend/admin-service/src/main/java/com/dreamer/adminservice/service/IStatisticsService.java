package com.dreamer.adminservice.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dreamer.adminservice.entity.pojo.Statistics;

public interface IStatisticsService extends IService<Statistics> {

    SaResult getStatistics();

    String getView();
}
