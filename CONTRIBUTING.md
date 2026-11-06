# 贡献指南

本文件记录 game-bird **已在执行但此前无成文**的规范。新改动请先读本文再动手。

---

## 一、构建与测试

```bash
mvn -B clean test                          # 单元测试（当前 158 个用例，必须全绿）
mvn -B clean package -Pcontainer -DskipTests   # 打容器用 fat jar（35MB）
```

### ⚠️ `-Pcontainer` 不能漏

上游 `pom.xml` **未声明 `spring-boot-maven-plugin`**，作者原本用 IDE 直接跑 `main`。
因此：

- **不带** `-Pcontainer` → 产出**普通 jar（1.5MB）**，无 `Main-Class`、无内嵌依赖
- 用 `java -jar` 启动会报 `no main manifest attribute`
- 若已 `docker cp` 进镜像，表现为**容器反复重启**而非构建失败
  （`docker cp` 绕过了 Dockerfile 的构建期断言）

**部署前必查**：`ls -lh target/*.jar` 应为 **35M**；若是 1.5M 就是漏了 profile。
详见 [`docker/README.md`](docker/README.md) 的「坑一」。

### 其他命令

```bash
mvn -B clean compile                       # 只编译，改完 import/注解后快速验证
```

---

## 二、Commit 规范（Conventional Commits）

格式：

```
<type>(<scope>): <description>

[可选的正文]

[可选的 footer]
```

### type 取值

| type | 用途 |
|---|---|
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `refactor` | 重构（不改变外部行为） |
| `docs` | 文档 |
| `test` | 测试 |
| `style` | 格式（不影响语义） |
| `chore` | 杂务 |
| `build` | 构建系统 / 依赖 |
| `security` | 安全 |
| `revert` | 回退 |

### scope 取值

按域划分：`route` / `shop` / `pack` / `role` / `account` / `game` / `trap` / `chat` /
`core` / `base` / `data` / `mapper` / `web` / `config` / `cors` / `templates` /
`mvc` / `util` / `path` / `repo` / `deps` / `verify`。

### 破坏性变更

**必须**用以下两种方式之一标记：

1. type 后加 `!`：`refactor(route)!: strip the .do suffix ...`
2. footer 写 `BREAKING CHANGE: <说明>`

> ⚠️ **常见误用**：把 `BREAKING CHANGE:` 当正文后缀写。
> 提交 `433b31c` 的消息末尾是 `BREAKING CHANGE: none -- test-only commit`，
> 这是**误用**——它写在正文里而非独立 footer 行，且内容是 `none`。
> 规范要求：footer 必须是独立行（`BREAKING CHANGE: ` 开头），
> 且只在**确有破坏性变更**时写。

### description 用英文

历史提交 `6e9c5cc`（`chore(mapper): 为 Account 与 Role 补显式 @TableName`）用了中文，
本文件起统一为英文。理由：`git log --oneline` 与 GitHub 提交列表对英文渲染更稳定，
且中文在部分终端会因宽度计算错位。

### 正文写「为什么」而非「做了什么」

diff 已经说明了做了什么。正文应记录：

- 设计取舍与理由（尤其是反直觉的决定）
- 踩到并修正的坑（避免后人重犯）
- 与规格不符之处及依据

参考 `61b4b94`、`0da3efd` 的提交信息。

---

## 三、分支命名

| 前缀 | 用途 | 示例 |
|---|---|---|
| `feat/*` | 新功能 | `feat/path-normalization` |
| `refactor/*` | 重构 | `refactor/aggressive-routing-and-dedup` |
| `fix/*` | 缺陷修复 | `fix/xxx` |

**基线的取法**：从 `main` 切出，不要叠加在未合并的功能分支上。
切出后用 `git merge-base main HEAD` 记录基线 hash，供后续 diff 统计使用。

---

## 四、红线清单（不可触碰）

### 4.1 数据层

| 项 | 约束 |
|---|---|
| `sql/game_bird.sql` | **一字节不改**。25 张表的结构与初始数据均不可变 |
| 与 DB 列绑定的字段名 | **一律不动**，即使拼写错误（见 4.2） |
| 新增 DB 写操作 | **禁止**（重构批次的红线是「零行为变更」） |
| 密码哈希迁移 | **不做**。口令在库中仍为明文，属已知问题，需产品先定方案 |

