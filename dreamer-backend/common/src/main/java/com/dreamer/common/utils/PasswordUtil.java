package com.dreamer.common.utils;

/**
 * 密码格式校验工具类
 * 规则：长度6-15位，允许大小写字母 + 数字，任意组合都可以
 */
public class PasswordUtil {

    // 正则：6-15位，只允许字母和数字
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9]{6,15}$";

    /**
     * 校验密码格式是否合法
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.matches(PASSWORD_REGEX);
    }
}