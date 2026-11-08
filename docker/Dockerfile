# syntax=docker/dockerfile:1
# ---- 构建阶段：Maven 编译 ----
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 阿里云镜像配置（含中央仓库兜底，国内网络抖动时可自动回退）
COPY docker/settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
# 跳过测试（上游 fork 项目无 CI 保障，测试产物不影响部署）
# 不使用 dependency:go-offline：该插件在网络不稳时易因传输中断失败，
# 直接 package 阶段下载依赖更可靠（层缓存仍可复用）
# 启用 container profile：其spring-boot-maven-plugin 把 repackage 绑定到
# package 阶段，产出内嵌依赖且带 Main-Class 的可执行 fat jar。
# （单独执行 `mvn spring-boot:repackage` 会报 "Source file must not be null"，
#   因为脱离 lifecycle 时插件拿不到源 jar，必须通过 execution 绑定。）
RUN mvn -B clean package -Pcontainer -DskipTests

# 自检：产物必须含 BOOT-INF/lib，否则直接让构建失败（而不是等运行时才报错）
RUN test -f /build/target/game-bird-0.0.1-SNAPSHOT.jar \
    && unzip -l /build/target/game-bird-0.0.1-SNAPSHOT.jar | grep -q "BOOT-INF/lib" \
    && echo "✅ fat jar 构建成功（含 BOOT-INF/lib）" \
    && ls -lh /build/target/*.jar

# ---- 运行阶段：JRE 17 精简运行时（与 pom 的 compiler target 一致）----
FROM eclipse-temurin:17-jre-alpine

# 安装时区与 curl（curl 用于容器健康检查）
RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 以非 root 运行
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
RUN mkdir -p /app/files && chown -R spring:spring /app
USER spring

# 从构建阶段取已通过自检的 fat jar
COPY --from=builder --chown=spring:spring /build/target/game-bird-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 80

# JVM 参数：容器内存受限，显式设置堆上限
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:80/ || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]