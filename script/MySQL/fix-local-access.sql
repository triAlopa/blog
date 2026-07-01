-- 本地文件访问修复SQL
-- 执行此SQL前请确保后端服务已停止

USE blog;

-- 1. 更新本地存储路径配置
UPDATE sys_file_oss SET
  storage_path = 'D:/Chen/Pictures/dev_temp/',
  domain = 'http://127.0.0.1:8800/localFile/',
  path_patterns = 'localFile/**',
  base_path = 'local-plus/',
  is_enable = 1,
  enable_access = 1
WHERE platform = 'local';

-- 2. 如果没有本地存储配置，则插入
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

-- 3. 验证配置
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

-- 预期结果：
-- platform: local
-- storage_path: D:/Chen/Pictures/dev_temp/
-- domain: http://127.0.0.1:8800/localFile/
-- path_patterns: localFile/**
-- base_path: local-plus/
-- is_enable: 1
-- enable_access: 1
