package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.model.dbo.QuestDef;
import com.gamebird.game.bird.model.dbo.QuestProgress;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.service.QuestDefService;
import com.gamebird.game.bird.service.QuestProgressService;
import com.gamebird.game.bird.service.RolePackService;
import com.gamebird.game.bird.service.RoleValueService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 奋进目标引擎（主线任务链 + 每日任务）。
 *
 * <p>与 task 表的「自动触发即完成」不同，本系统支持<b>进度计数</b>
 * （如"累计捕捉 5 只"）、<b>前置串联</b>（主线逐章解锁）与<b>道具奖励</b>。
 * 定义在 quest_def（09 号增量脚本），进度在 quest_progress。
 *
 * <p><b>事件驱动</b>：各玩法在关键节点调用 {@link #onEvent}（事件类型见常量），
 * 引擎推进对应类型的进行中任务；等级/图鉴类任务以「快照取最大」推进。
 * 引擎内部吞异常——任务旁路失败不影响玩法主流程。
 *
 * <p><b>每日任务</b>：进度行带 {@code day_date} 归属日，进入相关页面时
 * 补建当天的每日任务行，跨天自动出现新的一轮。
 */
@Service
public class QuestService {

    /** 事件类型：与 quest_def.event_type 同源。 */
    public static final int EV_LOGIN = 1;
    public static final int EV_CATCH = 2;
    public static final int EV_PURCHASE = 3;
    public static final int EV_TRAIN = 4;
    public static final int EV_BIRTH = 5;
    public static final int EV_FIGHT_WIN = 6;
    public static final int EV_SELL = 7;
    public static final int EV_LEVEL = 8;
    public static final int EV_BOOK = 9;

    /** 章节：主线 / 每日。 */
    public static final int CHAPTER_MAIN = 1;
    public static final int CHAPTER_DAILY = 2;

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    public QuestDefService questDefService;

    @Resource
    public QuestProgressService questProgressService;

    @Resource
    public RolePackService packService;

    @Resource
    public RoleValueService roleValueService;

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public CollectionService collectionService;

    /**
     * 事件推进：把该角色进行中、事件类型匹配的任务进度 +delta；
     * 等级/图鉴类（EV_LEVEL/EV_BOOK）以快照取最大值推进。
     * 达到目标自动置为「可交付」。
     */
    public void onEvent(Role role, int eventType, int delta) {
        try {
            ensureDailyProgress(role);
            int today = CommonUtils.nowTime();
            for (QuestProgress progress : activeProgressRows(role.getRoleId())) {
                QuestDef def = questDefService.getById(progress.getQuestId());
                if (def == null || def.getEventType() == null || def.getEventType() != eventType) {
                    continue;
                }
                int target = def.getTargetNum() == null ? 1 : def.getTargetNum();
                if (eventType == EV_LEVEL) {
                    int level = role.getLevel() == null ? 0 : role.getLevel();
                    progress.setProgress(Math.max(progress.getProgress(), level));
                } else if (eventType == EV_BOOK) {
                    long collected = collectionService.countCollected(role.getRoleId());
                    progress.setProgress((int) Math.max(progress.getProgress(), collected));
                } else {
                    progress.setProgress(progress.getProgress() + Math.max(0, delta));
                }
                if (progress.getProgress() >= target) {
                    progress.setProgress(target);
                    progress.setStatus(1);
                    progress.setFinishTime(today);
                }
                questProgressService.updateById(progress);
            }
        } catch (Exception ignored) {
            // 任务旁路失败不影响玩法主流程
        }
    }

    /** 任务中心的完整视图：主线（含未解锁的后续章）+ 今日每日。 */
    public List<QuestView> listFor(Role role) {
        ensureDailyProgress(role);
        ensureChainProgress(role);
        List<QuestView> views = new ArrayList<>();
        List<QuestDef> defs = questDefService.list(new LambdaQueryWrapper<QuestDef>()
                .in(QuestDef::getChapter, CHAPTER_MAIN, CHAPTER_DAILY)
                .orderByAsc(QuestDef::getChapter).orderByAsc(QuestDef::getSort));
        for (QuestDef def : defs) {
            QuestView view = new QuestView();
            view.def = def;
            if (def.getChapter() == CHAPTER_DAILY) {
                view.progress = findProgress(role.getRoleId(), def.getId(), today());
            } else {
                view.progress = findProgress(role.getRoleId(), def.getId(), null);
                if (view.progress == null && !chainUnlocked(def, role.getRoleId())) {
                    view.locked = true;
                }
            }
            views.add(view);
        }
        return views;
    }

    /**
     * 交付任务奖励：金币 + 钻石 + 可选道具。只有「可交付」状态可交付。
     *
     * @return 结果文案（失败时为原因）
     */
    @Transactional(rollbackFor = Exception.class)
    public String deliver(Role role, int questId) {
        QuestDef def = questDefService.getById(questId);
        if (def == null) {
            return null;
        }
        QuestProgress progress = def.getChapter() == CHAPTER_DAILY
                ? findProgress(role.getRoleId(), questId, today())
                : findProgress(role.getRoleId(), questId, null);
        if (progress == null || progress.getStatus() == null || progress.getStatus() != 1) {
            return null;
        }
        long coin = def.getRewardCoin() == null ? 0 : def.getRewardCoin();
        int diamond = def.getRewardDiamond() == null ? 0 : def.getRewardDiamond();
        StringBuilder text = new StringBuilder("「").append(def.getTitle()).append("」交付成功：");
        if (coin > 0) {
            roleValueService.addRoleCoin(role, coin);
            text.append("金币+").append(coin).append(" ");
        }
        if (diamond > 0) {
            roleValueService.addRoleDiamond(role, diamond);
            text.append("钻石+").append(diamond).append(" ");
        }
        if (def.getRewardPropId() != null && def.getRewardPropId() > 0 && def.getRewardPropNum() != null && def.getRewardPropNum() > 0) {
            addPackItem(role, def.getRewardPropId(), def.getRewardPropNum());
            String propName = "神秘道具";
            if (configGateway.getBaseProp(def.getRewardPropId()) != null) {
                propName = configGateway.getBaseProp(def.getRewardPropId()).getName();
            }
            text.append(propName).append("×").append(def.getRewardPropNum()).append(" ");
        }
        roleValueService.addRoleExp(role, 50L);
        text.append("经验+50");
        progress.setStatus(2);
        progress.setFinishTime(CommonUtils.nowTime());
        questProgressService.updateById(progress);
        return text.toString();
    }

    // ==================== 内部装配 ====================

    /** 补建今天缺失的每日任务进度行。 */
    private void ensureDailyProgress(Role role) {
        String today = today();
        List<QuestDef> dailies = questDefService.list(new LambdaQueryWrapper<QuestDef>()
                .eq(QuestDef::getChapter, CHAPTER_DAILY));
        for (QuestDef def : dailies) {
            if (findProgress(role.getRoleId(), def.getId(), today) == null) {
                createProgress(role.getRoleId(), def.getId(), today);
            }
        }
    }

    /** 补建主线链上「前置已交付且尚无进度行」的任务（逐章自动接取）。 */
    private void ensureChainProgress(Role role) {
        List<QuestDef> mains = questDefService.list(new LambdaQueryWrapper<QuestDef>()
                .eq(QuestDef::getChapter, CHAPTER_MAIN).orderByAsc(QuestDef::getSort));
        for (QuestDef def : mains) {
            if (findProgress(role.getRoleId(), def.getId(), null) == null && chainUnlocked(def, role.getRoleId())) {
                createProgress(role.getRoleId(), def.getId(), null);
            }
        }
    }

    /** 主线前置判定：无前置，或前置任务已交付。 */
    private boolean chainUnlocked(QuestDef def, String roleId) {
        if (def.getPreQuestId() == null || def.getPreQuestId() <= 0) {
            return true;
        }
        QuestProgress pre = findProgress(roleId, def.getPreQuestId(), null);
        return pre != null && pre.getStatus() != null && pre.getStatus() == 2;
    }

    /** 查进度行；每日任务按归属日匹配。 */
    private QuestProgress findProgress(String roleId, int questId, String dayDate) {
        LambdaQueryWrapper<QuestProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestProgress::getRoleId, roleId);
        wrapper.eq(QuestProgress::getQuestId, questId);
        if (dayDate != null) {
            wrapper.eq(QuestProgress::getDayDate, dayDate);
        }
        wrapper.last("LIMIT 1");
        return questProgressService.getOne(wrapper);
    }

    private void createProgress(String roleId, int questId, String dayDate) {
        QuestProgress row = new QuestProgress();
        row.setId(0);
        row.setRoleId(roleId);
        row.setQuestId(questId);
        row.setProgress(0);
        row.setStatus(0);
        row.setDayDate(dayDate == null ? "" : dayDate);
        row.setCreateTime(CommonUtils.nowTime());
        questProgressService.save(row);
    }

    /** 该角色全部进行中（status=0）的进度行。 */
    private List<QuestProgress> activeProgressRows(String roleId) {
        return questProgressService.list(new LambdaQueryWrapper<QuestProgress>()
                .eq(QuestProgress::getRoleId, roleId)
                .eq(QuestProgress::getStatus, 0));
    }

    /** 向包裹发放道具（无记录则新建），含落库。 */
    private void addPackItem(Role role, int basePropId, int num) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, com.gamebird.game.bird.data.ConstData.PACK_TYPE_PROP);
        wrapper.eq(RolePack::getBaseId, basePropId);
        RolePack pack = packService.getOne(wrapper);
        if (pack == null) {
            pack = new RolePack();
            pack.setId(0);
            pack.setRoleId(role.getRoleId());
            pack.setTypeId(com.gamebird.game.bird.data.ConstData.PACK_TYPE_PROP);
            pack.setBaseId(basePropId);
            pack.setNum(0);
        }
        roleValueService.addPackNum(pack, num);
        packService.saveOrUpdate(pack);
    }

    private String today() {
        return LocalDate.now(ZONE).format(DAY);
    }

    /** 任务视图：定义 + 进度 + 锁定标记。 */
    public static class QuestView {
        public QuestDef def;
        public QuestProgress progress;
        public boolean locked;

        public QuestDef getDef() {
            return def;
        }

        public QuestProgress getProgress() {
            return progress;
        }

        public boolean isLocked() {
            return locked;
        }
    }
}
