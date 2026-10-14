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

