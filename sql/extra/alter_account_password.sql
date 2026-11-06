-- 口令哈希兼容的列宽调整（与红线文件 game_bird.sql 解耦，单独维护）。
--
-- 背景：口令存储从明文升级为 BCrypt 哈希（固定 60 字符）后，发现两处列长不足：
--   1. account.password varchar(50)：BCrypt 哈希 60 字符写入即报
--      「Data too long for column 'password'」，导致注册 500、无法登录。
--   2. fight_log.opponent_name varchar(50)：战斗记录的对手文案为
--      「鸟名(玩家名)」，玩家名上限 50，拼接后超长。
--
-- 说明：本脚本是对已有表【加宽列长】而非新增表，属增量脚本特例——
-- 哈希存储无法在 50 字符内完成，且加宽 varchar 向后兼容、不触碰任何
-- 红线字段名。重复执行无副作用（MODIFY 为幂等定义）。

ALTER TABLE `account`
  MODIFY COLUMN `password` varchar(80) NOT NULL DEFAULT '' COMMENT '密码(BCrypt哈希)';

ALTER TABLE `fight_log`
  MODIFY COLUMN `opponent_name` varchar(100) NOT NULL DEFAULT '' COMMENT '对手名称';
