# 部署文档

## 1. 服务器要求

| 项目     | 最低配置        | 推荐配置         |
| -------- | --------------- | ---------------- |
| CPU      | 1 核            | 2 核             |
| 内存     | 2 GB            | 4 GB             |
| 磁盘     | 20 GB           | 40 GB SSD        |
| 系统     | Ubuntu 22.04    | Ubuntu 22.04     |

云平台安全组仅开放：22（SSH）、80（HTTP）、443（HTTPS）。**不要开放 3306 和 6379。**

---

## 2. 服务器环境安装

```bash
# Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# 重新登录终端后验证
docker --version

# Nginx
sudo apt update && sudo apt install -y nginx
sudo systemctl enable nginx
```

---

## 3. 前置条件

服务器上需已部署 MySQL 和 Redis，且容器在网络 `app-storage` 中：

- MySQL 容器名：`mysql_server`
- Redis 容器名：`redis_server`

如未创建该网络：

```bash
docker network create app-storage
```

确保 MySQL 中已存在 `todo` 数据库及对应用户。

---

## 4. 本地打包

在项目根目录执行：

```bash
./mvnw clean package -DskipTests
```

---

## 5. 上传文件

将以下文件上传到服务器 `/opt/todo-back/`：

- `target/todo-backend-0.0.1-SNAPSHOT.jar`
- `Dockerfile`（项目根目录）
- `docker-compose.yml`（项目根目录）

> `.env` 需要在服务器上手动创建，不要上传本地版本。

上传完成后在服务器上创建 `.env`：

```bash
cd /opt/todo-back
nano .env
```

填入以下内容：

```env
# MySQL
MYSQL_HOST=mysql_server
DB_PORT=3306
DB_NAME=todo
DB_USERNAME=todo_user
DB_PASSWORD=<密码>

# Redis
REDIS_HOST=redis_server
REDIS_PORT=6379
REDIS_PASSWORD=<密码>

# QQ 邮箱
MAIL_USERNAME=<QQ邮箱>
MAIL_PASSWORD=<邮箱授权码>

# 阿里云 OSS
ALIYUN_OSS_ACCESS_KEY_ID=<Key>
ALIYUN_OSS_ACCESS_KEY_SECRET=<Secret>
ALIYUN_OSS_BUCKET_NAME=<Bucket>

# 应用
SERVER_PORT=8088
JVM_XMS=256m
JVM_XMX=512m
```

```bash
chmod 600 .env
```

---

## 6. 启动后端

```bash
cd /opt/todo-back
docker compose up -d --build
```

验证：

```bash
docker compose ps                           # 容器状态为 Up
curl localhost:8088/swagger-ui.html          # 能访问即成功
```

---

## 7. Nginx 反向代理

```bash
sudo nano /etc/nginx/sites-available/api.conf
```

填入以下内容：

```nginx
server {
    listen 80;
    server_name api.dlacm.top;

    client_max_body_size 5M;

    location /todo/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/api.conf /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

DNS 解析：添加 A 记录 `api.dlacm.top` → 服务器 IP。

---

## 8. 日常运维

```bash
cd /opt/todo-back

# 查看日志
docker compose logs -f todo-back

# 重启后端
docker compose restart todo-back

# 更新应用：上传新 JAR 后执行
docker compose up -d --build todo-back
```
