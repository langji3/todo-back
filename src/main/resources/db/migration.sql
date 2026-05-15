CREATE DATABASE IF NOT EXISTS todo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- 2. 切换数据库（关键！）
USE todo;

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(64)  NULL                    COMMENT '用户名',
    password    VARCHAR(255) NOT NULL                COMMENT 'BCrypt加密密码',
    nickname    VARCHAR(64)  NULL                    COMMENT '昵称（前端name字段）',
    email       VARCHAR(128) NOT NULL                COMMENT '邮箱（登录标识）',
    avatar      VARCHAR(500) NULL                    COMMENT '头像OSS URL',
    phone       VARCHAR(20)  NULL                    COMMENT '手机号',
    role        TINYINT      NOT NULL DEFAULT 0      COMMENT '角色 0=普通用户 1=管理员',
    delete_flag TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 创建分类表
CREATE TABLE IF NOT EXISTS t_category (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(64)  NOT NULL                COMMENT '分类名称',
    color       VARCHAR(32)  NOT NULL DEFAULT '#6C5CE7' COMMENT '颜色（hex）',
    user_id     BIGINT       NOT NULL                COMMENT '所属用户ID',
    delete_flag TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 3. 创建待办事项表
CREATE TABLE IF NOT EXISTS t_todo (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '待办ID',
    title        VARCHAR(255) NOT NULL                COMMENT '标题',
    description  TEXT         NULL                    COMMENT '描述',
    date         DATE         NOT NULL                COMMENT '日期（YYYY-MM-DD）',
    category_id  BIGINT       NULL                    COMMENT '分类ID',
    status       TINYINT      NOT NULL DEFAULT 0      COMMENT '状态 0=待完成 1=已完成',
    user_id      BIGINT       NOT NULL                COMMENT '所属用户ID',
    delete_flag  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_date (date),
    INDEX idx_category_id (category_id),
    INDEX idx_user_completed (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- 4. 创建用户设置表
CREATE TABLE IF NOT EXISTS t_user_settings (
    id            BIGINT     AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',
    user_id       BIGINT     NOT NULL                COMMENT '用户ID（一对一）',
    dark_mode     TINYINT(1) NOT NULL DEFAULT 0        COMMENT '深色模式',
    notifications TINYINT(1) NOT NULL DEFAULT 1        COMMENT '通知开关',
    create_time   DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';
