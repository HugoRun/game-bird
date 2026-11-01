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

    /** 三类任务的奖励金额（金币）。 */
    public static final long REWARD_LOGIN = 500;
    public static final long REWARD_CATCH = 300;
    public static final long REWARD_PURCHASE = 200;

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
