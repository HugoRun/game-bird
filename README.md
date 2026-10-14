# game-bird

## 介绍

wap游戏小鸟情人

`不做更新`

## 软件架构

JAVA语言为基础

使用SpringBoot + Mybatis + Thylemeaf

## 版本状态

### 0.1
购买诱饵等
使用诱饵


## 使用说明

导入数据库后记得修改`application.yml`配置的数据库信息


## 截图

![](./screenshot/msedge_kBB3OpU6qk.png)

![](./screenshot/msedge_QKjIDp9rRm.png)

![](./screenshot/msedge_ufSK5zEqOu.png)

## 已知问题

### 修改密码功能已移除（死路由）

该功能原作者从未实现：模板缺失 + POST 方法体是纯空壳（只 `return`，不查库不更新）+ 全项目无任何入口链接。

本次重构不新增功能，故移除死路由（`GET /changePassword.do` 与 `POST /changePassword.do`，含 `templates/account/changePassword.html`）。

### 未来工作：修改密码（尚未实现）

以下为设计规格，**当前代码中并不存在**，此处仅作记录，供后续需要时实现参考。

- **密码长度阈值**：6 到 20 位。
- **错误文案（4 条）**：
  - `原密码不正确。`
  - `新密码长度需为 6 到 20 位。`
  - `两次输入的新密码不一致。`
  - `新密码不能与原密码相同。`
- **成功文案**：`密码修改成功。`
- **所需路由与视图**：
  - `GET /changePassword.do` → 视图 `account/changePassword`（当前缺失）
  - `POST /changePassword.do` → 校验通过后更新 `account.password`，回到同一视图
- **所需字段**：旧密码 `oldPassword`、新密码 `password1`、确认新密码 `password2`。
- **注意**：口令在数据库中仍为明文；本轮红线明确「不做密码哈希迁移」，实现此功能前需先确定哈希方案。

### 商品详情页从未实现（6 条死路由已移除）

6 种道具（诱饵、陷阱、鸟笼、巢穴、训练场、道具）各有一条详情路由，原`ShopController.pageShopXxxDetail` 方法体恒返回视图名 `shop/xxxDetail`，而**这 6 个模板从未存在**，也没有任何模板链接到它们。因此这6 条路由在重构前就是必然 500 的死路由。

本次重构不新增功能，故移除这 6 条死路由。与上面「修改密码」同源处理：**删代码、留规格**——不靠一具抛 500 的尸体承载产品线索。

以下为设计规格，**当前代码中并不存在**，此处仅作记录，供后续需要时实现参考。

- **原路由**：`GET /shop/{t}/detail.do?id={商品配置id}`，共 6 条。
- **重构后应有的路由**（待实现时按RT-1 的资源化命名对齐）：
  - `GET /shop/{t}/items/{id}` → 视图 `shop/{t}Detail`，展示单个商品的配置详情。
- **所需视图**：6 个 `templates/shop/{bait,trap,cage,nest,train,prop}Detail.html`。
- **可复用的数据访问**：`ShopItemAccessors.load(ShopKind, Integer)` 已提供按 id 单查，且返回已解决 `BaseBait.lu` 为 `Short`、其余五者为 `Integer` 差异的 `ShopItemView` 投影。注意该record **不可放入 Model**（SpEL 不认 record 的 `name()` 访问器，会抛 `EL1008E`），需先转成 JavaBean。
- **入口**：目前商品列表页无「详情」链接，实现时需一并补上。

### `fragment/shop.html` 的取款链接曾指向不存在的路由（已修正）

`fragment/shop.html` 顶部余额栏的「取款」链接原为 `@{/userBank.do}`，而 `/userBank.do` **全项目零 Controller 映射**，是条死链。现已改指向真实存在的 `@{/bank.do}`（`GameController`）。「获得钻石」的 `@{/pay.do}` 一直有效。

### 5 条道具「使用」死链（已置灰）

5 个包裹页（鸟笼、巢穴、道具、训练场、陷阱）各有一个「使用」按钮，原指向 `GET /pack/{cage,nest,prop,train,trap}/use.do`。这 5 条路由在全项目 `src/main/java/` 下零 Controller 映射，点下去必然 404。

**现状**：入口已保留但**置灰** —— 移除 `th:href`、改用 `<span class="text-muted" title="功能开发中，暂不可用">使用</span>`，文字与位置不变。置灰而非删除，是为了让玩家知道「这里有功能、只是还没做」，而不是让按钮凭空消失。

实现时需注意：应为 **POST** 而非 GET（消耗道具、变更状态属写操作）。

