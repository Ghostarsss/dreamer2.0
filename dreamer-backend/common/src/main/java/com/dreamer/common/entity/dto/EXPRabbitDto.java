package com.dreamer.common.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EXPRabbitDto {

    private Long userId;

    private Integer expIncrement;

    private Long causeUserId;
}
