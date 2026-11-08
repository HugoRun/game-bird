-- 任务 / 公告 新表结构（与 game_bird.sql 解耦，单独维护，便于增量导入）。
-- 使用 CREATE TABLE IF NOT EXISTS，重复执行不会报错。

CREATE TABLE IF NOT EXISTS `task` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '所属角色Id',
  `task_type` tinyint(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '任务类型:1每日登录/2捕捉小鸟/3购买道具',
  `status` tinyint(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态:0进行中/1已完成未领/2已领取',
  `reward_coin` bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '奖励金钱',
  `create_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `finish_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务表';

CREATE TABLE IF NOT EXISTS `board` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `content` varchar(500) NOT NULL DEFAULT '' COMMENT '内容',
  `create_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- 预置一条示例公告（幂等：固定 id + ON DUPLICATE KEY，重复执行只更新标题/内容）。
INSERT INTO `board` (`id`, `title`, `content`, `create_time`)
VALUES (1, '欢迎来到小鸟情人', '这是一段示例公告，后续可由后台维护。', UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `content` = VALUES(`content`);
