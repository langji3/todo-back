-- 邮箱查询索引：每次认证请求、登录、注册都会查询
CREATE UNIQUE INDEX idx_user_email ON t_user(email, delete_flag);

-- 用户设置外键索引
CREATE INDEX idx_user_settings_user_id ON t_user_settings(user_id);
