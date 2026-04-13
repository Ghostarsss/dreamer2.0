package com.dreamer.userservice.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.dreamer.common.entity.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo extends BaseEntity {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号（邮箱）
     */
    private String email;

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
     * 计算后的 level 等级
     */
    private Integer level;
}
