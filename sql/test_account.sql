-- =============================================================================
-- 测试账号（资源充足）
-- =============================================================================
-- 用途：本地 / E2E 测试用的满资源账号，省去每次手工注册、买道具、刷等级。
--
-- 账号：test        密码：test123
--
-- 与 sql/game_bird.sql 的关系：
--   本文件是【可选叠加脚本】，不被 docker 自动加载（Dockerfile 只把 game_bird.sql
--   挂到 /docker-entrypoint-initdb.d）。需要时手动执行一次：
--
--     # 已在运行的库（推荐，幂等，可重复执行）
--     cd docker
--     PW=$(grep '^MYSQL_ROOT_PASSWORD=' .env | cut -d= -f2-)
--     docker exec -i -e MYSQL_PWD="$PW" game-bird-db \
--         mysql -uroot game_bird < ../sql/test_account.sql
--
--     # 或跟着重建一起跑（⚠️ reset 会清空数据库）
--     ./deploy.sh reset
--
-- 设计说明：
--   * 密码为明文：AccountController.passwordMatches 用的是
--     MessageDigest.isEqual(输入字节, 库里字节)，是明文比对、并非哈希
--     （MessageDigest 这个 import 有误导性，别被它骗去加盐哈希）。
--   * 等级 200 必须直接写库：RoleValueService.calcRoleLevel 的循环上界是
--     i < 200，程序最多把角色推到 199 级，**推不到 200**。写进去后是稳定的——
--     再加经验时 calcRoleLevel(200, ...) 循环不执行，等级保持 200。
--   * exp 与 total_exp 取相等：addRoleExp 对两者同步递增，种子数据也正是相等
--     （如 level=6 时 exp=550 / total_exp=550）。数值取 base_level 里等级 200
--     的 total_exp，保证等级与经验自洽。
--   * ID 用 UUID，格式与 UUIDUtils.genUserId / genRoleId 一致
--     （genUserId = "bird-" + uuidhex，genRoleId = uuidhex）。
--   * 配套表 role_trap / role_train / role_birth 必须各有一行（idx=1）：
--     RoleController.postCreateRole:119-135 建角时会创建这三行，缺任意一行
--     对应页面会因查不到记录而异常。role_cage 建角时不创建，故不插。
--   * 幂等实现：account / role 有主键，用 ON DUPLICATE KEY；
--     role_trap / role_train / role_birth / role_pack / storage_bird **只有
--     自增主键 + 普通 KEY role_id，没有 (role_id, idx) 唯一键**，
--     所以 ON DUPLICATE KEY 不生效，必须用 NOT EXISTS / LEFT JOIN 反连接去重。
-- =============================================================================

-- ---- 账号 -------------------------------------------------------------------
INSERT INTO `account`
  (`user_id`, `platform`, `username`, `password`, `nickname`, `avatar_url`,
   `token`, `refresh_token`, `open_id`, `create_time`, `is_admin`, `is_ban`)
VALUES
  ('bird-167cd7669e5e433dad1b24b92caf18fa', 'bird', 'test', 'test123',
   '测试账号', '/assets/images/avatar/d.jpg', '', '', '', UNIX_TIMESTAMP(), 0, 0)
ON DUPLICATE KEY UPDATE
  `platform`   = VALUES(`platform`),
  `username`   = VALUES(`username`),
  `password`   = VALUES(`password`),
  `nickname`   = VALUES(`nickname`),
  `avatar_url` = VALUES(`avatar_url`),
  `is_admin`   = VALUES(`is_admin`),
  `is_ban`     = VALUES(`is_ban`);

-- ---- 角色（满资源）----------------------------------------------------------
-- 金钱 / 银行 / 钻石 / 爱心值取极大值，覆盖全部 fee_type（1金钱/2钻石/3爱心）的
-- 购买场景；钻石字段是 int unsigned，故取 999999 而非 999999999。
INSERT INTO `role`
  (`role_id`, `user_id`, `role_name`, `level`, `exp`, `total_exp`, `vip`, `vip_exp`,
   `total_vip_exp`, `sex`, `field_id`, `bird_num`, `coin`, `bank_coin`, `diamond`,
   `love_point`, `title_id`, `avatar_url`, `marriage_type`, `dynamic`,
   `reg_time`, `last_login_time`)
