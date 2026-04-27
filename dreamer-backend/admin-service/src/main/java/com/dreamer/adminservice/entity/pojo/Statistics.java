package com.dreamer.adminservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据统计表
 */
@Data
@TableName("statistics")
@Builder
public class Statistics {

    /**
     * 统计ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 总用户数
     */
    private Long userCount;

    /**
     * 总文章数
     */
    private Long postCount;

    /**
     * 总信件数
     */
    private Long letterCount;

    /**
     * 总评论数
     */
    private Long commentCount;

    /**
     * 网站总浏览量
     */
    private Long totalViews;

    /**
     * 记录时间
     */
    private LocalDateTime createTime;
}