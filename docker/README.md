# game-bird Docker 部署

用 Docker Compose 把game-bird（Spring Boot 3.2.7 + Thymeleaf + MySQL）跑成完整栈。

---

## 快速命令

```bash
cd docker

docker compose up -d --build# 启动（首次会构建镜像，约 3 分钟）
docker compose ps              # 查看状态
docker compose logs -f app     # 看应用日志
docker compose down            # 停止（保留数据）
docker compose down -v         # 停止并删除数据卷（⚠️ 数据丢失）
```

访问：**http://127.0.0.1:9090**

---

## 架构

```
┌─────────────────┐      ┌──────────────────┐
│  game-bird-app  │─────▶│   game-bird-db   │
│  JRE 17 alpine  │  JDBC │   MySQL 8.0      │
│  非 root 运行   │:3306 │  utf8mb4          │
│  127.0.0.1:9090 │      │  数据卷持久化      │
└─────────────────┘      └──────────────────┘
```

- **端口**：容器内监听 80（源码 `server.port=80`），映射到本机 `9090`
- **暴露范围**：仅绑定 `127.0.0.1`，局域网与公网均不可达
- **启动依赖**：`depends_on: condition: service_healthy`，等MySQL 健康后才启动应用
- **时区**：容器内已设 `Asia/Shanghai`

---

## 文件说明

| 文件 | 作用 |
|---|---|
| `docker/Dockerfile` | 多阶段构建：Maven 编译 → JRE 17 运行 |
| `docker/docker-compose.yml` | 应用 + 数据库编排 |
| `docker/settings.xml` | Maven 阿里云镜像 + 中央仓库兜底 |
| `docker/.env` |密码与端口（**已gitignore，勿提交**） |
| `docker/.env.example` | 配置模板 |

---

## 镜像构建要点

### 为什么需要 `-Pcontainer`

上游 `pom.xml` **未声明 `spring-boot-maven-plugin`** —— 作者原本用 IDE 直接跑 `main` 方法，所以 `mvn package` 只产出**普通 jar**（1.5MB，无 `Main-Class`、无内嵌依赖），用 `java -jar` 启动会报：

```
no main manifest attribute, in app.jar
```

已在 `pom.xml` 加了 `container` profile 把 `repackage` 绑定到 `package` 阶段：

```bash
mvn -B clean package -Pcontainer -DskipTests
```

**默认构建行为不变** —— 不加 `-Pcontainer` 时 IDE 照常开发，不影响原作者的使用方式。

> 踩坑：不能直接执行 `mvn spring-boot:repackage` —— 脱离 lifecycle 时插件拿不到源 jar，会报 `Source file must not be null`，必须通过 execution 绑定。

### 坑一：`-Pcontainer` 漏了会导致容器进重启循环（且不报错）

若用**不带** `-Pcontainer` 的 `mvn package` 产物（普通 jar，约 1.5MB），
`docker cp` 进镜像后，`java -jar` 会因缺 `Main-Class` 与内嵌依赖而失败。
**现象是容器反复重启、而不是构建失败**——因为 Dockerfile 里的构建期断言
只校验 `target/` 下的产物，`docker cp` 绕过了构建阶段。

**排查手法**：

```bash
ls -lh target/*.jar                # 1.5MB = 普通 jar（错）；35MB = fat jar（对）
unzip -l target/*.jar | grep BOOT-INF/lib   # 无输出 = 缺内嵌依赖
docker compose logs app | tail -30
```

**正解**：始终用 `mvn -B clean package -Pcontainer -DskipTests`，
且 `docker cp` 前**先确认 jar 大小**。IDE 里直接 `mvn package` 的习惯产物不能进镜像。

### 坑二：`docker compose build` 在 Docker Hub 不通时会静默失败，测到旧代码

网络受限时 `docker compose build` 可能只打印 warning 而不退出非零，
`docker compose up -d` 于是**沿用上一轮的旧镜像**——E2E 测的全是旧代码，
却看起来"部署成功"，极易误判。

**排查手法**（每次部署后必做）：

```bash
docker compose build app 2>&1 | tail -20   # 看是否真的出现 build 步骤日志
docker compose images app                   # 看 IMAGE ID / 创建时间是否已变
docker inspect game-bird-app --format '{{.Created}}'   # 容器创建时间应晚于构建
```

**正解**：改完代码后**不要只 `up -d`**，要么 `--build` 并核对 IMAGE ID 变化，
要么直接 `docker cp` 新 jar + `docker compose restart app`（本项目验证链用的就是后者，
可精确控制进容器的产物）。

### 构建期自检

Dockerfile 里加了断言，产物不对会**在构建阶段失败**而不是等运行时才炸：

```dockerfile
RUN test -f /build/target/game-bird-0.0.1-SNAPSHOT.jar \
    && unzip -l ... | grep -q "BOOT-INF/lib" \
    && echo "✅ fat jar 构建成功"
```

### 安全加固

- 运行时**非 root**（`adduser spring`）
- 基础镜像 `eclipse-temurin:17-jre-alpine`（JRE 而非 JDK）
- JVM 堆上限 `-XX:MaxRAMPercentage=75.0`，避免容器内存溢出
- 密码由脚本随机生成 20 位，存`docker/.env`，通过 Compose 变量注入，**不入镜像层、不入库**

---

## 数据管理

### 初始数据

`sql/game_bird.sql`（189KB / 25 张表 / 4 条账号）通过 `/docker-entrypoint-initdb.d` **仅在数据卷为空时**自动导入。

⚠️ **删卷后重建会重新导入**；若卷已存在想重新导入，需手动执行：

```bash
docker exec -i game-bird-db mysql -uroot -p<密码> game_bird < sql/game_bird.sql
```

### 备份

```bash
cd docker
docker exec game-bird-db mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" \
  --single-transaction --default-character-set=utf8mb4 \
  game_bird > ../backup_game_bird_$(date +%Y%m%d).sql
```

### 常用查询

```bash
source docker/.env
docker exec game-bird-db mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -D game_bird \
  --default-character-set=utf8mb4 -e "SHOW TABLES;"
```

---

## 故障排查

| 现象 | 原因与处理 |
|---|---|
| `no main manifest attribute` | 未用 `-Pcontainer` 构建（见上文） |
| app 反复重启 | 查 `docker compose logs app`，多为数据库连接失败 |
| 端口 9090 被占用 | 改 `docker/.env` 里的 `APP_PORT` |
| `unhealthy` 且端口不通 | 容器内是 80 不是 8080，确认 `ports` 映射为 `:80` |
| MySQL 首次启动慢 | 需等初始化脚本执行完（189KB SQL），约 30–60 秒 |
| 构建时阿里云传输中断 | `Premature end of Content-Length`，网络抖动；`settings.xml` 已配中央仓库兜底，重试即可 |
| 容器反复重启且 `logs` 里报 `no main manifest attribute` | 用了不带 `-Pcontainer` 的普通 jar（1.5MB），见**坑一** |
| 改完代码但页面行为没变 | `docker compose build` 静默失败、沿用旧镜像，见**坑二**；用 `docker compose images app` 核对 IMAGE ID |

---

## 回滚

镜像与配置均在版本控制下，回滚只需：

```bash
git checkout pom.xml# 还原 pom 改动
docker compose down && docker compose up -d --build
```