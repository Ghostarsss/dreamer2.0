package com.dreamer.adminservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户反馈表 实体类
 */
@Data
@TableName("feedback")
public class Feedback {

    /**
     * 反馈ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 反馈用户ID
     */
    private Long userId;

    /**
     * 反馈类型：0 bug，1 建议
     */
    private Integer type;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 处理状态：0未处理，1已处理
     */
    private Integer status;

    /**
     * 处理回复内容
     */
    private String reply;

    /**
     * 反馈时间，由后端插入
     */
    private LocalDateTime createTime;

    /**
     * 处理更新时间，由后端插入
     */
    private LocalDateTime updateTime;
}