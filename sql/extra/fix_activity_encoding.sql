-- 活动种子数据编码修复（10 号增量脚本，幂等）。
--
-- 背景：act_fight.sql 的中文 INSERT 与 task_board.sql 同源问题——初始化管道
-- 按错误客户端字符集解析，activity 表存入双重编码乱码（活动中心标题乱码）。
-- 本脚本用正确的 utf8mb4 连接重写预置活动。
-- 附：act_fight.sql 头部已补 SET NAMES utf8mb4 防再犯。

SET NAMES utf8mb4;

UPDATE `activity`
  SET `title` = '新手狂欢周',
      `content` = '全服活动火热进行中：试炼之塔、神兽资料片与奋进目标已开放。活动期间在梦想花园捕捉，有小概率偶遇稀有神兽！'
  WHERE `id` = 1;
