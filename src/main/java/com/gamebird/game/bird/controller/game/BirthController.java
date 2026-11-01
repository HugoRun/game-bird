package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseNest;
import com.gamebird.game.bird.model.dbo.Friend;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleBirth;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.service.FriendService;
import com.gamebird.game.bird.service.TaskTriggerService;
import com.gamebird.game.bird.service.impl.CollectionService;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.impl.QuestService;
import com.gamebird.game.bird.service.RoleBirthService;
import com.gamebird.game.bird.util.GameUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 孵化场 / 配对（role_birth）。
 *
 * <p>完整玩法闭环：包裹装入巢穴 → {@code /birth/start} 选一雄一雌开始配对
 * （自己的两只，或「借用」好友的一只异性鸟跨玩家配对）→ 到期 {@code /birth/collect}
 * 产出一枚爱情结晶（父母种类与星级共同决定）→ 父母解锁回仓库。
 *
 * <p><b>好友配对的状态机</b>（全部由 role_birth 行自身字段承载，无跨行事务）：
 * <ol>
 *   <li>发起方写入自己槽位：bird_id=自己的鸟、match_role_id=好友、match_bird_id=好友的鸟、
 *       birth_end_time=0（待确认）；同时在好友的空闲槽写入对称记录；</li>
 *   <li>好友主页可见邀请，{@code /birth/accept} 双方槽位同时进入孵化；</li>
 *   <li>{@code /birth/decline} 双方清槽解锁。</li>
 * </ol>
 * 此前只有「领取成果」（固定 1000 经验、不产鸟）而没有任何开始配对的入口。
 */
@RequestMapping("/birth")
@Controller
public class BirthController extends BaseController {

    @Resource
    public RoleBirthService roleBirthService;

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public GameProperties gameProperties;

    @Resource
    public FriendService friendService;

    @Resource
    public GameEventService gameEventService;

    @Resource
    public CollectionService collectionService;

    @Resource
    public TaskTriggerService taskTriggerService;

    @Resource
    public QuestService questService;

    /** 孵化场列表。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<RoleBirth> births = listBirthSlots(role);
        model.addAttribute("births", births);
        model.addAttribute("now", CommonUtils.nowTime());
        return "birth/list";
    }

    /** 开始配对页：选自己的两只异性鸟；带 friend 参数时展示好友的鸟。 */
    @GetMapping("/start")
    public String pageStart(HttpSession session, Model model, @RequestParam(defaultValue = "1") int idx,
                            @RequestParam(required = false) String friend) {
        Role role = getSessionRole(session);
        RoleBirth slot = getSlot(role, idx);
        if (slot == null) {
            model.addAttribute("message", "孵化槽位不存在。");
            return list(model, role);
        }
        if (slot.getBirdId() > 0) {
            model.addAttribute("message", "该槽位配对进行中。");
            return list(model, role);
        }
        if (slot.getUseNestId() == null || slot.getUseNestId() <= 0) {
            model.addAttribute("message", "请先到包裹把巢穴放入孵化槽。");
            return list(model, role);
        }
        model.addAttribute("slot", slot);
        // 自己的空闲鸟
        LambdaQueryWrapper<StorageBird> myBirdWrapper = new LambdaQueryWrapper<>();
        myBirdWrapper.eq(StorageBird::getRoleId, role.getRoleId());
        myBirdWrapper.eq(StorageBird::getIsLock, (byte) 0);
        myBirdWrapper.orderByDesc(StorageBird::getWeight);
        List<StorageBird> myBirds = storageBirdService.list(myBirdWrapper);
        model.addAttribute("myBirds", myBirds);

        if (friend != null && !friend.isEmpty()) {
            // 好友的空闲鸟（跨玩家配对）
            Role friendRole = roleService.getById(friend);
            if (friendRole == null) {
                model.addAttribute("message", "好友不存在。");
                return list(model, role);
            }
            LambdaQueryWrapper<StorageBird> friendBirdWrapper = new LambdaQueryWrapper<>();
            friendBirdWrapper.eq(StorageBird::getRoleId, friendRole.getRoleId());
            friendBirdWrapper.eq(StorageBird::getIsLock, (byte) 0);
            friendBirdWrapper.orderByDesc(StorageBird::getWeight);
            model.addAttribute("friendRole", friendRole);
            model.addAttribute("friendBirds", storageBirdService.list(friendBirdWrapper));
        } else {
            // 好友下拉（邀请入口）
            LambdaQueryWrapper<Friend> friendWrapper = new LambdaQueryWrapper<>();
            friendWrapper.eq(Friend::getRoleId, role.getRoleId());
            model.addAttribute("friends", friendService.list(friendWrapper));
        }
        return "birth/start";
    }

