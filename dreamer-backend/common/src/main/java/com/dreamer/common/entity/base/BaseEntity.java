package com.dreamer.common.entity.base;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公共实体类
 */
@Data
public class BaseEntity {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}