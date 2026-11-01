package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.entity.BizException;
import com.gamebird.common.entity.ResultCode;
import com.gamebird.common.util.CommonUtils;

import java.util.List;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseBait;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseCage;
import com.gamebird.game.bird.model.dbo.BaseField;
import com.gamebird.game.bird.model.dbo.BaseLevel;
import com.gamebird.game.bird.model.dbo.BaseProp;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleCage;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.model.vo.TrapResultVO;
import com.gamebird.game.bird.service.ActivityService;
import com.gamebird.game.bird.service.GuildMemberService;
import com.gamebird.game.bird.service.GuildService;
import com.gamebird.game.bird.service.BirdRaffleService;
import com.gamebird.game.bird.service.impl.CollectionService;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.RoleCageService;
import com.gamebird.game.bird.service.RolePackService;
import com.gamebird.game.bird.service.RoleService;
import com.gamebird.game.bird.service.RoleTrapService;
import com.gamebird.game.bird.service.RoleValueService;
import com.gamebird.game.bird.service.StorageBirdService;
import com.gamebird.game.bird.service.TaskTriggerService;
import com.gamebird.game.bird.service.TrapService;
import com.gamebird.game.bird.util.BirdTextUtils;
import com.gamebird.game.bird.util.GameUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 捕捉循环的核心业务：下饵与收网。
 *
 * <p>原先这段 100 余行的逻辑内联在 {@code TrapController}，且存在多处半成品：
 * 成长值/克制值写死、陷阱耐久从不扣减、仓库没有上限、收网用 GET。本类补全玩法并
 * 作为批次重构的落点 —— Controller 只做参数接收与消息渲染，规则全部收口在这里。
 *
 * <p><b>逃脱机制</b>：星越高的鸟越容易挣脱（每星 {@code ConstData.ESCAPE_PERCENT_PER_STAR}%）。
 * 下饵时即掷骰，若判定逃脱，把 {@code catch_bird_star} 存为 {@code -(star+1)}
 * 作为「将逃脱」标记（role_trap 无空闲列可用，负值标记是不改表结构的约定）；
 * 收网时读到负星即结算为逃脱，给一半经验作为安慰奖。粘性道具
 * （base_prop effect_type=1）可在下饵时勾选消耗，把逃脱概率削去一半。
 */
@Service
public class TrapServiceImpl implements TrapService {

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public GameProperties gameProperties;

    @Resource
    public RolePackService packService;

    @Resource
    public RoleTrapService trapService;

    @Resource
    public StorageBirdService storageBirdService;

    @Resource
    public RoleValueService roleValueService;

    @Resource
    public RoleCageService cageService;

    @Resource
    public RoleService roleService;

    @Resource
    public BirdRaffleService birdRaffleService;

    @Resource
    public GameEventService gameEventService;

    @Resource
    public TaskTriggerService taskTriggerService;

    @Resource
    public CollectionService collectionService;

    @Resource
    public QuestService questService;

    @Resource
    public ActivityService activityService;

    @Resource
    public GuildMemberService guildMemberService;