    /** 开始配对提交：自家两只（birdIdA+birdIdB），或自家一只+好友一只（friendRoleId+friendBirdId）。 */
    @PostMapping("/start")
    public String start(HttpSession session, Model model, @RequestParam(defaultValue = "1") int idx,
                        @RequestParam(required = false) Integer birdIdA,
                        @RequestParam(required = false) Integer birdIdB,
                        @RequestParam(required = false) String friendRoleId,
                        @RequestParam(required = false) Integer friendBirdId) {
        Role role = getSessionRole(session);
        RoleBirth slot = getSlot(role, idx);
        if (slot == null) {
            model.addAttribute("message", "孵化槽位不存在。");
            return list(model, role);
        }
        if (slot.getBirdId() > 0) {
            model.addAttribute("message", "该槽位配对进行中。");
            return list(model, role);
        }
        if (slot.getUseNestId() == null || slot.getUseNestId() <= 0) {
            model.addAttribute("message", "请先到包裹把巢穴放入孵化槽。");
            return list(model, role);
        }
        StorageBird birdA = birdIdA == null ? null : storageBirdService.getById(birdIdA);
        if (birdA == null || !role.getRoleId().equals(birdA.getRoleId())
                || (birdA.getIsLock() != null && birdA.getIsLock() != 0)) {
            model.addAttribute("message", "请选择一只属于自己的空闲小鸟放在巢中。");
            return list(model, role);
        }

        if (friendRoleId != null && !friendRoleId.isEmpty()) {
            // ---- 好友配对：发起邀请 ----
            StorageBird friendBird = friendBirdId == null ? null : storageBirdService.getById(friendBirdId);
            if (friendBird == null || (friendBird.getIsLock() != null && friendBird.getIsLock() != 0)) {
                model.addAttribute("message", "请选择好友的一只空闲小鸟。");
                return list(model, role);
            }
            if (friendBird.getRoleId().equals(role.getRoleId())) {
                model.addAttribute("message", "这是你自己的小鸟，选它就不需要邀请好友啦。");
                return list(model, role);
            }
            Role friendRole = roleService.getById(friendRoleId);
            if (friendRole == null || !friendRole.getRoleId().equals(friendBird.getRoleId())) {
                model.addAttribute("message", "好友信息有误。");
                return list(model, role);
            }
            if (birdA.getSex().equals(friendBird.getSex())) {
                model.addAttribute("message", "两只小鸟的性别相同，孵不出蛋哦。");
                return list(model, role);
            }
            // 好友需要有一个空闲孵化槽（没有则用默认巢自动开一个新槽）
            RoleBirth friendSlot = findFreeBirthSlot(friendRole.getRoleId());
            if (friendSlot == null) {
                model.addAttribute("message", friendRole.getRoleName() + " 的孵化场全被占用了，稍后再试。");
                return list(model, role);
            }
            int now = CommonUtils.nowTime();
            // 发起方槽位：待对方确认（birth_end_time=0）
            slot.setBirdId(birdA.getId());
            slot.setBirdBaseId(birdA.getBirdId());
            slot.setMatchRoleId(intRoleId(friendRole.getRoleId()));
            slot.setMatchRoleName(friendRole.getRoleName());
            slot.setMatchBirdId(friendBird.getId());
            slot.setMatchBirdBaseId(friendBird.getBirdId());
            slot.setBirthTime(now);
            slot.setBirthEndTime(0);
            roleBirthService.updateById(slot);
            // 好友槽位：对称记录，待好友确认
            friendSlot.setBirdId(friendBird.getId());
            friendSlot.setBirdBaseId(friendBird.getBirdId());
            friendSlot.setMatchRoleId(intRoleId(role.getRoleId()));
            friendSlot.setMatchRoleName(role.getRoleName());
            friendSlot.setMatchBirdId(birdA.getId());
            friendSlot.setMatchBirdBaseId(birdA.getBirdId());
            friendSlot.setBirthTime(now);
            friendSlot.setBirthEndTime(0);
            if (friendSlot.getUseNestId() == null || friendSlot.getUseNestId() <= 0) {
                BaseNest defaultNest = configGateway.getBaseNest(ConstData.DEFAULT_NEST_ID);
                friendSlot.setUseNestId(ConstData.DEFAULT_NEST_ID);
                friendSlot.setUseNestNum(defaultNest == null ? 0 : defaultNest.getNum());
            }
            roleBirthService.updateById(friendSlot);
            // 锁定两只鸟
            lockBird(birdA, true);
            lockBird(friendBird, true);
            gameEventService.record(role.getRoleId(),
                    "向 " + friendRole.getRoleName() + " 的小鸟发起了配对邀请。");
            model.addAttribute("message", "已向 " + friendRole.getRoleName() + " 发出配对邀请，对方确认后开始孵化。");
            return list(model, role);
        }

        // ---- 自家配对 ----
        StorageBird birdB = birdIdB == null ? null : storageBirdService.getById(birdIdB);
        if (birdB == null || !role.getRoleId().equals(birdB.getRoleId())
                || (birdB.getIsLock() != null && birdB.getIsLock() != 0)) {
            model.addAttribute("message", "请再选一只属于自己的空闲小鸟。");
            return list(model, role);
        }
        if (birdA.getSex().equals(birdB.getSex())) {
            model.addAttribute("message", "两只小鸟的性别相同，孵不出蛋哦。");
            return list(model, role);
        }
        int now = CommonUtils.nowTime();
        int duration = birthDuration(slot, role);
        slot.setBirdId(birdA.getId());
        slot.setBirdBaseId(birdA.getBirdId());
        slot.setMatchRoleId(0);
        slot.setMatchRoleName("");
        slot.setMatchBirdId(birdB.getId());
        slot.setMatchBirdBaseId(birdB.getBirdId());
        slot.setBirthTime(now);
        slot.setBirthEndTime(now + duration);
        roleBirthService.updateById(slot);
        lockBird(birdA, true);
        lockBird(birdB, true);
        BaseBird nameBird = configGateway.getBaseBird(birdA.getBirdId());
        gameEventService.record(role.getRoleId(),
                "把两只小鸟送进了爱的巢穴，" + (nameBird == null ? "" : nameBird.getName()) + "的蜜月开始了。");
        model.addAttribute("message", "配对开始！" + duration / 3600 + "小时后来看爱情结晶。");
        return list(model, role);
    }

