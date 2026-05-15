package com.dreamer.adminservice.service.impl;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dreamer.adminservice.entity.pojo.Statistics;
import com.dreamer.adminservice.feign.LetterFeignClient;
import com.dreamer.adminservice.feign.PostFeignClient;
import com.dreamer.adminservice.feign.UserFeignClient;
import com.dreamer.adminservice.mapper.StatisticsMapper;
import com.dreamer.adminservice.service.IStatisticsService;
import com.dreamer.common.message.SystemMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl extends ServiceImpl<StatisticsMapper, Statistics> implements IStatisticsService {

    private final UserFeignClient userFeignClient;
    private final PostFeignClient postFeignClient;
    private final LetterFeignClient letterFeignClient;
    private final StringRedisTemplate redisTemplate;

    @Override
    public SaResult getStatistics() {
        //远程查询总用户数
        Long userCont = userFeignClient.countUsers();

        //总文章数和评论数
        Long postCount = postFeignClient.countPosts();
        Long commentCount = postFeignClient.countComments();

        //总信件数
        Long letterCount = letterFeignClient.countLetters();

        //总浏览量
        String view = redisTemplate.opsForValue().get("statistics:view");

        //封装
        Statistics statistics = Statistics.builder()
                .userCount(userCont)
                .postCount(postCount)
                .commentCount(commentCount)
                .letterCount(letterCount)
                .totalViews(Long.valueOf(view))
                .createTime(LocalDateTime.now())
                .build();

        boolean save = save(statistics);
        if (!save) {
            return SaResult.error(SystemMessage.SYSTEM_ERROR);
        }

        return SaResult.data(statistics);
    }

    @Override
    public String getView() {

        return redisTemplate.opsForValue().get("statistics:view");
    }
}