### 4.2 刻意保留的拼写错误

以下字段名是错的，但**与 DB 列名一致**，改了反而破坏映射：

| 字段 | 对应列 | 说明 |
|---|---|---|
| `RoleCage.baseBridId` | `base_brid_id` | `Brid` 应为 `Bird` |
| `RoleCage.useCageNum0` | `use_cage_num0` | 尾部 `0` 无意义 |

> 二者「拼写与列名同为错误，因而巧合正确」。`RoleCage.java:54,70` 有注释说明。

其余与列绑定的字段（**共 30 个**）同样不可改名，包括：
`idx`、`lu`、`lb`、`ut`、`s`、`r`、`lvu`、`ep`、`ad`、`ko`、`restrained`、`field` 等。

### 4.3 会话与网络

| 项 | 值 | 约束 |
|---|---|---|
| session key | `sRole` | 不可改名 |
| Cookie 名 | `sk_bird` | 不可改名 |
| 端口 | `80` | 不可改 |
| 静态资源路径 | `/assets/**` | 不可改 |

### 4.4 类型漂移

以下字段的类型在不同表间不一致（`Short`/`Integer`/`Byte`/`String`），
**当前批次不统一**，改了会牵动大量比较逻辑：

- `lu`：`BaseBait` 是 `Short`，其余五者是 `Integer`
- `ut`：`Integer` / `String`
- `lb`：`Byte` / `Integer`

### 4.5 不可删除的 public 成员

不得删任何 public 方法/类（死 logger 等明确授权的除外）。
确需删除时先在 README「已知问题」留规格，再删代码。

---

## 五、已知陷阱

### 5.1 探针文件不得入库

排查过程中产生的临时文件（`.probe/` 下的脚本、curl cookie jar、响应体 dump 等）
**不得提交**。`.gitignore` 已覆盖 `.probe/`。

自查：`git ls-files | grep probe` 应输出为空。

### 5.2 `.do` 注释字面量会影响门禁

`.do` 后缀已归零，但代码注释里为解释历史仍保留 `"/message.do"` 这类字面量
（如 `ConstData.java:77`、`GameStatusHandlerInterceptor.java:30`、
`GlobalExceptionHandler.java:100-101`）。

跑门禁 `grep -rn '\.do\b' src/` 时会命中它们，**这是预期的 4 处注释，非残留**。
不要为了「让 grep 归零」去改注释——那会删掉解释「为什么不要写 `.do`」的反面教材。

硬编码 `.do` 的门禁是另一条，应严格为 0：

```bash
grep -rnoE '(href|action)="/[^"]*\.do"' src/
```

### 5.3 `Path.of(userInput)` 会在规范化之前就失败

Windows 上 `Paths.get("C:/data/a<b.txt")` 直接抛 `InvalidPathException`，
根本走不到 `PathNormalizer`。故业务侧**不要**直接 `Path.of` 外部输入，
先过 `PathNormalizer.normalize` 或直接用 `PathOps`。详见
`PathNormalizer` 类级 Javadoc。

### 5.4 通配符 import 不可机械展开

`java.*` / `javax.*` 与第三方包无法从源码树解析类清单。
若按「包内无 .java 文件」判定为无用而删掉通配符，会连带丢掉
`Documented`/`Inherited` 等 JDK 注解类，编译直接失败。
展开前先确认该包能在 `src/` 下找到目录。

### 5.5 简化限定名必须与 import 清理同批做

原代码用 `@jakarta.annotation.Resource` 全限定写法时，正文没有 `Resource` 简单名，
「未使用 import」检测会误判。改简写后必须立即编译，并手工补回 import。

---

## 六、相关文档

| 文档 | 内容 |
|---|---|
| [`README.md`](README.md) | 项目介绍、路由对照表、已知问题 |
| [`docs/规范/编号词汇表.md`](docs/规范/编号词汇表.md) | `RT-n` / `Rn` / `Q-x` / `D-x` / `Un` 编号释义 |
| [`docker/README.md`](docker/README.md) | 容器化部署与两个构建陷阱 |
| [`docs/验证记录/改库验证-成长值.md`](docs/验证记录/改库验证-成长值.md) | 一次完整的写库→观测→复原验证留证 |