VALUES
  ('1e5a365c332e4b08b333726cf9691593', 'bird-167cd7669e5e433dad1b24b92caf18fa',
   '测试小号', 200, 4420697320, 4420697320, 0, 0, 0, 0, 1, 0,
   999999999, 999999999, 999999, 999999999, 0, '/assets/images/head/1.png',
   0, '资源充足的测试账号', UNIX_TIMESTAMP(), UNIX_TIMESTAMP())
ON DUPLICATE KEY UPDATE
  `user_id`     = VALUES(`user_id`),
  `role_name`   = VALUES(`role_name`),
  `level`       = VALUES(`level`),
  `exp`         = VALUES(`exp`),
  `total_exp`   = VALUES(`total_exp`),
  `field_id`    = VALUES(`field_id`),
  `coin`        = VALUES(`coin`),
  `bank_coin`   = VALUES(`bank_coin`),
  `diamond`     = VALUES(`diamond`),
  `love_point`  = VALUES(`love_point`),
  `avatar_url`  = VALUES(`avatar_url`),
  `dynamic`     = VALUES(`dynamic`),
  `last_login_time` = VALUES(`last_login_time`);

-- ---- 配套表（与建角逻辑一致，缺一则页面异常）---------------------------------
-- 陷阱槽位
INSERT INTO `role_trap`
  (`role_id`, `idx`, `bait_id`, `catch_bird_id`, `catch_bird_sex`,
   `catch_bird_weight`, `catch_bird_star`, `catch_time`, `catch_end_time`,
   `use_trap_id`, `use_trap_num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 1, 0, 0, 0, 0, 0, 0, 0, 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `role_trap`
    WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593' AND `idx` = 1
);

-- 训练场槽位
INSERT INTO `role_train`
  (`role_id`, `idx`, `bird_id`, `bird_base_id`, `train_time`, `train_end_time`,
   `use_train_id`, `use_train_expire_time`)
SELECT '1e5a365c332e4b08b333726cf9691593', 1, 0, 0, 0, 0, 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `role_train`
    WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593' AND `idx` = 1
);

-- 孵化场槽位
INSERT INTO `role_birth`
  (`role_id`, `idx`, `bird_id`, `bird_base_id`, `match_role_id`, `match_role_name`,
   `match_bird_id`, `match_bird_base_id`, `birth_time`, `birth_end_time`,
   `use_nest_id`, `use_nest_num`)
-- match_role_id 是 int unsigned NOT NULL，不能插 ''（会报 1366）；用 0 占位，
-- match_role_name 是 varchar，留空串。
SELECT '1e5a365c332e4b08b333726cf9691593', 1, 0, 0, 0, '', 0, 0, 0, 0, 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `role_birth`
    WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593' AND `idx` = 1
);

-- ---- 背包（全部配置的道具，各 999 个）---------------------------------------
-- type_id：1 诱饵 / 2 陷阱 / 3 鸟笼 / 4 巢穴 / 5 训练场 / 6 道具
-- 用 LEFT JOIN 反连接补缺失行，避免重复执行时插入重复背包行。
INSERT INTO `role_pack` (`role_id`, `type_id`, `base_id`, `num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 1, b.`id`, 999
FROM `base_bait` b
LEFT JOIN `role_pack` p
  ON p.`role_id` = '1e5a365c332e4b08b333726cf9691593' AND p.`type_id` = 1
 AND p.`base_id` = b.`id`
WHERE p.`id` IS NULL;

INSERT INTO `role_pack` (`role_id`, `type_id`, `base_id`, `num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 2, b.`id`, 999
FROM `base_trap` b
LEFT JOIN `role_pack` p
  ON p.`role_id` = '1e5a365c332e4b08b333726cf9691593' AND p.`type_id` = 2
 AND p.`base_id` = b.`id`
WHERE p.`id` IS NULL;

INSERT INTO `role_pack` (`role_id`, `type_id`, `base_id`, `num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 3, b.`id`, 999
FROM `base_cage` b
LEFT JOIN `role_pack` p
  ON p.`role_id` = '1e5a365c332e4b08b333726cf9691593' AND p.`type_id` = 3
 AND p.`base_id` = b.`id`
WHERE p.`id` IS NULL;

INSERT INTO `role_pack` (`role_id`, `type_id`, `base_id`, `num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 4, b.`id`, 999
FROM `base_nest` b
LEFT JOIN `role_pack` p
  ON p.`role_id` = '1e5a365c332e4b08b333726cf9691593' AND p.`type_id` = 4
 AND p.`base_id` = b.`id`
WHERE p.`id` IS NULL;

INSERT INTO `role_pack` (`role_id`, `type_id`, `base_id`, `num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 5, b.`id`, 999
FROM `base_train` b
LEFT JOIN `role_pack` p
  ON p.`role_id` = '1e5a365c332e4b08b333726cf9691593' AND p.`type_id` = 5
 AND p.`base_id` = b.`id`
WHERE p.`id` IS NULL;

INSERT INTO `role_pack` (`role_id`, `type_id`, `base_id`, `num`)
SELECT '1e5a365c332e4b08b333726cf9691593', 6, b.`id`, 999
FROM `base_prop` b
LEFT JOIN `role_pack` p
  ON p.`role_id` = '1e5a365c332e4b08b333726cf9691593' AND p.`type_id` = 6
 AND p.`base_id` = b.`id`
WHERE p.`id` IS NULL;

-- 已存在的行补满数量（玩掉之后重复执行本脚本可回血）
UPDATE `role_pack`
SET `num` = 999
WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593' AND `num` < 999;

-- ---- 小鸟（每种配置各 2 只：1 雄 / 2 雌，满星满重）--------------------------
-- star 取 5：GameUtils.getStarName 只认 0..5，其余返回空串，别写 6 以上。
-- grow 取 1000（显示时 /100 → 10.00%），与种子数据同量级。
-- weight / init_weight 取该配置的 max_weight。
INSERT INTO `storage_bird`
  (`role_id`, `bird_id`, `level`, `exp`, `total_exp`, `sex`,
   `init_weight`, `weight`, `star`, `grow`, `ko`, `is_lock`, `time`)
SELECT '1e5a365c332e4b08b333726cf9691593', b.`id`, 1, 0, 0, 1,
       b.`max_weight`, b.`max_weight`, 5, 1000, 0, 0, UNIX_TIMESTAMP()
FROM `base_bird` b
WHERE NOT EXISTS (
    SELECT 1 FROM `storage_bird` s
    WHERE s.`role_id` = '1e5a365c332e4b08b333726cf9691593'
      AND s.`bird_id` = b.`id` AND s.`sex` = 1
);

INSERT INTO `storage_bird`
  (`role_id`, `bird_id`, `level`, `exp`, `total_exp`, `sex`,
   `init_weight`, `weight`, `star`, `grow`, `ko`, `is_lock`, `time`)
SELECT '1e5a365c332e4b08b333726cf9691593', b.`id`, 1, 0, 0, 2,
       b.`max_weight`, b.`max_weight`, 5, 1000, 0, 0, UNIX_TIMESTAMP()
FROM `base_bird` b
WHERE NOT EXISTS (
    SELECT 1 FROM `storage_bird` s
    WHERE s.`role_id` = '1e5a365c332e4b08b333726cf9691593'
      AND s.`bird_id` = b.`id` AND s.`sex` = 2
);

-- bird_num 只写库不参与业务计算（全项目无 getBirdNum 调用），仍同步一下以免显脏
UPDATE `role`
SET `bird_num` = (
    SELECT COUNT(*) FROM `storage_bird`
    WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
)
WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593';

-- ---- 执行后自检（应全部为非空/非零）-----------------------------------------
SELECT '账号' AS `项`, `username` AS `值` FROM `account`
  WHERE `user_id` = 'bird-167cd7669e5e433dad1b24b92caf18fa'
UNION ALL SELECT '角色等级', `level` FROM `role`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
UNION ALL SELECT '金钱', `coin` FROM `role`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
UNION ALL SELECT '银行', `bank_coin` FROM `role`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
UNION ALL SELECT '钻石', `diamond` FROM `role`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
UNION ALL SELECT '爱心值', `love_point` FROM `role`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
UNION ALL SELECT '背包条目', COUNT(*) FROM `role_pack`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593'
UNION ALL SELECT '小鸟数量', COUNT(*) FROM `storage_bird`
  WHERE `role_id` = '1e5a365c332e4b08b333726cf9691593';
