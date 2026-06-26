package com.mojian.utils;

import cn.dev33.satoken.secure.BCrypt;

/**
 * 密码生成工具类
 * 用于生成 BCrypt 加密的密码
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        // 【修改这里】设置你想要的密码
        String password = "123456";
        
        // 生成 BCrypt 加密的密码
        String encodedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        System.out.println("========================================");
        System.out.println("密码生成工具");
        System.out.println("========================================");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encodedPassword);
        System.out.println("========================================");
        System.out.println();
        System.out.println("使用方法：");
        System.out.println("1. 复制上面的 BCrypt 加密值");
        System.out.println("2. 在 SQL 中使用：");
        System.out.println();
        System.out.println("INSERT INTO sys_user (username, password, nickname, status, create_time, update_time)");
        System.out.println("VALUES ('你的用户名', '" + encodedPassword + "', '你的昵称', 1, NOW(), NOW());");
        System.out.println();
        System.out.println("INSERT INTO sys_user_role (user_id, role_id) VALUES (LAST_INSERT_ID(), 2);");
        System.out.println("========================================");
    }
}
