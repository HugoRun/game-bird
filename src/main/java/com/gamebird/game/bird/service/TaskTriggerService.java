package com.gamebird.game.bird.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.Task;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 任务触发器：把「每日登录 / 捕捉小鸟 / 购买道具」三类游戏事件接到 task 表。
 *
 * <p>原先 task 表只有建表脚本与预置数据，全代码没有任何创建或推进任务的路径，
 * 任务页永远只有手工插库的假数据。本类补上三个触发点：
 * <ul>
 *   <li>{@link #onLogin}：每天首次进入游戏时创建「每日登录」任务并直接置为已完成未领；</li>
 *   <li>{@link #onCatch}：收网捕获时把进行中的「捕捉小鸟」任务置为已完成未领；</li>
 *   <li>{@link #onPurchase}：商店购买成功时推进「购买道具」任务。</li>
 * </ul>
 *
 * <p>task 表没有进度/目标数字段，因此采用「一次达标即完成」的最简口径；
 * 奖励金额在创建时按任务类型写入。触发方法全部吞异常——任务是旁路激励，
 * 不能让一次成功的收网因为任务写失败而报错。
 */
@Service
public class TaskTriggerService {

    /** 任务类型：每日登录。 */
    public static final int TASK_LOGIN = 1;
    /** 任务类型：捕捉小鸟。 */
    public static final int TASK_CATCH = 2;
    /** 任务类型：购买道具。 */
    public static final int TASK_PURCHASE = 3;
    /** 成就：图鉴收集（按收集物种数分档）。 */
    public static final int ACHIEVEMENT_BOOK = 4;
    /** 成就：首次孵出 5 星小鸟。 */
    public static final int ACHIEVEMENT_FIVE_STAR = 5;
    /** 成就：累计战斗胜利 10 场。 */
    public static final int ACHIEVEMENT_FIGHT_10 = 6;
    /** 成就：角色达到 10 级。 */
    public static final int ACHIEVEMENT_LEVEL_10 = 7;
    /** 成就：持有金币满 10 万。 */
    public static final int ACHIEVEMENT_RICH = 8;

    /** 三类任务的奖励金额（金币）。 */
    public static final long REWARD_LOGIN = 500;
    public static final long REWARD_CATCH = 300;
    public static final long REWARD_PURCHASE = 200;
    /** 图鉴成就各档奖励（金币）。 */
    public static final long REWARD_BOOK_10 = 500;
    public static final long REWARD_BOOK_30 = 1500;
    public static final long REWARD_BOOK_60 = 4000;
    public static final long REWARD_BOOK_ALL = 10000;
    /** 其余成就奖励（金币）。 */
    public static final long REWARD_FIVE_STAR = 2000;
    public static final long REWARD_FIGHT_10 = 1000;
    public static final long REWARD_LEVEL_10 = 1000;
    public static final long REWARD_RICH = 888;

    @Resource
    public TaskService taskService;

    /** 每日登录：当天尚无该任务时创建并直接完成（status=1 已完成未领）。 */
    public void onLogin(Role role) {
        try {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getRoleId, role.getRoleId());
            wrapper.eq(Task::getTaskType, TASK_LOGIN);
            wrapper.gt(Task::getCreateTime, dayStartSeconds());
            if (taskService.count(wrapper) > 0) {
                return;
            }
            Task task = new Task();
            task.setId(0);
            task.setRoleId(role.getRoleId());
            task.setTaskType(TASK_LOGIN);
            task.setStatus(1);
            task.setRewardCoin(REWARD_LOGIN);
            task.setCreateTime(CommonUtils.nowTime());
            task.setFinishTime(CommonUtils.nowTime());
            taskService.save(task);
        } catch (Exception ignored) {
            // 任务旁路失败不影响登录主流程
        }
    }

    /** 捕捉小鸟：把最早一条进行中的捕捉任务置为已完成未领。 */
    public void onCatch(Role role) {
        completeFirst(role, TASK_CATCH, REWARD_CATCH);
    }

    /** 购买道具：把最早一条进行中的购买任务置为已完成未领。 */
    public void onPurchase(Role role) {
        completeFirst(role, TASK_PURCHASE, REWARD_PURCHASE);
    }

    /**
     * 通用成就：该角色尚无此类型成就记录时，创建一条「已完成未领」成就任务。
     * 幂等——同类型成就只发一次。
     */
    public void onAchievement(Role role, int achievementType, long reward) {
        try {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getRoleId, role.getRoleId());
            wrapper.eq(Task::getTaskType, achievementType);
            if (taskService.count(wrapper) > 0) {
                return;
            }
            Task task = new Task();
            task.setId(0);
            task.setRoleId(role.getRoleId());
            task.setTaskType(achievementType);
            task.setStatus(1);
            task.setRewardCoin(reward);
            task.setCreateTime(CommonUtils.nowTime());
            task.setFinishTime(CommonUtils.nowTime());
            taskService.save(task);
        } catch (Exception ignored) {
            // 成就旁路失败不影响主流程
        }
    }

    /** 图鉴收集成就：收集物种数达到 10/30/60/全部分档时发对应档位（自动取最高未领档）。 */
    public void onCollect(Role role, long collectedKinds) {
        if (collectedKinds >= 111) {
            onAchievement(role, ACHIEVEMENT_BOOK, REWARD_BOOK_ALL);
            return;
        }
        if (collectedKinds >= 60) {
            onAchievement(role, ACHIEVEMENT_BOOK, REWARD_BOOK_60);
        }
        if (collectedKinds >= 30) {
            onAchievement(role, ACHIEVEMENT_BOOK, REWARD_BOOK_30);
        }
        if (collectedKinds >= 10) {
            onAchievement(role, ACHIEVEMENT_BOOK, REWARD_BOOK_10);
        }
    }

    /** 首只 5 星孵化成就。 */
    public void onFiveStarBirth(Role role) {
        onAchievement(role, ACHIEVEMENT_FIVE_STAR, REWARD_FIVE_STAR);
    }

    /** 战斗累计 10 胜成就（由调用方先 count 再传入）。 */
    public void onFightWinCount(Role role, long totalWins) {
        if (totalWins >= 10) {
            onAchievement(role, ACHIEVEMENT_FIGHT_10, REWARD_FIGHT_10);
        }
    }

    /** 角色 10 级成就（角色经验结算后检查）。 */
    public void onLevelUp(Role role) {
        if (role.getLevel() != null && role.getLevel() >= 10) {
            onAchievement(role, ACHIEVEMENT_LEVEL_10, REWARD_LEVEL_10);
        }
    }

    /** 富豪成就：金币满 10 万（加钱后检查）。 */
    public void onWealth(Role role) {
        if (role.getCoin() != null && role.getCoin() >= 100000) {
            onAchievement(role, ACHIEVEMENT_RICH, REWARD_RICH);
        }
    }

    /**
     * 把该角色最早一条指定类型的进行中任务置为已完成（status=1）。
     * 没有进行中的任务则静默忽略（不主动造任务，捕捉/购买类任务由每日触发补发）。
     */
    private void completeFirst(Role role, int taskType, long reward) {
        try {
            LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Task::getRoleId, role.getRoleId());
            wrapper.eq(Task::getTaskType, taskType);
            wrapper.eq(Task::getStatus, 0);
            wrapper.orderByAsc(Task::getId);
            wrapper.last("LIMIT 1");
            Task task = taskService.getOne(wrapper);
            if (task == null) {
                return;
            }
            task.setStatus(1);
            if (task.getRewardCoin() == null || task.getRewardCoin() <= 0) {
                task.setRewardCoin(reward);
            }
            task.setFinishTime(CommonUtils.nowTime());
            taskService.updateById(task);
        } catch (Exception ignored) {
            // 任务旁路失败不影响主流程
        }
    }

    /** 今天零点（东八区）的秒级时间戳。 */
    private int dayStartSeconds() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        return (int) (today.atStartOfDay(ZoneId.of("Asia/Shanghai")).toEpochSecond());
    }
}