    /** 好友确认配对邀请：双方槽位同时进入孵化。 */
    @PostMapping("/accept")
    public String accept(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleBirth mySlot = roleBirthService.getById(id);
        if (mySlot == null || !role.getRoleId().equals(mySlot.getRoleId())) {
            model.addAttribute("message", "孵化槽位不存在或不属于当前角色。");
            return list(model, role);
        }
        if (mySlot.getBirdId() == null || mySlot.getBirdId() <= 0
                || mySlot.getMatchBirdId() == null || mySlot.getMatchBirdId() <= 0) {
            model.addAttribute("message", "没有待确认的配对邀请。");
            return list(model, role);
        }
        if (mySlot.getBirthEndTime() != null && mySlot.getBirthEndTime() > 0) {
            model.addAttribute("message", "该配对已经开始孵化。");
            return list(model, role);
        }
        // 通过「对方槽位记录的我方鸟 id」反查发起方槽位（match_role_id 是 int 列存不下 UUID，不可用于定位）
        RoleBirth initiatorSlot = findInitiatorSlot(mySlot.getBirdId());
        if (initiatorSlot == null) {
            // 发起方已取消：清掉自己的记录
            unlockAndClear(mySlot);
            model.addAttribute("message", "对方已取消邀请。");
            return list(model, role);
        }
        int now = CommonUtils.nowTime();
        int myDuration = birthDuration(mySlot, role);
        mySlot.setBirthEndTime(now + myDuration);
        roleBirthService.updateById(mySlot);
        // 发起方按发起者的婚姻加成与巢穴计算时长
        Role initiator = roleService.getById(initiatorSlot.getRoleId());
        int initiatorDuration = initiator == null ? myDuration : birthDuration(initiatorSlot, initiator);
        initiatorSlot.setBirthEndTime(now + initiatorDuration);
        roleBirthService.updateById(initiatorSlot);
        gameEventService.record(role.getRoleId(),
                "接受了 " + (initiator == null ? "好友" : initiator.getRoleName()) + " 的配对邀请，爱的巢穴热闹起来了。");
        model.addAttribute("message", "配对成功，孵化开始！约" + myDuration / 3600 + "小时后收获爱情结晶。");
        return list(model, role);
    }

