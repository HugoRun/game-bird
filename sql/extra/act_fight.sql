-- ===========================================================================
-- act / fight 模块增量脚本
-- 红线文件 sql/game_bird.sql 严禁修改，所有新增表结构写入本文件。
-- 本脚本使用 CREATE TABLE IF NOT EXISTS，可重复执行；种子数据使用
-- INSERT ... ON DUPLICATE KEY UPDATE，固定 id 保证幂等。
-- ===========================================================================

CREATE TABLE IF NOT EXISTS `activity` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '标题',
  `content` varchar(500) NOT NULL DEFAULT '' COMMENT '内容',
  `start_time` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '开始时间戳',
  `end_time` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '结束时间戳',
  `status` tinyint(4) UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态:0下线/1上线',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动表';

CREATE TABLE IF NOT EXISTS `fight_log` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '挑战者角色Id',
  `opponent_name` varchar(50) NOT NULL DEFAULT '' COMMENT '对手名称',
  `result` tinyint(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '结果:0负/1胜',
  `reward_coin` bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '胜利奖励金钱',
  `fight_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='战斗记录表';

-- 预置一条示例活动（固定 id=1，重复执行安全）
INSERT INTO `activity` (`id`, `title`, `content`, `start_time`, `end_time`, `status`)
VALUES (1, '新手狂欢周', '欢迎来到小鸟情人，活动期间完成任务可获得丰厚奖励！', UNIX_TIMESTAMP(), UNIX_TIMESTAMP() + 30 * 24 * 60 * 60, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `content` = VALUES(`content`),
  `start_time` = VALUES(`start_time`),
  `end_time` = VALUES(`end_time`),
  `status` = VALUES(`status`);
