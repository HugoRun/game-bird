package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.FightLog;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleCage;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.service.FightLogService;
import com.gamebird.game.bird.service.RoleValueService;
import com.gamebird.game.bird.service.TaskTriggerService;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.RoleCageService;
import com.gamebird.game.bird.util.GameUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 战斗（fight）。
 *
 * <p>挑战真实玩家：对手从全服玩家仓库的小鸟中随机抽取（排除自己），胜负由
 * 「出战鸟数值 + 角色等级」综合计算；胜利抢走对方 1% 金币（封顶 1000）并获得经验，
 * 失败无损失；冷却期内不能重复挑战。战斗消耗鸟笼耐久。
 *
 * <p>原先的实现是随机打一只配置表里的小鸟、胜负只看角色等级——数值与成长系统
 * 完全不参与，养鸟没有战斗意义；本版让仓库里养的小鸟真正上场。
 */
@Controller
@RequestMapping("/fight")
public class FightController extends BaseController {

    /** 抢劫金币封顶。 */
    public static final long STEAL_COIN_CAP = 1000;
    /** 出战鸟对战力的权重。 */
    private static final int BIRD_LEVEL_POWER = 20;
    private static final int BIRD_STAR_POWER = 50;

    @Resource
    public FightLogService fightLogService;

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public GameProperties gameProperties;

    @Resource
    public RoleCageService cageService;

    @Resource
    public GameEventService gameEventService;

    @Resource
    public RoleValueService roleValueService;

    @Resource
    public TaskTriggerService taskTriggerService;

