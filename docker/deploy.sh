#!/usr/bin/env bash
# =============================================================================
# game-bird 一键构建与部署
# =============================================================================
# 用法：
#   ./deploy.sh              # 一键构建 + 部署（默认）
#   ./deploy.sh deploy       # 同上
#   ./deploy.sh stop         # 停止（保留数据卷）
#   ./deploy.sh start        # 启动（不重新构建）
#   ./deploy.sh restart      # 仅重启 app（改配置后热生效）
#   ./deploy.sh status       # 查看容器状态与健康检查结果
#   ./deploy.sh logs         # 跟踪 app 日志
#   ./deploy.sh backup       # 备份数据库到 ../backup_YYYYMMDD.sql
#   ./deploy.sh verify       # 只验证，不构建（HTTP 探活 + 镜像 ID 核对）
#   ./deploy.sh env          # 重新生成 docker/.env（随机密码）
#   ./deploy.sh reset        # ⚠️  删卷重建，数据全部丢失
#
# 额外参数（传给 docker compose build）：
#   ./deploy.sh deploy --no-cache    # 忽略层缓存，强制完整重建（约 3 分钟）
#
# 兼容 Git Bash（Windows）/ bash（Linux/macOS）。
# =============================================================================

set -euo pipefail

# ---- 定位到脚本所在目录（docker/），保证从任何位置调用都对 ----------------
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-game-bird}"
APP_PORT="${APP_PORT:-9090}"
BASE_URL="http://127.0.0.1:${APP_PORT}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-180}"     # 等待健康的秒数上限
ENV_FILE=".env"

# ---- 输出着色（非 TTY 时自动退化为纯文本，避免日志出现转义符） -------------
if [ -t 1 ]; then
    C_RED=$'\033[31m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'
    C_BLUE=$'\033[34m'; C_BOLD=$'\033[1m'; C_OFF=$'\033[0m'
else
    C_RED=''; C_GREEN=''; C_YELLOW=''; C_BLUE=''; C_BOLD=''; C_OFF=''
fi

info()  { printf '%s[INFO]%s  %s\n'  "$C_BLUE"   "$C_OFF" "$*"; }
ok()    { printf '%s[ OK ]%s  %s\n'  "$C_GREEN"  "$C_OFF" "$*"; }
warn()  { printf '%s[WARN]%s  %s\n'  "$C_YELLOW" "$C_OFF" "$*"; }
err()   { printf '%s[FAIL]%s  %s\n'  "$C_RED"    "$C_OFF" "$*" >&2; }
step()  { printf '\n%s==> %s%s\n'    "$C_BOLD"   "$C_OFF" "$*"; }
die()   { err "$*"; exit 1; }

# ---- 工具函数 ---------------------------------------------------------------

# 生成 20 位随机密码（字母数字，避开易混淆字符）
gen_password() {
    if command -v openssl >/dev/null 2>&1; then
        openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 20
    else
        head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 20
    fi
}

# 从 .env 读一个变量（不 source，避免把值泄进环境与子进程）
env_get() {
    local key="$1"
    [ -f "$ENV_FILE" ] || return 0
    grep -E "^${key}=" "$ENV_FILE" 2>/dev/null | tail -n1 | cut -d= -f2-
}

# ---- 前置检查 ---------------------------------------------------------------

require_docker() {
    command -v docker >/dev/null 2>&1 \
        || die "未找到 docker 命令。请先安装 Docker Desktop / Docker Engine。"
    docker info >/dev/null 2>&1 \
        || die "Docker 守护进程未运行或当前用户无权限。请启动 Docker Desktop，或以有权限的用户重试。"
    docker compose version >/dev/null 2>&1 \
        || die "未找到 'docker compose'（Compose V2）。请升级 Docker 到 20.10+ 并启用 compose 插件。"
}

ensure_env() {
    if [ -f "$ENV_FILE" ]; then
        ok "已存在 $ENV_FILE（保留原配置，未覆盖）"
        return 0
    fi

    step "生成 $ENV_FILE"
    local root_pw app_pw
    root_pw="$(gen_password)"
    app_pw="$(gen_password)"

    cat > "$ENV_FILE" <<EOF
# ---- game-bird 部署配置（敏感项，不入库）----
# 本文件由 deploy.sh 自动生成；删除后再次运行 deploy.sh 会重新生成。
# ⚠️ 重新生成会更换数据库密码，但已初始化的数据卷仍用旧密码 → 需配 reset 重建。
MYSQL_ROOT_PASSWORD=${root_pw}
MYSQL_PASSWORD=${app_pw}
MYSQL_DATABASE=game_bird
APP_PORT=${APP_PORT}
EOF
    chmod 600 "$ENV_FILE"
    ok "已生成 $ENV_FILE（权限 600，含 20 位随机密码）"
    warn "首次部署将用该密码初始化数据库；请妥善保管 $ENV_FILE"
}

# ---- 核心动作 ---------------------------------------------------------------

