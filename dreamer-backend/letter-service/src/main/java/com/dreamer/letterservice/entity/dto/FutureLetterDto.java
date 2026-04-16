package com.dreamer.letterservice.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FutureLetterDto {

    /**
     * 信件内容
     */
    private String content;

    /**
     * 图片
     */
    private String img;

    /**
     * 指定开信时间
     */
    private LocalDate openTime;

    /**
     * 0不公开 1公开
     */
    private Integer isPublic;
}