**诱饵的「使用」是另一条链路**：`pack/bait.html` 指向 `@{/trap/useBait.do(baitId=...)}`，该路由真实存在（`TrapController`），是活链接，未置灰。

### 17 处导航与功能入口死链（已置灰）

除上述 5 条外，另有 17 处入口同样指向不存在的路由，现已统一按同一方式置灰：

| 位置 | 原 `th:href` | 说明 |
|---|---|---|
| `fragment/navigation.html` | `/fight/index.do`、`/birth/index.do`、`/train/index.do`、`/storage/bird/list.do`、`/guild/index.do`、`/task/index.do`、`/marriage/index.do`、`/friend/list.do`、`/rank/level.do`、`/notice/list.do`、`/help.do`、`/online.do` | 主导航 12 项 |
| `role/bank.html` | `/userBankUp` | 存款「提升上限」 |
| `game.html` | `/notice/detail.do(id=0)` | 公告「真爱至上」 |
| `game.html` | `/task/list.do` | 主线任务 |
| `game.html` | `/weal/sign.do` | 福利签到 |
| `game.html` | `/trap/change.do(trapId=...)` | 更换陷阱 |
| `trap/over.html` | `/storage/bird.do`（2 处）、`/storage/bird/sellConfirm.do(birdId=...)` | 仓库与出售 |

**有意未置灰**的三条链接：

- `game.html` 的等级「详情」`@{/profile.do}` —— game 域去后缀后自动变为有效。
- `@{/trap/useBait.do(...)}` 与 `@{/trap/autoUseBait.do}` —— 均有真实映射（后者是仅返回 `redirectTo("/game.do")` 的空壳，属已知未实现功能，不是死链）。

### 已删除的 5 个零引用路由常量

`ConstData` 曾定义 5 个路由路径常量，全部**零引用**，且硬编码的 `.do` 后缀会与去后缀后的实际路由脱节，故一并删除：

| 已删除常量 | 原值 | 实际路由（现由 `@RequestMapping` 声明） |
|---|---|---|
| `PATH_LOGIN` | `/login.do` | `AccountController` → `/login` |
| `PATH_ROLE_LOGIN` | `/role/login.do` | `RoleController` → `/role/login` |
| `PATH_ROLE_CREATE` | `/role/create.do` | `RoleController` → `/role/create` |
| `PATH_ROLE_QUIT` | `/role/quit.do` | `RoleController` → `/role/quit` |
| `PATH_GAME` | `/game.do` | `GameController` → `/game.do`（**本轮未改，仍带后缀**） |

`ConstData.MESSAGE_PATH = "/message"` **保留并已启用** —— RT-5 起它是维护页路径的唯一来源，同时被 `GameController` 的映射、`GameStatusHandlerInterceptor` 的转发目标、`ServerWebMvcConfigurationSupport` 的 `excludePathPatterns` 三处引用。配套新增 `ConstData.MESSAGE_FORWARDED` 作为防递归标记键。

### 维护页路由曾三方不一致（已修）

改造前 `GameController` 的映射是 `/message.do`，而拦截器的转发目标与拦截器排除列表都写 `/message` —— **维护态下forward 到一个不存在的映射，必然 404**。现已三方统一为 `/message`，并加了一层与配置无关的兜底：`GameStatusHandlerInterceptor` 在 forward 前先打 `MESSAGE_FORWARDED` 标记，即使 exclude 列表被误改，退化结果也只是「重复 forward」而非 `StackOverflowError`。

### `WebMvcConfigurationSupport` 已迁移为 `WebMvcConfigurer`

原 `ServerWebMvcConfigurationSupport` 继承 `WebMvcConfigurationSupport`，这会让 Spring Boot 的 `WebMvcAutoConfiguration` **整体失效** —— MVC 的 `@ConfigurationProperties` 绑定、`HttpMessageConverter` 自动配置（即 `@ResponseBody` / JSON 序列化）、默认静态资源映射全部不生效。当前能跑只是因为该类手工注册了 `addResourceHandlers`，且项目尚无 `@ResponseBody` 用法。已改为实现 `WebMvcConfigurer` 接口并删除两处 `super.*` 调用，显式的 `/assets/**` 与 `/favicon.ico` 映射保留。

### 捕捉结算页是一次性渲染，无按 birdId 重开入口

`GET /trap/over` 展示的成长/克制值**来自本次捕捉流程的内存对象**，不是从 `storage_bird` 回读：

