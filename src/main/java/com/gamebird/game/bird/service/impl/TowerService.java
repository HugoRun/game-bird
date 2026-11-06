package com.gamebird.game.bird.service.impl;

import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.model.dbo.FightLog;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleCage;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.model.dbo.TowerRecord;
import com.gamebird.game.bird.service.FightLogService;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.RoleCageService;
import com.gamebird.game.bird.service.RoleService;
import com.gamebird.game.bird.service.RoleValueService;
import com.gamebird.game.bird.service.StorageBirdService;
import com.gamebird.game.bird.service.TowerRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 试炼之塔（PVE 爬塔）。
 *
 * <p>猪头大军盘踞的无尽塔：每层守卫战力随层数线性增长，战胜推进到下一层，
 * 失败原地不动；每天 3 次挑战机会（按东八区日期刷新）。奖励随层数提升，
 * 第 3/5 层倍数档给钻石。与入侵守卫（限时窗口）互补，构成可持续的 PVE 循环。
 *
 * <p>战力公式与战斗一致：出战鸟（等级/星级/成长/克制）+ 角色等级。
 */
@Service
public class TowerService {

    /** 每日挑战次数上限。 */
    public static final int DAILY_LIMIT = 3;
    /** 击败的固定事件文案前缀（与树林记录联动，勿改字面量）。 */
    public static final String TOWER_WIN_PREFIX = "登上了试炼之塔";

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    public TowerRecordService towerRecordService;

    @Resource
    public FightLogService fightLogService;

    @Resource
    public StorageBirdService storageBirdService;

    @Resource
    public RoleCageService cageService;

    @Resource
    public GameEventService gameEventService;

    @Resource
    public QuestService questService;

    @Resource
    public RoleService roleService;

    @Resource
    public RoleValueService roleValueService;

    /** 取（或补建）角色的塔记录。 */
    public TowerRecord record(String roleId) {
        TowerRecord record = towerRecordService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TowerRecord>()
                        .eq(TowerRecord::getRoleId, roleId)
                        .last("LIMIT 1"));
        if (record == null) {
            record = new TowerRecord();
            record.setId(0);
            record.setRoleId(roleId);
            record.setFloor(1);
            record.setBestFloor(0);
            record.setDailyDate(today());
            record.setDailyAttempts(0);
            record.setUpdateTime(CommonUtils.nowTime());
            towerRecordService.save(record);
        }
        return record;
    }

    /** 今日剩余挑战次数。 */
    public int remainAttempts(TowerRecord record) {
        return Math.max(0, DAILY_LIMIT - attemptsOf(record));
    }

    /**
     * 发起一次挑战。
     *
     * @return 结果文案；null 表示未开战（次数用尽等），由调用方给默认提示
     */
    @Transactional(rollbackFor = Exception.class)
    public String challenge(Role role) {
        TowerRecord record = record(role.getRoleId());
        rollDailyIfNeeded(record);
        if (record.getDailyAttempts() >= DAILY_LIMIT) {
            return null;
        }
        int floor = record.getFloor() == null || record.getFloor() < 1 ? 1 : record.getFloor();
        // 双方战力：我方 = 出战鸟 + 角色等级；猪头守卫 = 层数×10 + 随机浮动
        int myPower = (role.getLevel() == null ? 0 : role.getLevel()) * 10;
        StorageBird myBird = defendBird(role.getRoleId());
        if (myBird != null) {
            myPower += (myBird.getLevel() == null ? 1 : myBird.getLevel()) * 20
                    + (myBird.getStar() == null ? 0 : myBird.getStar()) * 50
                    + (myBird.getGrow() == null ? 0 : myBird.getGrow()) / 10;
        }
        int pigPower = floor * 10 + CommonUtils.randNum(0, 4 + floor);
        int winRate = Math.max(15, Math.min(85, myPower * 100 / Math.max(1, myPower + pigPower)));
        boolean win = CommonUtils.randNum(1, 100) <= winRate;
        // 消耗一次机会
        record.setDailyAttempts(record.getDailyAttempts() + 1);
        record.setUpdateTime(CommonUtils.nowTime());
        towerRecordService.updateById(record);
        // 战斗记录
        FightLog log = new FightLog();
        log.setRoleId(role.getRoleId());
        log.setOpponentName("试炼之塔 B" + floor + " 层·猪头守卫");
        log.setFightTime(CommonUtils.nowTime());
        String text;
        if (win) {
            long coin = 80 + floor * 120L;
            int diamond = floor % 5 == 0 ? 5 : (floor % 3 == 0 ? 2 : 0);
            role.setCoin((role.getCoin() == null ? 0L : role.getCoin()) + coin);
            if (diamond > 0) {
                role.setDiamond((role.getDiamond() == null ? 0 : role.getDiamond()) + diamond);
            }
            settleRewards(role);
            log.setResult(1);
            log.setRewardCoin(coin);
            text = "攻占了 B" + floor + " 层！（胜率 " + winRate + "%）缴获金币 " + coin
                    + (diamond > 0 ? "、钻石 " + diamond : "") + "，下一层是 B" + (floor + 1) + "。";
            // 推进层数
            record.setFloor(floor + 1);
            record.setBestFloor(Math.max(record.getBestFloor() == null ? 0 : record.getBestFloor(), floor));
            towerRecordService.updateById(record);
            gameEventService.record(role.getRoleId(),
                    TOWER_WIN_PREFIX + " B" + floor + " 层，缴获金币" + coin + "。");
            questService.onEvent(role, QuestService.EV_FIGHT_WIN, 1);
        } else {
            log.setResult(0);
            log.setRewardCoin(0L);
            text = "B" + floor + " 层的猪头守卫太凶悍，被轰了下来。（胜率 " + winRate
                    + "%）去训练场强化小鸟再战。";
        }
        fightLogService.save(log);
        return text;
    }

    /** 按日期滚动每日计数。 */
    private void rollDailyIfNeeded(TowerRecord record) {
        String today = today();
        if (!today.equals(record.getDailyDate())) {
            record.setDailyDate(today);
            record.setDailyAttempts(0);
            towerRecordService.updateById(record);
        }
    }

    private int attemptsOf(TowerRecord record) {
        return today().equals(record.getDailyDate()) && record.getDailyAttempts() != null
                ? record.getDailyAttempts() : 0;
    }

    /** 战斗经验结算并落库。 */
    private void settleRewards(Role role) {
        roleValueService.addRoleExp(role, 40L + (role.getLevel() == null ? 1 : role.getLevel()) * 10L);
        roleService.updateById(role);
    }

    /** 防守用的出战鸟。 */
    private StorageBird defendBird(String roleId) {
        RoleCage cage = cageService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleCage>()
                        .eq(RoleCage::getRoleId, roleId)
                        .eq(RoleCage::getIdx, 1)
                        .last("LIMIT 1"));
        if (cage == null || cage.getBirdId() == null || cage.getBirdId() <= 0) {
            return null;
        }
        StorageBird bird = storageBirdService.getById(cage.getBirdId());
        return bird != null && roleId.equals(bird.getRoleId()) ? bird : null;
    }

    private String today() {
        return LocalDate.now(ZONE).format(DAY);
    }
}
