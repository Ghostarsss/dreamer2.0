package com.dreamer.common.entity.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.dreamer.common.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 账号（邮箱）
     */
    private String email;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别：1男/0女/2未知
     */
    private Integer gender;

    /**
     * 简介
     */
    private String bio;

    /**
     * 用户角色：1普通用户，2管理员，3超级管理员
     */
    private Integer role;

    /**
     * 虚拟货币“质子”数量
     */
    private Integer proton;

    /**
     * 用户经验值（EXP）
     */
    private Integer exp;

    /**
     * 0正常 1封禁
     */
    private Integer status;

    /**
     * 逻辑删除：未删除=NULL，已删除=删除时间
     */
    @TableLogic(value = "null", delval = "now()")
    private LocalDateTime deletedAt;
}