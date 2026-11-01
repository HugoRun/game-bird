-- 每日签到表（与 game_bird.sql 解耦，单独维护，便于增量导入）。
-- 使用 CREATE TABLE IF NOT EXISTS，重复执行不会报错。

CREATE TABLE IF NOT EXISTS `sign_log` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '所属角色Id',
  `sign_date` varchar(8) NOT NULL DEFAULT '' COMMENT '签到日 yyyyMMdd（东八区）',
  `continuous` int(11) UNSIGNED NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `reward_coin` bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '本次奖励金币',
  `reward_diamond` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '本次奖励钻石',
  `create_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_date` (`role_id`, `sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='每日签到表';
