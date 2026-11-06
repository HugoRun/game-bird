-- 好友 / 公会 模块新增表（与 game_bird.sql 分离，便于增量部署）
-- 仅新增，不修改任何已有表。使用 IF NOT EXISTS 保证可重复执行。

CREATE TABLE IF NOT EXISTS `friend` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '所属角色Id',
  `friend_role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '好友角色Id',
  `friend_name` varchar(50) NOT NULL DEFAULT '' COMMENT '好友名称',
  `create_time` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '添加时间戳',
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='好友表';

CREATE TABLE IF NOT EXISTS `guild` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '公会名称',
  `leader_role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '会长角色Id',
  `level` int(11) UNSIGNED NOT NULL DEFAULT 1 COMMENT '公会等级',
  `create_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `leader_role_id` (`leader_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公会表';

CREATE TABLE IF NOT EXISTS `guild_member` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `guild_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '公会Id',
  `role_id` varchar(50) NOT NULL DEFAULT '' COMMENT '成员角色Id',
  `join_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `role_id` (`role_id`),
  KEY `guild_id` (`guild_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公会成员表';
