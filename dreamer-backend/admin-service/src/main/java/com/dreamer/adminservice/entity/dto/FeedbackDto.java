package com.dreamer.adminservice.entity.dto;

import lombok.Data;

@Data
public class FeedbackDto {

    /**
     * 反馈类型：0 bug，1 建议
     */
    private Integer type;

    private String content;

}
