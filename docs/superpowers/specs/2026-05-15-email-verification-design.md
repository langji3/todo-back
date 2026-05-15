# QQ 邮箱验证码 — 设计文档

## 概述

注册流程增加 QQ 邮箱验证码，采用两步流程：先发送验证码验证邮箱，再提交注册信息。使用 QQ 邮箱 SMTP 发送邮件，Redis 存储验证码及限流数据。

## 接口设计

### 新增：POST `/api/auth/send-code`

无需认证。

请求体：

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| `email` | String | 是 | `@NotBlank`, `@Email` |

响应：

```json
{ "code": 200, "message": "验证码已发送" }
```

### 修改：POST `/api/auth/register`

请求体新增 `code` 字段：

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| `name` | String | 是 | `@NotBlank`, `@Size(min=1, max=64)` |
| `email` | String | 是 | `@NotBlank`, `@Email` |
| `password` | String | 是 | `@NotBlank`, `@Size(min=6, max=128)` |
| `code` | String | 是 | `@NotBlank`, 4 位数字 |

响应格式不变，返回 `AuthVO`。

## 校验规则

| 规则 | 细节 |
|------|------|
| 发送间隔 | 同一邮箱 60 秒内不能重复发送 |
| 每日上限（邮箱） | 同一邮箱每天最多发送 5 次 |
| 每日上限（IP） | 同一 IP 每天最多发送 10 次 |
| 验证码 | 4 位随机数字，10 分钟过期 |
| 验证成功 | 验证码立即从 Redis 删除，防止重复使用 |
| 错误次数 | 连续输错 5 次，验证码失效 |
| 邮箱绑定 | 验证码存储在 `verify:code:{email}` key 下，注册时用提交的 email 查找，天然保证邮箱一致 |

## Redis Key 设计

| Key | Value | TTL |
|-----|-------|-----|
| `verify:code:{email}` | 4 位验证码字符串 | 10 分钟 |
| `verify:interval:{email}` | `"1"` | 60 秒 |
| `verify:daily:{email}` | 发送次数计数（int） | 到当天 23:59:59 |
| `verify:daily:ip:{ip}` | 发送次数计数（int） | 到当天 23:59:59 |
| `verify:fail:{email}` | 错误次数计数（int） | 10 分钟 |

## 邮件发送

使用 `spring-boot-starter-mail`，QQ 邮箱 SMTP。

配置（application.yml）：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    protocol: smtps
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          ssl:
            enable: true
```

邮件内容为暗色科技风 HTML 模板，验证码通过 `${code}` 占位符注入。

## 新增/修改文件清单

| 操作 | 文件 |
|------|------|
| 修改 | `pom.xml` — 添加 `spring-boot-starter-mail` 依赖 |
| 修改 | `application.yml` — 添加邮件配置 |
| 修改 | `SecurityConstant.java` — 添加 `/api/auth/send-code` 到免认证路径 |
| 修改 | `UserRegisterRequest.java` — 添加 `code` 字段 |
| 修改 | `UserServiceImpl.java` — register 方法增加验证码校验 |
| 修改 | `RedisConstant.java` — 添加验证码相关 key 常量 |
| 新增 | `EmailService.java` — 邮件发送服务接口 |
| 新增 | `EmailServiceImpl.java` — 邮件发送服务实现（含 HTML 模板） |
| 新增 | `VerifyCodeService.java` — 验证码生成/校验/限流服务接口 |
| 新增 | `VerifyCodeServiceImpl.java` — 验证码服务实现 |
| 新增 | `SendCodeRequest.java` — 发送验证码 DTO |
| 修改 | `AuthController.java` — 添加 send-code 接口 |
