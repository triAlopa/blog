-- ========================================
-- 操作日志测试数据（可重复执行）
-- ========================================

-- 删除旧的测试数据
DELETE FROM sys_operate_log WHERE create_time >= '2025-01-01';

-- 插入1200条测试数据
INSERT INTO sys_operate_log (type, username, operation_name, module, request_url, request_method, request_params, response_code, ip, ip_source, user_agent, device_type, os, browser, spend_time, class_path, method_name, create_time)
SELECT 
    -- 日志类型：随机 admin/user
    CASE WHEN RAND() < 0.4 THEN 'admin' ELSE 'user' END,
    -- 用户名
    ELT(FLOOR(1 + RAND() * 10), 'admin', 'zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'testuser1', 'testuser2', 'testuser3', 'testuser4', 'testuser5'),
    -- 操作名称
    ELT(FLOOR(1 + RAND() * 20), '修改个人资料', '添加评论', '删除评论', '发布文章', '修改文章', '删除文章', '添加留言', '删除留言', '添加友链', '修改友链', '用户登录', '用户注册', '查看文章', '查看通知', '修改密码', '上传头像', '收藏文章', '点赞文章', '分享文章', '举报评论'),
    -- 模块
    ELT(FLOOR(1 + RAND() * 8), '个人中心', '评论', '文章', '留言', '友链', '通知', '登录', '注册'),
    -- 请求接口
    CONCAT('/api/', ELT(FLOOR(1 + RAND() * 5), 'user', 'article', 'comment', 'message', 'friend'), '/', ELT(FLOOR(1 + RAND() * 4), 'list', 'add', 'update', 'delete')),
    -- 请求方式
    ELT(FLOOR(1 + RAND() * 4), 'GET', 'POST', 'PUT', 'DELETE'),
    -- 请求参数
    CONCAT('{"id":', FLOOR(1 + RAND() * 1000), '}'),
    -- 响应状态码
    IF(RAND() < 0.9, 200, 500),
    -- IP地址
    CONCAT('192.168.', FLOOR(RAND() * 255), '.', FLOOR(RAND() * 255)),
    -- IP来源（使用标准省份名称格式）
    CONCAT('中国|', ELT(FLOOR(1 + RAND() * 34), 
        '北京市', '上海市', '天津市', '重庆市',
        '河北省', '山西省', '辽宁省', '吉林省', '黑龙江省',
        '江苏省', '浙江省', '安徽省', '福建省', '江西省', '山东省',
        '河南省', '湖北省', '湖南省', '广东省', '海南省',
        '四川省', '贵州省', '云南省', '陕西省', '甘肃省', '青海省',
        '台湾省', '内蒙古自治区', '广西壮族自治区', '西藏自治区',
        '宁夏回族自治区', '新疆维吾尔自治区', '香港特别行政区', '澳门特别行政区'
    ), '|市区'),
    -- User-Agent
    CONCAT(ELT(FLOOR(1 + RAND() * 5), 'Chrome', 'Firefox', 'Safari', 'Edge', 'Opera'), '/', ELT(FLOOR(1 + RAND() * 5), 'Windows', 'macOS', 'Linux', 'Android', 'iOS')),
    -- 设备类型
    ELT(FLOOR(1 + RAND() * 3), 'PC', 'Mobile', 'Tablet'),
    -- 操作系统
    ELT(FLOOR(1 + RAND() * 5), 'Windows', 'macOS', 'Linux', 'Android', 'iOS'),
    -- 浏览器
    ELT(FLOOR(1 + RAND() * 5), 'Chrome', 'Firefox', 'Safari', 'Edge', 'Opera'),
    -- 耗时
    FLOOR(10 + RAND() * 1990),
    -- 类路径
    CONCAT('com.mojian.controller.', ELT(FLOOR(1 + RAND() * 5), 'user', 'article', 'comment', 'message', 'friend')),
    -- 方法名
    CONCAT(ELT(FLOOR(1 + RAND() * 4), 'list', 'add', 'update', 'delete'), 'Method'),
    -- 创建时间（最近30天内随机）
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) - INTERVAL FLOOR(RAND() * 24) HOUR - INTERVAL FLOOR(RAND() * 60) MINUTE
FROM 
    -- 使用数字表生成多行数据
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t2,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t3,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t4,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION 
     SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t5,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3) t6
LIMIT 1200;

-- 验证数据
SELECT '✅ 测试数据插入完成' AS message;
SELECT COUNT(*) AS '总记录数' FROM sys_operate_log;
SELECT type AS '日志类型', COUNT(*) AS '数量' FROM sys_operate_log GROUP BY type;
SELECT device_type AS '设备类型', COUNT(*) AS '数量' FROM sys_operate_log GROUP BY device_type;
SELECT browser AS '浏览器', COUNT(*) AS '数量' FROM sys_operate_log GROUP BY browser;

-- 验证地区数据格式
SELECT 
    ip_source AS 'IP来源',
    COUNT(*) AS '数量'
FROM sys_operate_log 
WHERE ip_source IS NOT NULL
GROUP BY ip_source
ORDER BY COUNT(*) DESC
LIMIT 10;