    @Resource
    public GuildService guildService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useBait(Role role, int trapId, int baitPackId, boolean useSticky) {
        RoleTrap slot = getOwnedSlot(role, trapId);
        if (slot.getBaitId() > 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "当前陷阱正在捕鸟中。");
        }
        if (slot.getUseTrapId() > 0 && slot.getUseTrapNum() == 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "陷阱已损坏，请到包裹更换新陷阱。");
        }
        RolePack packBait = baitPackId <= 0 ? null : packService.getById(baitPackId);
        if (packBait == null || !role.getRoleId().equals(packBait.getRoleId())
                || packBait.getNum() == null || packBait.getNum() <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "诱饵数量不足");
        }
        BaseBait baseBait = configGateway.getBaseBait(packBait.getBaseId());
        if (baseBait == null || baseBait.getBirdSet() == null || baseBait.getBirdSet().trim().isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "诱饵配置错误");
        }
        // 区域限制生效：lb 指定该饵料只能在一个场景使用（base_bait 数据约定 1/3/5/7/9）
        if (baseBait.getLb() != null && baseBait.getLb() > 0
                && role.getFieldId() != null
                && baseBait.getLb().intValue() != role.getFieldId().intValue()) {
            BaseField field = configGateway.getBaseField(baseBait.getLb().intValue());
            throw new BizException(ResultCode.PARAM_INVALID,
                    "「" + baseBait.getName() + "」只能在【" + (field == null ? "指定场景" : field.getName()) + "】使用，请先切换场景。");
        }
        BaseBird baseBird = birdRaffleService.randBird(baseBait.getBirdSet().split(","));
        if (baseBird == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "诱饵或者小鸟配置错误");
        }
        int now = CommonUtils.nowTime();
        boolean activityLucky = false;
        // 限时活动：活动期间 5% 概率惊现活动限定鸟（base_bird 93-111 中在售的）
        if (hasLiveActivity(now) && CommonUtils.randNum(1, 100) <= 5) {
            List<BaseBird> pool = configGateway.getBaseBirdList().stream()
                    .filter(b -> b.getId() != null && b.getId() >= 93 && b.getId() <= 111
                            && b.getStatus() != null && b.getStatus() == 1)
                    .toList();
            if (!pool.isEmpty()) {
                baseBird = pool.get(CommonUtils.randNum(0, pool.size() - 1));
                activityLucky = true;
            }
        }
        // 随机重量与星级
        long weight = CommonUtils.randNum(baseBird.getMinWeight(), baseBird.getMaxWeight());
        byte star = (byte) (weight / (baseBird.getMaxWeight() / 5));
        // 逃脱判定：星越高越容易跑；粘性道具把概率削半并消耗一张
        int escapePercent = star * ConstData.ESCAPE_PERCENT_PER_STAR;
        String stickyName = null;
        if (useSticky) {
            RolePack sticky = findStickyProp(role);
            if (sticky != null) {
                BaseProp stickyProp = configGateway.getBaseProp(sticky.getBaseId());
                stickyName = stickyProp == null ? "粘性道具" : stickyProp.getName();
                escapePercent = escapePercent * (100 - ConstData.ESCAPE_REDUCE_PERCENT) / 100;
                roleValueService.costPackNum(sticky, 1);
                packService.saveOrUpdate(sticky);
            }
        }
        boolean escaped = CommonUtils.randNum(1, 100) <= escapePercent;
        // 写捕捉状态；逃脱约定：星存负值 -(star+1)
        slot.setBaitId(baitPackId);
        slot.setCatchBirdId(baseBird.getId());
        slot.setCatchBirdSex((byte) (CommonUtils.randNum(1, 100) <= 50 ? 1 : 2));
        slot.setCatchBirdWeight(weight);
        slot.setCatchBirdStar(escaped ? (byte) (-(star + 1)) : star);
        slot.setCatchTime(now);
        slot.setCatchEndTime(now + baseBird.getCatchMin() * 60);
        trapService.updateById(slot);
        // 扣诱饵
        roleValueService.costPackNum(packBait, 1);
        packService.saveOrUpdate(packBait);
        if (activityLucky) {
            gameEventService.record(role.getRoleId(),
                    "活动中好运降临，陷阱里出现了一只稀有的" + baseBird.getName() + "！");
        }
    }

    /** 当前是否有进行中的活动（activity.status=1 且在时间窗内）。 */
    private boolean hasLiveActivity(int now) {
        return activityService.count(new LambdaQueryWrapper<com.gamebird.game.bird.model.dbo.Activity>()
                .eq(com.gamebird.game.bird.model.dbo.Activity::getStatus, 1)
                .le(com.gamebird.game.bird.model.dbo.Activity::getStartTime, now)
                .ge(com.gamebird.game.bird.model.dbo.Activity::getEndTime, now)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrapResultVO over(Role role, int trapId) {
        RoleTrap slot = getOwnedSlot(role, trapId);
        if (slot.getBaitId() == 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "当前陷阱正处于空闲中。");
        }
        BaseBird baseBird = configGateway.getBaseBird(slot.getCatchBirdId());
        if (baseBird == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "小鸟配置错误，无法结算。");
        }
        boolean escaped = slot.getCatchBirdStar() < 0;
        byte star = escaped ? (byte) (-(slot.getCatchBirdStar() + 1)) : slot.getCatchBirdStar();
        int oldLevel = role.getLevel();
        long addExp;
        TrapResultVO resultVO = new TrapResultVO();
        resultVO.setBirdBaseId(baseBird.getId());
        resultVO.setMinWeight(baseBird.getMinWeight());
        resultVO.setMaxWeight(baseBird.getMaxWeight());
        resultVO.setWishNum(0);
        resultVO.setEscaped(escaped);

        if (escaped) {
            // 逃脱：无鸟，一半经验安慰奖
            addExp = Math.max(1, baseBird.getCatchExp() / 2);
            resultVO.setBirdId(0);
            resultVO.setNameText(GameUtils.getBirdSexName(slot.getCatchBirdSex()) + baseBird.getName());
            resultVO.setStarText(GameUtils.getStarName(star));
            resultVO.setGrowText("-");
            resultVO.setKoText("-");
            gameEventService.record(role.getRoleId(),
                    "到手的" + baseBird.getName() + "挣脱陷阱飞走了，好在捞回了一些经验。");
        } else {
            // 仓库容量校验：满了拒绝收网并保留捕捉状态，玩家出售后可再来收
            long owned = storageBirdService.count(
                    new LambdaQueryWrapper<StorageBird>().eq(StorageBird::getRoleId, role.getRoleId()));
            if (owned >= gameProperties.getBirdStorageCapacity()) {
                // 仓库满：给出明确去向（此前玩家点收网只见报错、状态不变，体感"按钮无响应"）
                throw new BizException(ResultCode.PARAM_INVALID,
                        "小鸟仓库已满（" + owned + "/" + gameProperties.getBirdStorageCapacity()
                                + "只）。请先到仓库出售或放生几只，再回来收网。");
            }
            // 鸟笼加成：经验笼加经验、重量笼加重量（按 func 文案区分）
            long weight = slot.getCatchBirdWeight();
            CageBonus bonus = getCageBonus(role.getRoleId());
            if (bonus.percent() > 0 && bonus.weightBased()) {
                weight = weight + weight * bonus.percent() / 100;
            }
            addExp = baseBird.getCatchExp();
            if (bonus.percent() > 0 && !bonus.weightBased()) {
                addExp = addExp + addExp * bonus.percent() / 100;
                resultVO.setCageText("「" + bonus.cageName() + "」经验加成+" + bonus.percent() + "%");
            } else if (bonus.percent() > 0) {
                resultVO.setCageText("「" + bonus.cageName() + "」重量加成+" + bonus.percent() + "%");
            }
            // 公会加成：收网经验 +1%×公会等级（上限 10%）
            int guildPercent = getGuildExpBonus(role.getRoleId());
            if (guildPercent > 0) {
                addExp = addExp + addExp * guildPercent / 100;
                resultVO.setGuildText("公会庇佑，经验加成+" + guildPercent + "%");
            }
            // 主场捕捉加成：在鸟的归属场景（base_bird.field）收网，经验 +10%
            if (baseBird.getField() != null && role.getFieldId() != null
                    && baseBird.getField().intValue() == role.getFieldId().intValue()) {
                addExp = addExp + addExp / 10;
                resultVO.setHomeText("主场捕捉，经验加成+10%");
            }
            // 产出小鸟：成长/克制随机（此前写死 1000/0 的 todo 就此关闭）
            StorageBird bird = new StorageBird();
            bird.setId(0);
            bird.setRoleId(role.getRoleId());
            bird.setBirdId(baseBird.getId());
            bird.setLevel((short) 1);
            bird.setExp(0L);
            bird.setTotalExp(0L);
            bird.setSex(slot.getCatchBirdSex());
            bird.setWeight(weight);
            bird.setInitWeight(slot.getCatchBirdWeight());
            bird.setStar(star);
            bird.setGrow(CommonUtils.randNum(ConstData.GROW_MIN, ConstData.GROW_MAX).shortValue());
            bird.setKo(CommonUtils.randNum(0, ConstData.KO_MAX).shortValue());
            bird.setIsLock((byte) 0);
            bird.setTime(CommonUtils.nowTime());
            storageBirdService.save(bird);
            resultVO.setBirdId(bird.getId());
            resultVO.setNameText(weight / 100.0 + "斤的" + GameUtils.getBirdSexName(bird.getSex()) + baseBird.getName());
            resultVO.setStarText(GameUtils.getStarName(star));
            resultVO.setGrowText(BirdTextUtils.growText(bird.getGrow()));
            resultVO.setKoText(BirdTextUtils.koText(bird.getKo()));
            // 角色持有鸟数 +1（排行榜维度）
            role.setBirdNum((short) Math.max(0, (role.getBirdNum() == null ? 0 : role.getBirdNum()) + 1));
            // 图鉴收集（首次遇到该物种时记录）
            if (collectionService.record(role.getRoleId(), baseBird.getId())) {
                resultVO.setBookText("图鉴新收录：" + baseBird.getName() + "！");
            }
            // 事件日志与任务推进
            gameEventService.record(role.getRoleId(),
                    "收获了" + resultVO.getNameText() + "，经验+" + addExp);
            taskTriggerService.onCatch(role);
            taskTriggerService.onLevelUp(role);
            taskTriggerService.onWealth(role);
            // 奋进目标：捕捉 +1，图鉴/等级快照刷新
            questService.onEvent(role, QuestService.EV_CATCH, 1);
            questService.onEvent(role, QuestService.EV_BOOK, 0);
            questService.onEvent(role, QuestService.EV_LEVEL, 0);
        }
        // 消耗陷阱耐久（默认竹筐 use_trap_id=0 不扣）
        if (slot.getUseTrapId() > 0 && slot.getUseTrapNum() > 0) {
            slot.setUseTrapNum(slot.getUseTrapNum() - 1);
        }
        // 清空捕捉状态
        slot.setBaitId(0);
        slot.setCatchBirdId(0);
        slot.setCatchBirdSex((byte) 0);
        slot.setCatchBirdWeight(0L);
        slot.setCatchBirdStar((byte) 0);
        slot.setCatchTime(0);
        slot.setCatchEndTime(0);
        trapService.updateById(slot);
        // 角色经验（含落库）
        roleValueService.addRoleExp(role, addExp);
        roleService.updateById(role);
        resultVO.setAddExp(addExp);
        BaseLevel baseLevel = configGateway.getBaseLevel(role.getLevel() + 1);
        resultVO.setNeedExp(baseLevel == null ? 0 : baseLevel.getTotalExp() - role.getTotalExp());
        resultVO.setOldLevel(oldLevel);
        resultVO.setNewLevel(role.getLevel());
        return resultVO;
    }

    /** 角色所在公会的经验加成百分比（1%×公会等级，上限 10）；无公会为 0。 */
    private int getGuildExpBonus(String roleId) {
        com.gamebird.game.bird.model.dbo.GuildMember member = guildMemberService.getOne(
                new LambdaQueryWrapper<com.gamebird.game.bird.model.dbo.GuildMember>()
                        .eq(com.gamebird.game.bird.model.dbo.GuildMember::getRoleId, roleId)
                        .last("LIMIT 1"));
        if (member == null || member.getGuildId() == null) {
            return 0;
        }
        com.gamebird.game.bird.model.dbo.Guild guild = guildService.getById(member.getGuildId());
        if (guild == null || guild.getLevel() == null || guild.getLevel() <= 0) {
            return 0;
        }
        return Math.min(10, guild.getLevel());
    }

    /** 计算出战鸟笼的加成：返回 (百分比, 是否重量笼, 鸟笼名)。 */
    private CageBonus getCageBonus(String roleId) {
        LambdaQueryWrapper<RoleCage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleCage::getRoleId, roleId);
        wrapper.eq(RoleCage::getIdx, 1);
        RoleCage cage = cageService.getOne(wrapper);
        if (cage == null || cage.getUseCageId() == null || cage.getUseCageId() <= 0
                || cage.getUseCageNum0() == null || cage.getUseCageNum0() <= 0) {
            // 耐久耗尽的鸟笼不再提供加成（修复：原先 0 耐久仍生效）
            return new CageBonus(0, false, "");
        }
        BaseCage baseCage = configGateway.getBaseCage(cage.getUseCageId());
        if (baseCage == null || baseCage.getParam() == null || baseCage.getParam() <= 0) {
            return new CageBonus(0, false, "");
        }
        String func = baseCage.getFunc() == null ? "" : baseCage.getFunc();
        return new CageBonus(baseCage.getParam(), func.contains("重量"), baseCage.getName());
    }

    /** 鸟笼加成快照。 */
    private record CageBonus(int percent, boolean weightBased, String cageName) {
    }

    /** 取属于当前角色的陷阱槽，找不到即业务失败。 */
    private RoleTrap getOwnedSlot(Role role, int trapId) {
        RoleTrap slot = trapId <= 0 ? null : trapService.getById(trapId);
        if (slot == null || !role.getRoleId().equals(slot.getRoleId())) {
            throw new BizException(ResultCode.PARAM_INVALID, "没有该陷阱");
        }
        return slot;
    }

    /** 找包裹中一种粘性道具（effect_type=1）。 */
    private RolePack findStickyProp(Role role) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        wrapper.gt(RolePack::getNum, 0);
        for (RolePack pack : packService.list(wrapper)) {
            BaseProp prop = configGateway.getBaseProp(pack.getBaseId());
            if (prop != null && prop.getEffectType() != null && prop.getEffectType() == ConstData.PROP_EFFECT_STICKY) {
                return pack;
            }
        }
        return null;
    }
}
