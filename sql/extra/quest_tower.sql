-- 奋进目标（主线/每日任务链）+ 试炼之塔（PVE）——09 号增量脚本，幂等。
-- 与红线文件 game_bird.sql 解耦；task 表继续承载「自动触发的日常与成就」，
-- 本脚本的任务链独立成 quest_def/quest_progress 两张表，支持进度计数、
-- 前置串联与道具奖励。

SET NAMES utf8mb4;

-- ==================== 任务定义表 ====================
CREATE TABLE IF NOT EXISTS `quest_def` (
  `id` int(11) UNSIGNED NOT NULL,
  `title` varchar(50) NOT NULL DEFAULT '' COMMENT '任务名',
  `desc_text` varchar(200) NOT NULL DEFAULT '' COMMENT '达成条件说明',
  `chapter` tinyint(4) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1主线/2每日',
  `event_type` tinyint(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '进度事件:1登录/2捕捉/3购买/4训练/5孵化/6战斗胜/7出售/8等级',
  `target_num` int(11) UNSIGNED NOT NULL DEFAULT 1 COMMENT '目标次数（等级型为目标等级）',
  `pre_quest_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '前置任务id（0=无，主线串联）',
  `reward_coin` bigint(20) UNSIGNED NOT NULL DEFAULT 0,
  `reward_diamond` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `reward_prop_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '奖励道具配置id（0=无）',
  `reward_prop_num` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `sort` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `chapter` (`chapter`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='奋进目标定义表';

-- ==================== 任务进度表 ====================
CREATE TABLE IF NOT EXISTS `quest_progress` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` varchar(50) NOT NULL DEFAULT '',
  `quest_id` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `progress` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '当前进度',
  `status` tinyint(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0进行中/1可交付/2已交付',
  `day_date` varchar(8) NOT NULL DEFAULT '' COMMENT '每日任务的归属日 yyyyMMdd',
  `create_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `finish_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `role_quest` (`role_id`, `quest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='奋进目标进度表';

-- ==================== 试炼之塔记录表 ====================
CREATE TABLE IF NOT EXISTS `tower_record` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` varchar(50) NOT NULL DEFAULT '',
  `floor` int(11) UNSIGNED NOT NULL DEFAULT 1 COMMENT '当前挑战层',
  `best_floor` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '历史最高层',
  `daily_date` varchar(8) NOT NULL DEFAULT '' COMMENT '当日挑战计数归属日',
  `daily_attempts` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `update_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试炼之塔记录表';

-- ==================== 主线任务链（10 章，贴合猪头大军世界观） ====================
INSERT INTO `quest_def` (`id`, `title`, `desc_text`, `chapter`, `event_type`, `target_num`, `pre_quest_id`, `reward_coin`, `reward_diamond`, `reward_prop_id`, `reward_prop_num`, `sort`) VALUES
(101, '第一章·初出茅庐', '在树林里捕捉 1 只小鸟', 1, 2, 1, 0, 500, 0, 0, 0, 1),
(102, '第二章·磨刀霍霍', '在商店购买任意 1 份商品', 1, 3, 1, 101, 800, 0, 0, 0, 2),
(103, '第三章·小试牛刀', '累计捕捉 5 只小鸟', 1, 2, 5, 102, 1500, 5, 0, 0, 3),
(104, '第四章·修行之路', '送小鸟完成 1 次训练并领取成果', 1, 4, 1, 103, 2000, 0, 0, 0, 4),
(105, '第五章·爱的结晶', '完成 1 次配对并收获爱情结晶', 1, 5, 1, 104, 3000, 10, 0, 0, 5),
(106, '第六章·树林守护', '赢得 1 场战斗的胜利', 1, 6, 1, 105, 3000, 0, 0, 0, 6),
(107, '第七章·财源广进', '出售 3 只小鸟（一键出售也算）', 1, 7, 3, 106, 5000, 0, 0, 0, 7),
(108, '第八章·挑战祥瑞之地', '前往梦想花园并捕捉 1 只小鸟', 1, 2, 1, 107, 10000, 20, 0, 0, 8),
(109, '第九章·百鸟朝凤', '图鉴收集满 20 种小鸟', 1, 9, 20, 108, 8000, 0, 80, 2, 9),
(110, '终章·百战之王', '累计赢得 10 场战斗', 1, 6, 10, 109, 20000, 50, 0, 0, 10);

-- ==================== 每日任务（每天刷新，3 项） ====================
INSERT INTO `quest_def` (`id`, `title`, `desc_text`, `chapter`, `event_type`, `target_num`, `pre_quest_id`, `reward_coin`, `reward_diamond`, `reward_prop_id`, `reward_prop_num`, `sort`) VALUES
(201, '每日·勤快捕手', '今天捕捉 3 只小鸟', 2, 2, 3, 0, 600, 0, 0, 0, 1),
(202, '每日·以战养战', '今天赢下 1 场战斗（或试炼之塔）', 2, 6, 1, 0, 400, 0, 0, 0, 2),
(203, '每日·签到打卡', '今天完成一次签到', 2, 1, 1, 0, 300, 2, 0, 0, 3)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);