    /** 拒绝（或发起方撤回）配对邀请：双方清槽解锁。 */
    @PostMapping("/decline")
    public String decline(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleBirth mySlot = roleBirthService.getById(id);
        if (mySlot == null || !role.getRoleId().equals(mySlot.getRoleId())) {
            model.addAttribute("message", "孵化槽位不存在或不属于当前角色。");
            return list(model, role);
        }
        if (mySlot.getBirthEndTime() != null && mySlot.getBirthEndTime() > 0) {
            model.addAttribute("message", "孵化已开始，无法取消。");
            return list(model, role);
        }
        // 清发起方槽位（存在待确认的邀请时）
        if (mySlot.getBirdId() != null && mySlot.getBirdId() > 0
                && mySlot.getMatchBirdId() != null && mySlot.getMatchBirdId() > 0) {
            RoleBirth initiatorSlot = findInitiatorSlot(mySlot.getBirdId());
            if (initiatorSlot != null) {
                unlockAndClear(initiatorSlot);
            }
        }
        unlockAndClear(mySlot);
        model.addAttribute("message", "配对已取消。");
        return list(model, role);
    }

    /** 收获孵化成果：产出一枚爱情结晶，父母解锁，扣巢穴耐久。 */
    @PostMapping("/collect")
    public String collect(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleBirth birth = roleBirthService.getById(id);
        if (null == birth || !role.getRoleId().equals(birth.getRoleId())) {
            model.addAttribute("message", "孵化槽位不存在或不属于当前角色。");
            return list(model, role);
        }
        int now = CommonUtils.nowTime();
        if (birth.getBirdId() == null || birth.getBirdId() <= 0) {
            model.addAttribute("message", "该槽位没有在孵化。");
            return list(model, role);
        }
        if (birth.getBirthEndTime() == null || birth.getBirthEndTime() <= 0) {
            model.addAttribute("message", "孵化尚未开始，等待对方确认。");
            return list(model, role);
        }
        if (birth.getBirthEndTime() > now) {
            model.addAttribute("message", "孵化尚未完成，请耐心等待。");
            return list(model, role);
        }
        StorageBird parentA = storageBirdService.getById(birth.getBirdId());
        StorageBird parentB = birth.getMatchBirdId() == null ? null : storageBirdService.getById(birth.getMatchBirdId());
        if (parentA == null || parentB == null) {
            // 父母数据异常也要清槽
            unlockAndClear(birth);
            model.addAttribute("message", "配对数据异常，槽位已重置。");
            return list(model, role);
        }
        // 仓库容量
        long owned = storageBirdService.count(new LambdaQueryWrapper<StorageBird>()
                .eq(StorageBird::getRoleId, role.getRoleId()));
        if (owned >= gameProperties.getBirdStorageCapacity()) {
            model.addAttribute("message", "小鸟仓库已满，请先出售几只再来收获。");
            return list(model, role);
        }
        // 产出规则
        BaseBird configA = configGateway.getBaseBird(parentA.getBirdId());
        BaseBird configB = configGateway.getBaseBird(parentB.getBirdId());
        int childBaseId = pickChildBaseId(parentA, parentB);
        BaseBird childConfig = configGateway.getBaseBird(childBaseId);
        if (childConfig == null) {
            unlockAndClear(birth);
            model.addAttribute("message", "小鸟配置错误，槽位已重置。");
            return list(model, role);
        }
        // 星级 = 父母均值 ±1
        int starAvg = ((parentA.getStar() == null ? 0 : parentA.getStar())
                + (parentB.getStar() == null ? 0 : parentB.getStar())) / 2;
        byte star = (byte) Math.max(0, Math.min(5, starAvg + CommonUtils.randNum(-1, 1)));
        // 体重 = 父母均值 ±25%，再叠加巢穴/婚姻的重量加成
        long avgWeight = (parentA.getWeight() + parentB.getWeight()) / 2;
        long weight = avgWeight * (100 - ConstData.BIRTH_WEIGHT_FLOAT_PERCENT) / 100
                + CommonUtils.randNum(0, (int) (avgWeight * ConstData.BIRTH_WEIGHT_FLOAT_PERCENT / 50));
        int weightBonus = nestEffectValue(birth, 2) + marriageWeightBonus(role);
        if (weightBonus != 0) {
            weight = weight + weight * Math.min(50, Math.max(-50, weightBonus)) / 100;
        }
        StorageBird child = new StorageBird();
        child.setId(0);
        child.setRoleId(role.getRoleId());
        child.setBirdId(childBaseId);
        child.setLevel((short) 1);
        child.setExp(nestEffectValue(birth, 1) > 0 ? (long) nestEffectValue(birth, 1) : 0L);
        child.setTotalExp(nestEffectValue(birth, 1) > 0 ? (long) nestEffectValue(birth, 1) : 0L);
        child.setSex((byte) (CommonUtils.randNum(1, 100) <= 50 ? 1 : 2));
        child.setWeight(Math.max(childConfig.getMinWeight(), weight));
        child.setInitWeight(Math.max(childConfig.getMinWeight(), weight));
        child.setStar(star);
        child.setGrow(CommonUtils.randNum(ConstData.GROW_MIN, ConstData.GROW_MAX).shortValue());
        child.setKo(CommonUtils.randNum(0, ConstData.KO_MAX).shortValue());
        child.setIsLock((byte) 0);
        child.setTime(now);
        storageBirdService.save(child);
        role.setBirdNum((short) Math.max(0, (role.getBirdNum() == null ? 0 : role.getBirdNum()) + 1));
        roleService.updateById(role);
        // 图鉴与成就：首次收集新物种 / 首只 5 星结晶
        collectionService.record(role.getRoleId(), childBaseId);
        taskTriggerService.onCollect(role, collectionService.countCollected(role.getRoleId()));
        questService.onEvent(role, QuestService.EV_BIRTH, 1);
        if (star >= 5) {
            taskTriggerService.onFiveStarBirth(role);
        }
        // 父母解锁 + 槽位清空 + 巢穴耐久
        lockBird(parentA, false);
        lockBird(parentB, false);
        if (birth.getUseNestId() != null && birth.getUseNestId() > 0 && birth.getUseNestNum() != null && birth.getUseNestNum() > 0) {
            birth.setUseNestNum(birth.getUseNestNum() - 1);
        }
        birth.setBirdId(0);
        birth.setBirdBaseId(0);
        birth.setMatchRoleId(0);
        birth.setMatchRoleName("");
        birth.setMatchBirdId(0);
        birth.setMatchBirdBaseId(0);
        birth.setBirthTime(0);
        birth.setBirthEndTime(0);
        roleBirthService.updateById(birth);
        gameEventService.record(role.getRoleId(),
                "爱的结晶诞生了：" + child.getWeight() / 100.0 + "斤的"
                        + GameUtils.getBirdSexName(child.getSex()) + childConfig.getName()
                        + "(" + GameUtils.getStarName(star) + ")。");
        model.addAttribute("message", "恭喜！爱情结晶诞生：" + child.getWeight() / 100.0 + "斤的"
                + GameUtils.getBirdSexName(child.getSex()) + childConfig.getName()
                + "，" + GameUtils.getStarName(star) + "。");
        return list(model, role);
    }

