package com.dreamer.common.entity.dto;

import lombok.Data;

@Data
public class UserDto {


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
     * 验证码
     */
    private String verifyCode;
}
