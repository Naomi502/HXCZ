package com.ruoyi.common.utils;

import com.ruoyi.common.exception.user.UserPasswordNotMatchException;

/**
 * 密码工具类
 * 用于验证密码复杂度等
 * 
 * @author ruoyi
 */
public class PasswordUtils {
    
    /**
     * 验证密码复杂度
     * 
     * @param password 密码
     * @throws UserPasswordNotMatchException 密码不符合复杂度要求
     */
    public static void validatePasswordComplexity(String password) {
        // 密码长度至少8位
        if (password == null || password.length() < 8) {
            throw new UserPasswordNotMatchException();
        }
        // 包含大写字母
        if (!password.matches("^.*[A-Z].*$")) {
            throw new UserPasswordNotMatchException();
        }
        // 包含小写字母
        if (!password.matches("^.*[a-z].*$")) {
            throw new UserPasswordNotMatchException();
        }
        // 包含数字
        if (!password.matches("^.*[0-9].*$")) {
            throw new UserPasswordNotMatchException();
        }
        // 包含特殊字符
        if (!password.matches("^.*[!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>\\/?].*$")) {
            throw new UserPasswordNotMatchException();
        }
    }
}