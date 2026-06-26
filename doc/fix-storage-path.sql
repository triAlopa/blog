-- 更新本地存储路径配置
-- 注意：Windows路径要用正斜杠 /，结尾必须有斜杠

UPDATE sys_file_oss SET 
  -- 本地存储路径（必须用正斜杠，结尾有斜杠）
  storage_path = 'D:/Chen/Pictures/dev_temp/',
  
  -- 访问域名（用于返回给前端的URL）
  domain = 'http://127.0.0.1:8800/localFile/',
  
  -- 访问路径模式（Spring MVC资源映射用）
  path_patterns = 'localFile/**',
  
  -- 存储基础路径（在存储目录内的子目录）
  base_path = 'local-plus/',
  
  -- 启用此存储平台
  is_enable = 1,
  
  -- 启用访问（允许通过HTTP访问）
  enable_access = 1
  
WHERE platform = 'local';

-- 如果没有本地存储配置，则插入一条
INSERT INTO sys_file_oss (domain, access_key, secret_key, bucket, base_path, platform, is_enable, storage_path, enable_access, path_patterns, region, create_time)
SELECT 
  'http://127.0.0.1:8800/localFile/',
  '',
  '',
  '',
  'local-plus/',
  'local',
  1,
  'D:/Chen/Pictures/dev_temp/',
  1,
  'localFile/**',
  NULL,
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_file_oss WHERE platform = 'local');

-- 查看更新后的配置
SELECT 
  id,
  platform,
  storage_path,
  domain,
  path_patterns,
  base_path,
  is_enable,
  enable_access
FROM sys_file_oss 
WHERE platform = 'local';
