-- 公告种子数据编码修复（07 号增量脚本，幂等）。
--
-- 背景：task_board.sql 的中文 INSERT 在部分初始化路径下以错误客户端字符集
-- 执行，导致 board 表存入双重编码乱码（页面上公告栏与公告列表显示乱码）。
-- 本脚本用正确的 utf8mb4 连接重写种子公告。
-- 附：task_board.sql 头部已补 SET NAMES utf8mb4 防再犯。

SET NAMES utf8mb4;

UPDATE `board`
  SET `title` = '欢迎来到小鸟情人',
      `content` = '新版本上线：场景捕捉、小鸟图鉴、竞技场、入侵守卫与图鉴成就已开放。在鸟的主场捕捉有经验加成，公会等级还能提升捕获收益。祝各位捕鸟愉快！'
  WHERE `id` = 1;