    /** 战斗记录列表：当前角色的战斗记录 + 冷却状态。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        LambdaQueryWrapper<FightLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FightLog::getRoleId, role.getRoleId());
        queryWrapper.orderByDesc(FightLog::getFightTime);
        queryWrapper.last("LIMIT 50");
        List<FightLog> logs = fightLogService.list(queryWrapper);
        model.addAttribute("logs", logs);
        int now = CommonUtils.nowTime();
        model.addAttribute("now", now);
        model.addAttribute("cooldownRemain", Math.max(0, cooldownRemain(role.getRoleId(), now)));
        model.addAttribute("cagedBird", getCagedBird(role.getRoleId()));
        return "fight/list";
    }

    /** 发起挑战：抽对手、算战力、结算奖励。 */
    @PostMapping("/challenge")
    public String challenge(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        int now = CommonUtils.nowTime();
        if (cooldownRemain(role.getRoleId(), now) > 0) {
            model.addAttribute("message", "太频繁啦，休息一下再战。");
            return list(session, model);
        }
        // 随机抽一只别人的鸟
        StorageBird opponent = pickOpponentBird(role.getRoleId());
        if (opponent == null) {
            model.addAttribute("message", "暂时找不到可挑战的小鸟，请稍后再试。");
            return list(session, model);
        }
        Role opponentRole = roleService.getById(opponent.getRoleId());
        BaseBird opponentConfig = configGateway.getBaseBird(opponent.getBirdId());
        String opponentName = (opponentConfig == null ? "神秘小鸟" : opponentConfig.getName())
                + "(" + (opponentRole == null ? "神秘玩家" : opponentRole.getRoleName()) + ")";

        // 战力：出战鸟（等级/星级/成长/克制值）+ 角色等级，再乘类型相克倍率
        StorageBird myBird = getCagedBird(role.getRoleId());
        int myPower = (role.getLevel() == null ? 0 : role.getLevel()) * 10;
        Byte myType = null;
        if (myBird != null) {
            myPower += birdPower(myBird);
            BaseBird myConfig = configGateway.getBaseBird(myBird.getBirdId());
            myType = myConfig == null ? null : myConfig.getType();
        }
        int oppPower = birdPower(opponent) + (opponentRole == null || opponentRole.getLevel() == null ? 0 : opponentRole.getLevel() * 10)
                + (opponent.getKo() == null ? 0 : opponent.getKo() / 10);
        double myMultiplier = GameUtils.typeCounterMultiplier(myType, opponentConfig == null ? null : opponentConfig.getType());
        myPower = (int) Math.round((myPower + (myBird == null || myBird.getKo() == null ? 0 : myBird.getKo() / 10)) * myMultiplier);
        // 胜率 = 我方战力占比，限制在 15%~85%
        int winRate = Math.max(15, Math.min(85, myPower * 100 / Math.max(1, myPower + oppPower)));
        boolean win = CommonUtils.randNum(1, 100) <= winRate;
        String counterText = "";
        if (myMultiplier > 1.0) {
            counterText = "你的出战鸟属性克制对方！";
        } else if (myMultiplier < 1.0) {
            counterText = "对方属性克制你的出战鸟，小心！";
        }

        FightLog log = new FightLog();
        log.setRoleId(role.getRoleId());
        log.setOpponentName(opponentName);
        log.setFightTime(now);
        String text;
        if (win) {
            long steal = 0;
            if (opponentRole != null && opponentRole.getCoin() != null && opponentRole.getCoin() > 0) {
                steal = Math.min(STEAL_COIN_CAP, Math.max(1, opponentRole.getCoin() / 100));
                opponentRole.setCoin(opponentRole.getCoin() - steal);
                roleService.updateById(opponentRole);
            }
            long exp = 30 + (opponent.getLevel() == null ? 1 : opponent.getLevel()) * 10;
            addRoleExp(role, exp);
            roleService.updateById(role);
            saveSessionRole(session, role);
            log.setResult(1);
            log.setRewardCoin(steal);
            // 连胜里程碑（含本次，从最近记录倒推连续胜利数）
            int streak = currentWinStreak(role.getRoleId()) + 1;
            long bonus = streak == 3 ? 300 : (streak == 5 ? 800 : (streak == 10 ? 2000 : 0));
            text = counterText + "挑战 " + opponentName + " 胜利！抢到金币 " + steal + "，经验 +" + exp
                    + "。（胜率 " + winRate + "%，已连胜" + streak + "场）";
            if (bonus > 0) {
                roleValueService.addRoleCoin(role, bonus);
                roleService.updateById(role);
                saveSessionRole(session, role);
                text += " 连胜" + streak + "场里程碑，额外奖励金币" + bonus + "！";
                gameEventService.record(role.getRoleId(), "达成了" + streak + "场连胜，额外获得金币" + bonus + "。");
            }
            gameEventService.record(role.getRoleId(), "在战斗中击败了" + opponentName + "，抢到金币" + steal + "。");
            // 被挑战方的战报通知（个人树林记录）
            if (opponentRole != null) {
                gameEventService.record(opponentRole.getRoleId(),
                        role.getRoleName() + " 挑战了你的" + (opponentConfig == null ? "小鸟" : opponentConfig.getName())
                                + "并获胜，被抢走金币 " + steal + "。快去强化出战鸟反击吧！");
            }
            taskTriggerService.onFightWinCount(role, countWins(role.getRoleId()));
            taskTriggerService.onWealth(role);
        } else {
            log.setResult(0);
            log.setRewardCoin(0L);
            text = counterText + "挑战 " + opponentName + " 失败，对方的" + (opponentConfig == null ? "小鸟" : opponentConfig.getName())
                    + "实在凶悍。（胜率 " + winRate + "%，失败无损失）";
            if (opponentRole != null) {
                gameEventService.record(opponentRole.getRoleId(),
                        role.getRoleName() + " 挑战了你的" + (opponentConfig == null ? "小鸟" : opponentConfig.getName())
                                + "，被你轻松击退。");
            }
        }
        fightLogService.save(log);
        // 战斗消耗鸟笼耐久（有装备且有余量时）
        costCageDurability(role.getRoleId());
        model.addAttribute("message", text);
        return list(session, model);
    }