    // ==================== 私有工具 ====================

    /**
     * 孵化时长：基础时长 × 巢穴时间加成（effect_type=3）× 婚姻时间减免。
     * 昆仑府 effect_value=-15 表示减 15%。
     */
    private int birthDuration(RoleBirth slot, Role role) {
        int duration = ConstData.BIRTH_DURATION_SECONDS;
        BaseNest nest = slot.getUseNestId() == null ? null : configGateway.getBaseNest(slot.getUseNestId());
        if (nest != null && nest.getEffectType() != null && nest.getEffectType() == 3
                && nest.getEffectValue() != null && nest.getEffectValue() != 0) {
            int cut = Math.min(90, Math.abs(nest.getEffectValue()));
            duration = duration * (100 - cut) / 100;
        }
        int marriageCut = marriageBonusPercent(role);
        if (marriageCut > 0) {
            duration = duration * (100 - marriageCut) / 100;
        }
        return Math.max(60, duration);
    }

    /** 婚姻加成百分比（重量+X% 与时间-X% 同值）。 */
    private int marriageBonusPercent(Role role) {
        if (role.getMarriageType() == null) {
            return 0;
        }
        return switch (role.getMarriageType()) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 8;
            case 4 -> 20;
            default -> 0;
        };
    }

    private int marriageWeightBonus(Role role) {
        return marriageBonusPercent(role);
    }

    /** 取巢穴上指定效果类型（1 经验 / 2 重量）的 |effect_value|，没有则 0。 */
    private int nestEffectValue(RoleBirth slot, int effectType) {
        BaseNest nest = slot.getUseNestId() == null ? null : configGateway.getBaseNest(slot.getUseNestId());
        if (nest == null || nest.getEffectType() == null || nest.getEffectValue() == null) {
            return 0;
        }
        return nest.getEffectType() == effectType ? Math.abs(nest.getEffectValue()) : 0;
    }

    /** 孩子的种类：同种随父母；异种取配置 id 较小的一方（低阶）。 */
    private int pickChildBaseId(StorageBird parentA, StorageBird parentB) {
        if (parentA.getBirdId().equals(parentB.getBirdId())) {
            return parentA.getBirdId();
        }
        return Math.min(parentA.getBirdId(), parentB.getBirdId());
    }

    /** 锁定/解锁小鸟。 */
    private void lockBird(StorageBird bird, boolean lock) {
        bird.setIsLock((byte) (lock ? 1 : 0));
        storageBirdService.updateById(bird);
    }

    /** 清空槽位并解锁槽内与小鸟。 */
    private void unlockAndClear(RoleBirth slot) {
        if (slot.getBirdId() != null && slot.getBirdId() > 0) {
            StorageBird bird = storageBirdService.getById(slot.getBirdId());
            if (bird != null) {
                lockBird(bird, false);
            }
        }
        if (slot.getMatchBirdId() != null && slot.getMatchBirdId() > 0) {
            StorageBird match = storageBirdService.getById(slot.getMatchBirdId());
            if (match != null) {
                lockBird(match, false);
            }
        }
        slot.setBirdId(0);
        slot.setBirdBaseId(0);
        slot.setMatchRoleId(0);
        slot.setMatchRoleName("");
        slot.setMatchBirdId(0);
        slot.setMatchBirdBaseId(0);
        slot.setBirthTime(0);
        slot.setBirthEndTime(0);
        roleBirthService.updateById(slot);
    }

    /** 找角色的一个空闲孵化槽（无鸟且无待确认邀请）。 */
    private RoleBirth findFreeBirthSlot(String roleId) {
        LambdaQueryWrapper<RoleBirth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleBirth::getRoleId, roleId);
        wrapper.orderByAsc(RoleBirth::getIdx);
        for (RoleBirth slot : roleBirthService.list(wrapper)) {
            if ((slot.getBirdId() == null || slot.getBirdId() <= 0)
                    && (slot.getBirthEndTime() == null || slot.getBirthEndTime() <= 0)) {
                return slot;
            }
        }
        // 好友没有任何槽位记录（极端情况）时补建一个
        RoleBirth created = new RoleBirth();
        created.setId(0);
        created.setRoleId(roleId);
        created.setIdx(1);
        roleBirthService.save(created);
        return created;
    }

    /**
     * 找发起方的待确认槽位：发起方在 match_bird_id 里记录了「被邀请方的鸟 id」，
     * 即我槽位里的 bird_id，据此精确定位（match_role_id 是 int 列，存不下 UUID 角色 id）。
     */
    private RoleBirth findInitiatorSlot(Integer myBirdId) {
        LambdaQueryWrapper<RoleBirth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleBirth::getMatchBirdId, myBirdId);
        wrapper.eq(RoleBirth::getBirthEndTime, 0);
        for (RoleBirth slot : roleBirthService.list(wrapper)) {
            if (slot.getBirdId() != null && slot.getBirdId() > 0) {
                return slot;
            }
        }
        return null;
    }

    /**
     * 角色 id 的 int 压缩表示，仅作信息展示与粗筛。
     * role_birth.match_role_id 是 int 列（历史 schema），UUID 角色 id 存不下，
     * 因此跨玩家定位一律走 match_bird_id，本值不参与精确匹配。
     */
    private int intRoleId(String roleId) {
        return roleId == null ? 0 : Math.abs(roleId.hashCode());
    }

    /** 角色全部孵化槽。 */
    private List<RoleBirth> listBirthSlots(Role role) {
        LambdaQueryWrapper<RoleBirth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleBirth::getRoleId, role.getRoleId());
        wrapper.orderByAsc(RoleBirth::getIdx);
        return roleBirthService.list(wrapper);
    }

    /** 按槽位序号取孵化槽。 */
    private RoleBirth getSlot(Role role, int idx) {
        LambdaQueryWrapper<RoleBirth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleBirth::getRoleId, role.getRoleId());
        wrapper.eq(RoleBirth::getIdx, idx);
        return roleBirthService.getOne(wrapper);
    }

    /** 渲染列表页并带提示。 */
    private String list(Model model, Role role) {
        model.addAttribute("births", listBirthSlots(role));
        model.addAttribute("now", CommonUtils.nowTime());
        return "birth/list";
    }
}
