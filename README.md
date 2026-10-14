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

### `fragment/shop.html` 中 2 处指向 game 域的死链

`fragment/shop.html` 顶部的余额栏有两个链接：`@{/userBank.do}`（取款）与 `@{/pay.do}`（获得钻石）。

- `/pay.do` 由 `GameController` 处理，**可用**，只是尚未随 game 域一起去后缀。
- `/userBank.do` **全项目零 Controller 映射**，是死链（`userBank` 在 `src/main/java/` 下无任何路由）。它属于 game 域，按「本轮不新增功能、不修死链」的约定保持原样，随 game 域批次一并处理。

### 5 条道具「使用」死链

5 个包裹页（鸟笼、巢穴、道具、训练场、陷阱）各有一个「使用」按钮，指向 `GET /pack/{cage,nest,prop,train,trap}/use.do`，携带参数 `cageId` / `nestId` / `propId` / `trainId` / `trapId`。

这5 条路由**在全项目 `src/main/java/` 下零 Controller 映射**，点下去必然 404。它们与上面的 `/userBank.do` 同属「模板有入口、路由不存在」的死链，按本轮约定保持原样，重构不改。

- **原路由**：
  - `GET /pack/cage/use.do?cageId={id}`
  - `GET /pack/nest/use.do?nestId={id}`
  - `GET /pack/prop/use.do?propId={id}`
  - `GET /pack/train/use.do?trainId={id}`
  - `GET /pack/trap/use.do?trapId={id}`
- **待实现时的两点提示**：
  - **应为 POST 而非 GET**。这 5 个都是「使用」写操作（消耗道具、变更状态），与本轮已处理的 4 个 GET 写操作（`doChangeAvatar`、`doChangeTitle`、`doOver`、`pageTest`）同性质，实现时必须一并改为 POST + form 结构，不能沿用当前的 `<a href>`。
  - **诱饵的「使用」是另一条链路**：`pack/bait.html` 指向 `@{/trap/useBait.do(baitId=...)}`，属于 trap 域，与上述 5 条无关，需单独评估。