# 构建镜像。
# 坑二防护：Docker Hub 不通时 `docker compose build` 可能只打印 warning 而退出码为 0，
# 于是 up -d 沿用旧镜像、测的是旧代码。判据**不是**镜像 ID 是否变化——
# 源码未变时层缓存命中，产物 ID 本就相同（已实测：二次构建 2 分钟、ID 不变、完全正常）。
# 可靠判据是构建输出里必须出现 Built 标记，证明构建流程真的跑完了。
build_image() {
    step "构建镜像"
    local log
    # 捕获完整输出供判据使用，同时回显尾部供人工核对
    if ! log="$(docker compose build "$@" app 2>&1)"; then
        printf '%s\n' "$log" | tail -30 >&2
        err "镜像构建失败。常见原因：网络不通导致依赖下载中断。"
        err "排查：docker compose build app 2>&1 | tail -30"
        die "构建失败，已中止部署（未启动任何容器）。"
    fi
    printf '%s\n' "$log" | tail -15

    if ! printf '%s\n' "$log" | grep -q "Built"; then
        err "构建输出中未见 'Built' 标记 —— 构建很可能静默失败了（见 docker/README.md 坑二）。"
        err "这意味着部署会沿用旧镜像，测的是旧代码。"
        err "排查：docker compose build app 2>&1 | tail -20"
        die "已中止部署。请确认网络后重试。"
    fi

    local id
    id="$(docker compose images -q app 2>/dev/null | head -n1 || true)"
    ok "镜像已就绪（IMAGE ID: ${id:0:12}）"

    # 提示本次是否复用了缓存（仅为信息，不作为成败判据）
    if printf '%s\n' "$log" | grep -q "CACHED"; then
        info "本次构建复用了层缓存；源码未变时镜像 ID 不变属正常现象"
    fi
}

wait_healthy() {
    step "等待服务就绪（最长 ${HEALTH_TIMEOUT}s）"
    local waited=0
    while [ "$waited" -lt "$HEALTH_TIMEOUT" ]; do
        local db_state app_state
        db_state="$(docker inspect --format '{{.State.Health.Status}}' \
            "${COMPOSE_PROJECT_NAME}-db" 2>/dev/null || echo "missing")"
        app_state="$(docker inspect --format '{{.State.Health.Status}}' \
            "${COMPOSE_PROJECT_NAME}-app" 2>/dev/null || echo "missing")"

        if [ "$db_state" = "healthy" ] && [ "$app_state" = "healthy" ]; then
            ok "db=${db_state}  app=${app_state}"
            return 0
        fi

        # 中途失败快速退出，不必等满超时
        case "$app_state" in
            unhealthy)
                err "app 健康检查失败。查看日志：docker compose logs app | tail -40"
                die "部署失败。"
                ;;
        esac

        printf '\r  等待中... %3ds / %ds   (db=%s app=%s)   ' \
            "$waited" "$HEALTH_TIMEOUT" "$db_state" "$app_state"
        sleep 5
        waited=$((waited + 5))
    done
    printf '\n'
    err "等待超时（${HEALTH_TIMEOUT}s）服务仍未就绪。"
    err "MySQL 首次启动需导入 189KB 初始化脚本，约 30–60 秒属正常。"
    die "请用 'docker compose logs app' 与 'docker compose logs db' 排查。"
}

verify_http() {
    step "验证 HTTP 探活"
    command -v curl >/dev/null 2>&1 || { warn "无 curl，跳过 HTTP 验证"; return 0; }

    local code
    code="$(curl -s -o /dev/null -w '%{http_code}' --max-time 15 "$BASE_URL/login" || echo "000")"
    [ "$code" = "200" ] || die "GET ${BASE_URL}/login 返回 ${code}（期望 200）。"
    ok "GET ${BASE_URL}/login → 200"

    # 未登录访问受保护路径应重定向到 /login（证明拦截器生效）
    local redirect
    redirect="$(curl -s -o /dev/null -w '%{redirect_url}' --max-time 15 "$BASE_URL/game" || echo "")"
    case "$redirect" in
        */login*) ok "GET ${BASE_URL}/game → 302 重定向到 ${redirect}（登录拦截生效）" ;;
        *)       warn "GET ${BASE_URL}/game 未重定向到 /login（得到: ${redirect:-无}），请人工确认" ;;
    esac
}

print_summary() {
    step "部署完成"
    cat <<EOF

  应用地址    ${C_BOLD}${BASE_URL}${C_OFF}    （仅绑定 127.0.0.1，局域网与公网不可达）
  数据库      ${COMPOSE_PROJECT_NAME}-db  (MySQL 8.0, 库名 $(env_get MYSQL_DATABASE || echo game_bird))
  配置文件    ${ENV_FILE}  （含随机密码，权限 600，已被 .gitignore 忽略）

  常用命令：
    ./deploy.sh status      查看状态与健康检查
    ./deploy.sh logs        跟踪应用日志
    ./deploy.sh backup      备份数据库
    ./deploy.sh restart     改配置后热重启 app

  故障排查见 docker/README.md「故障排查」一节。
EOF
}

