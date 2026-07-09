-- ----------------------------
-- 微信用户绑定表
-- ----------------------------
DROP TABLE IF EXISTS `sys_wechat_user`;
CREATE TABLE `sys_wechat_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '系统用户ID',
    `openid` varchar(64) NOT NULL COMMENT '微信OpenID',
    `nickname` varchar(50) DEFAULT NULL COMMENT '微信昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '微信头像',
    `subscribe` tinyint DEFAULT 0 COMMENT '是否关注：0-否 1-是',
    `subscribe_time` datetime DEFAULT NULL COMMENT '关注时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户绑定表';
