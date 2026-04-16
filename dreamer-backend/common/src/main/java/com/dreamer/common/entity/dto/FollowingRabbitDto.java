package com.dreamer.common.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowingRabbitDto {

    private String fansUsername;

    private Long fansId;

    private Long followedId;
}