- `TrapController.doOver` 收网后把 `role_trap` 的 `catch_*` 字段**全部清零**（`TrapController.java:230-237`），没有"待结算"中间态；
- 页面文案取自 `BirdTextUtils.growText(bird.getGrow())`（`TrapController.java:256-257`），`bird` 是本次捕捉时生成并落库的内存实例；
- `GET /trap/birdInfo` 只读 `base_bird` 配置（类型/重量/单价/经验），**不含成长值**，不能当替代观测口。

**这是既有设计事实，不是本轮引入的缺陷。** 任何想验证或排查成长值的人都会撞到同一堵墙：
改库后无法通过重开页面观察变化。已实测确认并做过一次完整的
「捕捉 → 改库 → 复原」验证，等效证明链见 [`docs/验证记录/改库验证-成长值.md`](docs/验证记录/改库验证-成长值.md)。

若未来需要按 birdId 重查历史结算，属于**新增功能**，需单独立项。

### `ko` 的 `/100` 语义待产品确认

结算页克制值同样走 `BirdTextUtils.koText(ko)`，`ko` 存的是放大 100 倍的整数
（实测 `ko=0` 显示 `0.00%`）。这个 `/100` 约定**未见任何规格文档或注释说明**，
是逆向推断的结果。当前 `base_bird` 数据全部为 0，无法用真实数据反推验证。

**待产品确认**：`ko` 字段的业务含义、取值范围、以及"是否恒为 0"（若恒为 0，
则整个 `ko` 链路疑为未实现的占位）。

### 购买失败提示在两个域之间不一致（已知缺陷，本轮未修）

实测结论：

| 域 | 表现 |
|---|---|
| **诱饵域** `POST /shop/bait/purchase` | 货币/数量不足时**返回 200 且带提示块**，例如「您的爱心值不足(0/399)。」 |
| **陷阱域** `POST /shop/trap/purchase` | 同样失败时**返回 200 但响应体为空**（`size=850`），页面上看不到任何提示 |

**定位依据**：实测购买 `base_trap` id=18（`fee_type=3`，即爱心计价）时失败，
把角色 `level` 提到 20 仍失败——**说明并非等级门槛，而是货币种类分支**：
角色爱心为 0，而诱饵域的对应分支有兜底提示、陷阱域的对应分支没有。

**本轮不动**，理由：属提示文案/分支补全，是功能改动而非重构，且与本轮
"新增功能与 DB 写保持为 0"的红线冲突。留档供后续处理。

### `GET /trap/over` 仍是 GET 写操作（已知设计债）

`TrapController.doOver` 会消耗陷阱并结算捕获（**写库**），却仍是 `@GetMapping`。按 REST 语义应为 POST。本轮**有意不改**：改POST 需同步改造 `trap/over` 页的触发方式，属路由规范化批次的后续范围。已知缺陷是浏览器预取或刷新可能重复触发收网。

### 安全项整改记录（RT-6）

| 项 | 改造前 | 改造后 |
|---|---|---|
| 数据库凭据 | `application.properties` 明文 `root`/`root` | `${DB_USERNAME:root}` / `${DB_PASSWORD:root}`，URL 的主机/端口/库名同样可外置 |
| `CodeGen` 凭据 | `main()` 内明文 `root`/`root` | 读 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`（`main()` 无法用属性注入，只能 `System.getenv`） |
| CORS | `addAllowedOrigin("*")` +任意头 + 任意方法 | 由 `game.cors.allowed-origins` 驱动，**默认空 = 不开放任何跨域源**。本项目是服务端渲染单体，`src/main/resources` 下无任何 `fetch`/`axios`/`XMLHttpRequest` |
| Session Cookie | 只设了 `name` 与 `timeout` | 显式声明 `http-only=true`（虽是 Spring Boot 默认值）、`same-site=lax` |
| 405响应 | 被兜底handler 吞掉，返回 500「系统繁忙」 | `GlobalExceptionHandler` 新增 `HttpRequestMethodNotSupportedException` handler，返回真正的 **405**，并在响应里带上该路径支持的方法 |

**为什么 Cookie 不加 `secure`**：`server.port=80` 是明文 HTTP。一旦标记 `secure`，浏览器在 http 下就不再回传该 Cookie，**全站登录会立即失效**。若将来上 HTTPS，需同时改 `server.port`、设置 `server.forward-headers-strategy`，再开启 `secure=true`。

**为什么 CORS 选 Lax 而非 Strict**：Lax 允许「从站外链接点进来」的顶层导航携带 Cookie，而 Strict 会拦掉，导致从站外跳转进入的正常会话丢失；同时 Lax 仍能阻断「跨站 POST 表单提交」这一类 CSRF 攻击。
