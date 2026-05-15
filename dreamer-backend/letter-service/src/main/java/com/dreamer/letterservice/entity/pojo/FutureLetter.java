package com.dreamer.letterservice.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 未来信件表 实体类
 */
@Data
@TableName("future_letter")
public class FutureLetter {

    /**
     * 信件ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 信件所属用户ID，一个用户只能写一封
     */
    private Long userId;

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
     * 是否已开启 0未开启 1已开启
     */
    private Integer isOpen;

    /**
     * 0不公开 1公开（到时间后可展示）
     */
    private Integer isPublic;

    /**
     * 软删除时间
     */
    @TableLogic(value = "null",delval = "now()")
    private LocalDateTime deletedAt;

    /**
     * 信件创建时间，由后端插入
     */
    private LocalDateTime createTime;
}