    // ==================== 私有工具 ====================

    /** 小鸟战力：等级×20 + 星级×50 + 成长/10。 */
    private int birdPower(StorageBird bird) {
        return (bird.getLevel() == null ? 1 : bird.getLevel()) * BIRD_LEVEL_POWER
                + (bird.getStar() == null ? 0 : bird.getStar()) * BIRD_STAR_POWER
                + (bird.getGrow() == null ? 0 : bird.getGrow()) / 10;
    }

    /** 随机抽一只别人的鸟（排除自己与免战中的玩家），没有可抽的返回 null。 */
    private StorageBird pickOpponentBird(String myRoleId) {
        java.util.Set<String> shielded = new java.util.HashSet<>(
                gameEventService.recentShieldRoleIds(12 * 3600));
        for (int i = 0; i < 10; i++) {
            StorageBird candidate = storageBirdService.getOne(
                    new LambdaQueryWrapper<StorageBird>()
                            .ne(StorageBird::getRoleId, myRoleId)
                            .last("ORDER BY RAND() LIMIT 1"));
            if (candidate != null && !shielded.contains(candidate.getRoleId())) {
                return candidate;
            }
        }
        return null;
    }

    /** 从最近 10 条记录倒推当前连胜数（不含本次）。 */
    private int currentWinStreak(String roleId) {
        LambdaQueryWrapper<FightLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FightLog::getRoleId, roleId);
        wrapper.orderByDesc(FightLog::getFightTime);
        wrapper.last("LIMIT 10");
        int streak = 0;
        for (FightLog item : fightLogService.list(wrapper)) {
            if (item.getResult() == null || item.getResult() != 1) {
                break;
            }
            streak++;
        }
        return streak;
    }

    /** 累计胜场数（成就用）。 */
    private long countWins(String roleId) {
        return fightLogService.count(new LambdaQueryWrapper<FightLog>()
                .eq(FightLog::getRoleId, roleId)
                .eq(FightLog::getResult, 1));
    }

    /** 战斗冷却剩余秒数（以最后一条战斗记录计）。 */
    private int cooldownRemain(String roleId, int now) {
        LambdaQueryWrapper<FightLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FightLog::getRoleId, roleId);
        wrapper.orderByDesc(FightLog::getFightTime);
        wrapper.last("LIMIT 1");
        FightLog last = fightLogService.getOne(wrapper);
        if (last == null || last.getFightTime() == null) {
            return 0;
        }
        return Math.max(0, gameProperties.getFightCooldownSeconds() - (now - last.getFightTime()));
    }

    /** 出战鸟（鸟笼栏位里登记的那只）。 */
    private StorageBird getCagedBird(String roleId) {
        RoleCage cage = getCageSlot(roleId);
        if (cage == null || cage.getBirdId() == null || cage.getBirdId() <= 0) {
            return null;
        }
        StorageBird bird = storageBirdService.getById(cage.getBirdId());
        return bird != null && roleId.equals(bird.getRoleId()) ? bird : null;
    }

    /** 战斗后扣鸟笼耐久（装备中且有余量）。 */
    private void costCageDurability(String roleId) {
        RoleCage cage = getCageSlot(roleId);
        if (cage != null && cage.getUseCageId() != null && cage.getUseCageId() > 0
                && cage.getUseCageNum0() != null && cage.getUseCageNum0() > 0) {
            cage.setUseCageNum0(cage.getUseCageNum0() - 1);
            cageService.updateById(cage);
        }
    }

    /** 出战栏位（role_cage idx=1），无则返回 null（战斗不强制需要鸟笼）。 */
    private RoleCage getCageSlot(String roleId) {
        LambdaQueryWrapper<RoleCage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleCage::getRoleId, roleId);
        wrapper.eq(RoleCage::getIdx, 1);
        return cageService.getOne(wrapper);
    }
}
