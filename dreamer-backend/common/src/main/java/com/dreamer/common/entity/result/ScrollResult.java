package com.dreamer.common.entity.result;

import lombok.Data;

import java.util.List;

@Data
public class ScrollResult<T> {
    private List<T> list;
    private long cursor;   // 下一次查询的最大 score
    private Integer offset; // 偏移量（解决 score 相同）
}