# ---- 子命令 -----------------------------------------------------------------

cmd_deploy() {
    require_docker
    ensure_env

    # APP_PORT 以 .env 为准（.env 可能是上一次生成的）
    APP_PORT="$(env_get APP_PORT || echo "$APP_PORT")"
    BASE_URL="http://127.0.0.1:${APP_PORT}"

    build_image "$@"

    step "启动服务"
    docker compose up -d
    ok "容器已启动"

    wait_healthy
    verify_http
    print_summary
}

cmd_stop() {
    require_docker
    step "停止服务（保留数据卷）"
    docker compose down
    ok "已停止。数据仍在卷 ${COMPOSE_PROJECT_NAME}-db-data 中。"
}

cmd_start() {
    require_docker
    step "启动服务（不重新构建）"
    docker compose up -d
    wait_healthy
    verify_http
    print_summary
}

cmd_restart() {
    require_docker
    step "重启 app 容器"
    docker compose restart app
    wait_healthy
    verify_http
    print_summary
}

cmd_status() {
    require_docker
    docker compose ps
    printf '\n'
    local c
    for c in "${COMPOSE_PROJECT_NAME}-db" "${COMPOSE_PROJECT_NAME}-app"; do
        local st
        st="$(docker inspect --format '{{.State.Health.Status}}' "$c" 2>/dev/null || echo missing)"
        printf '  %-24s health=%s\n' "$c" "$st"
    done
    printf '\n  镜像 ID：%s\n' "$(docker compose images -q app 2>/dev/null | head -n1 | cut -c1-12)"
    printf '  访问地址：%s\n' "$BASE_URL"
}

cmd_logs() {
    require_docker
    docker compose logs -f app
}

cmd_backup() {
    require_docker
    local out="../backup_$(date +%Y%m%d_%H%M%S).sql"
    step "备份数据库 → $out"
    # 从 .env 取密码但不落盘到命令行历史以外的任何地方
    local pw
    pw="$(env_get MYSQL_ROOT_PASSWORD)"
    [ -n "$pw" ] || die "$ENV_FILE 中缺少 MYSQL_ROOT_PASSWORD"
    # MYSQL_PWD 方式避免密码出现在 ps 与 shell 历史里
    MYSQL_PWD="$pw" docker exec "${COMPOSE_PROJECT_NAME}-db" \
        mysqldump -uroot --single-transaction --default-character-set=utf8mb4 \
        "$(env_get MYSQL_DATABASE || echo game_bird)" > "$out"
    ok "备份完成：$out ($(du -h "$out" | cut -f1))"
}

cmd_verify() {
    require_docker
    step "仅验证（不构建）"
    verify_http
    ok "验证结束"
}

cmd_env() {
    step "重新生成 $ENV_FILE"
    if [ -f "$ENV_FILE" ]; then
        warn "将覆盖现有 $ENV_FILE —— 数据库密码会更换。"
        warn "若数据卷已用旧密码初始化，需执行 ./deploy.sh reset 重建。"
        printf '确认覆盖？[y/N] '
        local ans; read -r ans
        case "$ans" in
            y|Y|yes|YES) ;;
            *) die "已取消。" ;;
        esac
    fi
    rm -f "$ENV_FILE"
    ensure_env
}

cmd_reset() {
    require_docker
    step "⚠️  重置：删除数据卷并重新部署"
    warn "将删除卷 ${COMPOSE_PROJECT_NAME}-db-data 与 ${COMPOSE_PROJECT_NAME}-app-files。"
    warn "数据库中所有数据（账号、角色、道具）将全部丢失且不可恢复。"
    printf '确认执行？输入 RESET 继续，其它任意键取消: '
    local ans; read -r ans
    [ "$ans" = "RESET" ] || die "已取消。"

    docker compose down -v
    ok "卷已删除"
    APP_PORT="${APP_PORT}"
    cmd_deploy
}

usage() {
    # 只打印「用法」区块，避免把文件头注释与代码也带出来
    sed -n '/^# 用法：/,/^# 兼容/p' "$0" | sed 's/^#\{1,\} \{0,1\}//; /^$/d'
}

# ---- 入口 -------------------------------------------------------------------

main() {
    local cmd="${1:-deploy}"
    shift 2>/dev/null || true
    case "$cmd" in
        deploy)          cmd_deploy "$@" ;;
        stop|down)       cmd_stop "$@" ;;
        start|up)        cmd_start "$@" ;;
        restart)         cmd_restart "$@" ;;
        status|ps)       cmd_status "$@" ;;
        logs)            cmd_logs "$@" ;;
        backup)          cmd_backup "$@" ;;
        verify)          cmd_verify "$@" ;;
        env)             cmd_env "$@" ;;
        reset)           cmd_reset "$@" ;;
        -h|--help|help)  usage ;;
        *)               err "未知子命令: $cmd"; usage; exit 2 ;;
    esac
}

main "$@"
