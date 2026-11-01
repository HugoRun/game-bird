package com.gamebird.game.bird.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseCage;
import com.gamebird.game.bird.model.dbo.BaseNest;
import com.gamebird.game.bird.model.dbo.BaseProp;
import com.gamebird.game.bird.model.dbo.BaseTrain;
import com.gamebird.game.bird.model.dbo.BaseTrap;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleBirth;
import com.gamebird.game.bird.model.dbo.RoleCage;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.RoleTrain;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.model.vo.UseResult;
import com.gamebird.game.bird.service.RoleBirthService;
import com.gamebird.game.bird.service.RoleCageService;
import com.gamebird.game.bird.service.RolePackService;
import com.gamebird.game.bird.service.RoleService;
import com.gamebird.game.bird.service.RoleTrapService;
import com.gamebird.game.bird.service.RoleTrainService;
import com.gamebird.game.bird.service.RoleValueService;
import com.gamebird.game.bird.service.StorageBirdService;
import com.gamebird.game.bird.util.BirdTextUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 包裹「使用」的唯一业务实现。
 *
 * <p>原先 5 类道具（陷阱/鸟笼/巢穴/训练场/道具）的使用按钮全部置灰，
 * 商店卖出的东西除诱饵外均为废品。本类补全 5 类装载逻辑，并把
 * {@code base_prop.effect_type} 的十余种效果收敛到一处分派。
 *
 * <p><b>事务边界</b>：与 {@code ShopPurchaseService} 同规 ——
 * {@code @Transactional} 只标注会被 Controller 直接调用的 public 方法；
 * 本类 private 方法若标注会因自调用绕过代理而静默失效。
 *
 * <p><b>失败路径不写库</b>：所有校验（归属/数量/槽位状态）全部在任何
 * 扣减与落库之前完成。
 */
@Service
public class PackUseService {

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public GameProperties gameProperties;

    @Resource
    public RoleValueService roleValueService;

    @Resource
    public RolePackService packService;

    @Resource
    public RoleTrapService trapService;

    @Resource
    public RoleCageService cageService;

    @Resource
    public RoleBirthService birthService;

    @Resource
    public RoleTrainService trainService;

    @Resource
    public StorageBirdService storageBirdService;

    @Resource
    public RoleService roleService;

    // ==================== 陷阱：装入陷阱槽 ====================

    /**
     * 把包裹中的陷阱装入指定陷阱槽位。
     *
     * <p>规则：捕鸟中的槽位禁止更换；替换时旧陷阱剩余耐久作废（原版语义：
     * 拆下的陷阱不能回收）。默认竹筐（use_trap_id=0）无需购买，不占包裹。
     *
     * @param role    当前角色
     * @param packId  包裹记录 id（role_pack.id）
     * @param trapIdx 陷阱槽位序号（1 起）
     * @return 操作结果文案
     */
    @Transactional(rollbackFor = Exception.class)
    public UseResult useTrap(Role role, Integer packId, int trapIdx) {
        RolePack pack = loadOwnedPack(role, packId, ConstData.PACK_TYPE_TRAP);
        BaseTrap baseTrap = configGateway.getBaseTrap(pack.getBaseId());
        if (baseTrap == null) {
            return UseResult.fail("陷阱配置错误。");
        }
        RoleTrap slot = loadTrapSlot(role, trapIdx);
        if (slot.getBaitId() > 0) {
            return UseResult.fail("该槽位正在捕鸟中，请先收网再更换陷阱。");
        }
        // 装入并扣减包裹
        slot.setUseTrapId(baseTrap.getId());
        slot.setUseTrapNum(baseTrap.getNum() == null ? 0 : baseTrap.getNum());
        trapService.updateById(slot);
        costPack(pack, 1);
        String durability = slot.getUseTrapNum() > 0 ? "，耐久" + slot.getUseTrapNum() + "次" : "";
        return UseResult.ok("已将「" + baseTrap.getName() + "」装入" + trapIdx + "号陷阱槽" + durability + "。");
    }

    // ==================== 鸟笼：装备出战 ====================

    /**
     * 装备鸟笼。鸟笼提供收网经验加成（{@code base_cage.func}），全角色共用一个
     * 出战栏位（role_cage idx=1，建角时不建行、首次装备时创建）。
     */
    @Transactional(rollbackFor = Exception.class)
    public UseResult useCage(Role role, Integer packId) {
        RolePack pack = loadOwnedPack(role, packId, ConstData.PACK_TYPE_CAGE);
        BaseCage baseCage = configGateway.getBaseCage(pack.getBaseId());
        if (baseCage == null) {
            return UseResult.fail("鸟笼配置错误。");
        }
        RoleCage cage = getCageSlot(role.getRoleId());
        cage.setUseCageId(baseCage.getId());
        cage.setUseCageNum0(baseCage.getNum() == null ? 0 : baseCage.getNum());
        cageService.saveOrUpdate(cage);
        costPack(pack, 1);
        String durability = cage.getUseCageNum0() > 0 ? "，耐久" + cage.getUseCageNum0() + "次" : "";
        return UseResult.ok("已装备「" + baseCage.getName() + "」" + (baseCage.getFunc() == null ? "" : "（" + baseCage.getFunc() + "）") + durability + "。");
    }

    // ==================== 巢穴：装入孵化槽 ====================

    /**
     * 把巢穴装入孵化槽。孵化中（含待好友确认）的槽位禁止换巢。
     */
    @Transactional(rollbackFor = Exception.class)
    public UseResult useNest(Role role, Integer packId, int birthIdx) {
        RolePack pack = loadOwnedPack(role, packId, ConstData.PACK_TYPE_NEST);
        BaseNest baseNest = configGateway.getBaseNest(pack.getBaseId());
        if (baseNest == null) {
            return UseResult.fail("巢穴配置错误。");
        }
        RoleBirth slot = loadBirthSlot(role, birthIdx);
        if (slot.getBirdId() > 0 || slot.getBirthEndTime() > 0) {
            return UseResult.fail("该槽位配对进行中，请先收获再更换巢穴。");
        }
        slot.setUseNestId(baseNest.getId());
        slot.setUseNestNum(baseNest.getNum() == null ? 0 : baseNest.getNum());
        birthService.updateById(slot);
        costPack(pack, 1);
        String durability = slot.getUseNestNum() > 0 ? "，耐久" + slot.getUseNestNum() + "次" : "";
        return UseResult.ok("已将「" + baseNest.getName() + "」放入" + birthIdx + "号巢穴" + durability + "。");
    }

    // ==================== 训练场：装入训练槽 ====================

    /**
     * 把训练场装入训练槽，同时刷新训练场有效期（{@code game.trainExpireDays} 天）。
     */
    @Transactional(rollbackFor = Exception.class)
    public UseResult useTrainGround(Role role, Integer packId, int trainIdx) {
        RolePack pack = loadOwnedPack(role, packId, ConstData.PACK_TYPE_TRAIN);
        BaseTrain baseTrain = configGateway.getBaseTrain(pack.getBaseId());
        if (baseTrain == null) {
            return UseResult.fail("训练场配置错误。");
        }
        RoleTrain slot = loadTrainSlot(role, trainIdx);
        if (slot.getBirdId() > 0) {
            return UseResult.fail("该槽位有小鸟正在训练，请先取回再更换场地。");
        }
        int now = CommonUtils.nowTime();
        slot.setUseTrainId(baseTrain.getId());
        slot.setUseTrainExpireTime(now + gameProperties.getTrainExpireDays() * 86400);
        trainService.updateById(slot);
        costPack(pack, 1);
        return UseResult.ok("已把「" + baseTrain.getName() + "」搬进" + trainIdx + "号训练槽，"
                + gameProperties.getTrainExpireDays() + "天内可训练小鸟。");
    }

    // ==================== 道具：按 effect_type 分派 ====================

    /**
     * 使用道具。按 {@code base_prop.effect_type} 分派：
     * 数值类直接生效；需要选择目标（鸟/槽位）的经可选参数指定；
     * 跳页类返回 redirectPath；礼包类解析 award_list 逐项发放。
     *
     * @param role    当前角色
     * @param packId  包裹记录 id
     * @param birdId  目标小鸟（克制/成长/变异药剂需要），可为 null
     * @param trapId  目标陷阱（捕捉加速需要），可为 null
     * @param trainId 目标训练槽（训练加速卡需要），可为 null
     * @param birthId 目标孵化槽（催产剂需要），可为 null
     */
    @Transactional(rollbackFor = Exception.class)
    public UseResult useProp(Role role, Integer packId, Integer birdId,
                             Integer trapId, Integer trainId, Integer birthId) {
        RolePack pack = loadOwnedPack(role, packId, ConstData.PACK_TYPE_PROP);
        BaseProp prop = configGateway.getBaseProp(pack.getBaseId());
        if (prop == null) {
            return UseResult.fail("道具配置错误。");
        }
        int effectType = prop.getEffectType() == null ? 0 : prop.getEffectType();
        int effectValue = prop.getEffectValue() == null ? 0 : prop.getEffectValue();
        switch (effectType) {
            case ConstData.PROP_EFFECT_LOVE -> {
                // 爱心树：文案「获得双倍爱心」，按双倍发放
                roleValueService.addRoleLovePoint(role, effectValue * 2);
                roleService.updateById(role);
                costPack(pack, 1);
                return UseResult.ok("「" + prop.getName() + "」散发出爱的气息，爱心 +" + effectValue * 2 + "。");
            }
            case ConstData.PROP_EFFECT_GENE -> {
                return useGeneProp(role, pack, prop, effectValue);
            }
            case 8 -> { // 捕捉加速 N 分钟
                return hastenTrap(role, trapId, effectValue * 60);
            }
            case 9 -> { // 捕捉加速剩余时间的 50%
                RoleTrap slot = loadOwnedTrap(role, trapId);
                if (slot.getBaitId() == 0) {
                    return UseResult.fail("该陷阱没有在捕鸟。");
                }
                int remain = slot.getCatchEndTime() - CommonUtils.nowTime();
                if (remain <= 0) {
                    return UseResult.fail("小鸟已经可以收网了。");
                }
                return applyTrapHasten(role, pack, slot, remain / 2, "「" + prop.getName() + "」的香气让小鸟提前" + remain / 120 + "分钟落网！");
            }
            case 10 -> { // 全体陷阱加速 N%
                int now = CommonUtils.nowTime();
                int totalMinutes = 0;
                List<RoleTrap> slots = listTrapSlots(role.getRoleId());
                for (RoleTrap slot : slots) {
                    if (slot.getBaitId() > 0 && slot.getCatchEndTime() > now) {
                        int remain = slot.getCatchEndTime() - now;
                        int cut = Math.min(remain, Math.max(1, remain * effectValue / 100));
                        slot.setCatchEndTime(slot.getCatchEndTime() - cut);
                        trapService.updateById(slot);
                        totalMinutes += cut / 60;
                    }
                }
                if (totalMinutes == 0) {
                    return UseResult.fail("没有正在捕鸟的陷阱。");
                }
                costPack(pack, 1);
                return UseResult.ok("「" + prop.getName() + "」让所有陷阱的等待缩短了约" + totalMinutes + "分钟！");
            }
            case 13 -> { // 金条：兑换金币
                roleValueService.addRoleCoin(role, (long) effectValue);
                roleService.updateById(role);
                costPack(pack, 1);
                return UseResult.ok("金子在哪都发光，兑换了 " + effectValue + " 金币。");
            }
            case 15 -> { // 训练加速卡：立即完成训练
                RoleTrain slot = loadOwnedTrain(role, trainId);
                if (slot.getBirdId() == 0 || slot.getTrainEndTime() <= 0) {
                    return UseResult.fail("该槽位没有小鸟在训练。");
                }
                int now = CommonUtils.nowTime();
                if (slot.getTrainEndTime() <= now) {
                    return UseResult.fail("训练已经完成，直接领取成果即可。");
                }
                slot.setTrainEndTime(now);
                trainService.updateById(slot);
                costPack(pack, 1);
                return UseResult.ok("时间扭转，训练瞬间完成！快去领取成果吧。");
            }
            case ConstData.PROP_EFFECT_STICKY -> {
                // 粘性道具在下饵页勾选使用，此处给出指引
                return UseResult.fail("粘性道具请在下饵时勾选「涂抹粘性道具」使用。");
            }
            case 16 -> { // 天赋药剂：重置克制值
                return resetBirdProp(role, pack, prop, birdId, true);
            }
            case 17 -> { // 成长药剂：重置成长值
                return resetBirdProp(role, pack, prop, birdId, false);
            }
            case 18 -> { // 不稳定药剂：50% 概率变异
                StorageBird bird = loadOwnedBird(role, birdId);
                if (CommonUtils.randNum(1, 100) <= 50) {
                    bird.setGrow((short) (ConstData.GROW_MAX + CommonUtils.randNum(0, 500)));
                    storageBirdService.updateById(bird);
                    costPack(pack, 1);
                    return UseResult.ok("变异成功！「" + birdText(bird) + "」的成长值飙升到" + BirdTextUtils.growText(bird.getGrow()) + "。");
                }
                costPack(pack, 1);
                return UseResult.ok("药剂似乎没什么效果……「" + birdText(bird) + "」毫无变化（下次也许能成）。");
            }
            case 19 -> { // 催产剂：孵化加速 N 分钟
                RoleBirth slot = loadOwnedBirth(role, birthId);
                if (slot.getBirdId() == 0 || slot.getBirthEndTime() <= 0) {
                    return UseResult.fail("该槽位没有在孵化。");
                }
                int now = CommonUtils.nowTime();
                if (slot.getBirthEndTime() <= now) {
                    return UseResult.fail("孵化已经完成，直接收获即可。");
                }
                slot.setBirthEndTime(slot.getBirthEndTime() - Math.min(effectValue * 60, slot.getBirthEndTime() - now));
                birthService.updateById(slot);
                costPack(pack, 1);
                return UseResult.ok("「" + prop.getName() + "」起效，孵化提前了约" + effectValue + "分钟！");
            }
            case ConstData.PROP_EFFECT_ROSE, ConstData.PROP_EFFECT_RING -> {
                // 玫瑰/对戒在教堂页使用
                return UseResult.redirectTo("/marriage");
            }
            case 40 -> {
                // 改名卡：改名页提交时消耗
                return UseResult.redirectTo("/role/name");
            }
            case 41 -> {
                // 性别卡：性别页提交时消耗
                return UseResult.redirectTo("/role/sex");
            }
            default -> {
                // 其余效果：若有礼包内容则按礼包发放
                return openGiftPack(role, pack, prop);
            }
        }
    }

    /**
     * 基因/配对卡（试管孵化）：随机抽一种在售小鸟，按效果值比例生成最大重量，
     * 直接入仓库。
     */
    private UseResult useGeneProp(Role role, RolePack pack, BaseProp prop, int percent) {
        List<BaseBird> pool = configGateway.getBaseBirdList();
        BaseBird picked = null;
        int total = 0;
        for (BaseBird bird : pool) {
            if (bird.getStatus() != null && bird.getStatus() == 1) {
                total++;
            }
        }
        if (total == 0) {
            return UseResult.fail("暂无可孵化的小鸟配置。");
        }
        int roll = CommonUtils.randNum(1, total);
        int index = 0;
        for (BaseBird bird : pool) {
            if (bird.getStatus() != null && bird.getStatus() == 1) {
                index++;
                if (index == roll) {
                    picked = bird;
                    break;
                }
            }
        }
        if (picked == null) {
            return UseResult.fail("暂无可孵化的小鸟配置。");
        }
        long weight = Math.max(picked.getMinWeight(), picked.getMaxWeight() * percent / 100);
        StorageBird bird = insertStorageBird(role, picked, randomSex(), weight,
                CommonUtils.randNum(0, 5).byteValue(), CommonUtils.randNum(ConstData.GROW_MIN, ConstData.GROW_MAX).shortValue());
        costPack(pack, 1);
        return UseResult.ok("试管孵化成功！获得" + birdText(bird) + "。");
    }

    /**
     * 重置小鸟的克制值（true）或成长值（false）。
     */
    private UseResult resetBirdProp(Role role, RolePack pack, BaseProp prop, Integer birdId, boolean resetKo) {
        StorageBird bird = loadOwnedBird(role, birdId);
        if (resetKo) {
            bird.setKo(CommonUtils.randNum(0, ConstData.KO_MAX).shortValue());
        } else {
            bird.setGrow(CommonUtils.randNum(ConstData.GROW_MIN, ConstData.GROW_MAX).shortValue());
        }
        storageBirdService.updateById(bird);
        costPack(pack, 1);
        return UseResult.ok("基因重组完成！「" + birdText(bird) + "」的"
                + (resetKo ? "克制值" : "成长值") + "已重置。");
    }

    /**
     * 捕捉中的陷阱加速指定分钟。
     */
    private UseResult hastenTrap(Role role, Integer trapId, int seconds) {
        RoleTrap slot = loadOwnedTrap(role, trapId);
        if (slot.getBaitId() == 0) {
            return UseResult.fail("该陷阱没有在捕鸟。");
        }
        int now = CommonUtils.nowTime();
        if (slot.getCatchEndTime() <= now) {
            return UseResult.fail("小鸟已经可以收网了。");
        }
        int cut = Math.min(seconds, slot.getCatchEndTime() - now);
        return applyTrapHasten(role, null, slot, cut, "小鸟提前" + Math.max(1, cut / 60) + "分钟落网！");
    }

    /**
     * 加速的公共落库段：{@code pack} 为 null 表示效果道具自身已计费（如加速卡走独立分支）。
     */
    private UseResult applyTrapHasten(Role role, RolePack pack, RoleTrap slot, int cutSeconds, String text) {
        slot.setCatchEndTime(slot.getCatchEndTime() - cutSeconds);
        trapService.updateById(slot);
        if (pack != null) {
            costPack(pack, 1);
        }
        return UseResult.ok(text);
    }

    /**
     * 打开礼包：award_list 格式 {@code [{"id":17,"num":3,"type":3}]}，
     * type 为包裹类别（1诱饵/2陷阱/3鸟笼/4巢穴/5训练场/6道具）。
     * 旧版 {t,s,c,n,i} 格式的条目跳过并在文案中注明。
     */
    private UseResult openGiftPack(Role role, RolePack pack, BaseProp prop) {
        String awardList = prop.getAwardList();
        if (awardList == null || awardList.trim().isEmpty()) {
            return UseResult.fail("该道具暂未开放使用。");
        }
        JSONArray items;
        try {
            items = JSONArray.parseArray(awardList);
        } catch (Exception e) {
            return UseResult.fail("礼包内容解析失败。");
        }
        if (items == null || items.isEmpty()) {
            return UseResult.fail("该道具暂未开放使用。");
        }
        StringBuilder granted = new StringBuilder();
        int skipped = 0;
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item == null || !item.containsKey("type") || !item.containsKey("id")) {
                skipped++;
                continue;
            }
            int type = item.getIntValue("type");
            int id = item.getIntValue("id");
            int num = item.getIntValue("num");
            if (num <= 0 || id <= 0 || type < ConstData.PACK_TYPE_BAIT || type > ConstData.PACK_TYPE_PROP) {
                skipped++;
                continue;
            }
            addPackItem(role, (byte) type, id, num);
            String name = propKindName(type, id);
            granted.append(name).append("×").append(num).append("、");
        }
        if (granted.isEmpty()) {
            return UseResult.fail("礼包内容暂未开放。");
        }
        costPack(pack, 1);
        String text = "打开「" + prop.getName() + "」获得：" + granted.substring(0, granted.length() - 1) + "。";
        if (skipped > 0) {
            text += "（另有" + skipped + "项内容暂未开放）";
        }
        return UseResult.ok(text);
    }

    // ==================== 公共小工具 ====================

    /** 载入属于当前角色、指定类别且数量大于 0 的包裹记录。 */
    private RolePack loadOwnedPack(Role role, Integer packId, byte typeId) {
        if (packId == null || packId <= 0) {
            return null;
        }
        RolePack pack = packService.getById(packId);
        if (pack == null || !role.getRoleId().equals(pack.getRoleId())
                || pack.getTypeId() == null || pack.getTypeId() != typeId
                || pack.getNum() == null || pack.getNum() <= 0) {
            return null;
        }
        return pack;
    }

    /** 按槽位序号取陷阱槽，不存在返回 null。 */
    private RoleTrap loadTrapSlot(Role role, int idx) {
        LambdaQueryWrapper<RoleTrap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTrap::getRoleId, role.getRoleId());
        wrapper.eq(RoleTrap::getIdx, idx);
        return trapService.getOne(wrapper);
    }

    /** 角色全部陷阱槽。 */
    private List<RoleTrap> listTrapSlots(String roleId) {
        LambdaQueryWrapper<RoleTrap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTrap::getRoleId, roleId);
        return trapService.list(wrapper);
    }

    /** 载入属于当前角色的陷阱槽（按 id），校验归属。 */
    private RoleTrap loadOwnedTrap(Role role, Integer trapId) {
        if (trapId == null || trapId <= 0) {
            return new RoleTrap();
        }
        RoleTrap slot = trapService.getById(trapId);
        if (slot == null || !role.getRoleId().equals(slot.getRoleId())) {
            return new RoleTrap();
        }
        return slot;
    }

    /** 载入属于当前角色的孵化槽（按 id）。 */
    private RoleBirth loadOwnedBirth(Role role, Integer birthId) {
        if (birthId == null || birthId <= 0) {
            return new RoleBirth();
        }
        RoleBirth slot = birthService.getById(birthId);
        if (slot == null || !role.getRoleId().equals(slot.getRoleId())) {
            return new RoleBirth();
        }
        return slot;
    }

    /** 载入属于当前角色的训练槽（按 id）。 */
    private RoleTrain loadOwnedTrain(Role role, Integer trainId) {
        if (trainId == null || trainId <= 0) {
            return new RoleTrain();
        }
        RoleTrain slot = trainService.getById(trainId);
        if (slot == null || !role.getRoleId().equals(slot.getRoleId())) {
            return new RoleTrain();
        }
        return slot;
    }

    /** 按槽位序号取孵化槽。 */
    private RoleBirth loadBirthSlot(Role role, int idx) {
        LambdaQueryWrapper<RoleBirth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleBirth::getRoleId, role.getRoleId());
        wrapper.eq(RoleBirth::getIdx, idx);
        RoleBirth slot = birthService.getOne(wrapper);
        return slot == null ? new RoleBirth() : slot;
    }

    /** 按槽位序号取训练槽。 */
    private RoleTrain loadTrainSlot(Role role, int idx) {
        LambdaQueryWrapper<RoleTrain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTrain::getRoleId, role.getRoleId());
        wrapper.eq(RoleTrain::getIdx, idx);
        RoleTrain slot = trainService.getOne(wrapper);
        return slot == null ? new RoleTrain() : slot;
    }

    /** 出战鸟笼栏位（role_cage idx=1），不存在则创建待插入对象。 */
    private RoleCage getCageSlot(String roleId) {
        LambdaQueryWrapper<RoleCage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleCage::getRoleId, roleId);
        wrapper.eq(RoleCage::getIdx, 1);
        RoleCage cage = cageService.getOne(wrapper);
        if (cage != null) {
            return cage;
        }
        RoleCage created = new RoleCage();
        created.setId(0);
        created.setRoleId(roleId);
        created.setIdx(1);
        created.setBirdId(0);
        created.setBaseBridId(0);
        created.setUseCageId(0);
        created.setUseCageNum0(0);
        return created;
    }

    /** 载入属于当前角色的小鸟。 */
    private StorageBird loadOwnedBird(Role role, Integer birdId) {
        if (birdId == null || birdId <= 0) {
            return new StorageBird();
        }
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            return new StorageBird();
        }
        return bird;
    }

    /** 扣减包裹数量（含落库）。 */
    private void costPack(RolePack pack, int num) {
        roleValueService.costPackNum(pack, num);
        packService.saveOrUpdate(pack);
    }

    /** 向包裹增发物品（无记录则新建），含落库。 */
    private void addPackItem(Role role, byte typeId, int baseId, int num) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, typeId);
        wrapper.eq(RolePack::getBaseId, baseId);
        RolePack pack = packService.getOne(wrapper);
        if (pack == null) {
            pack = new RolePack();
            pack.setId(0);
            pack.setRoleId(role.getRoleId());
            pack.setTypeId(typeId);
            pack.setBaseId(baseId);
            pack.setNum(0);
        }
        roleValueService.addPackNum(pack, num);
        packService.saveOrUpdate(pack);
    }

    /** 直接入库一只新鸟（收网/孵化/试管共用）。 */
    public StorageBird insertStorageBird(Role role, BaseBird baseBird, byte sex, long weight, byte star, short grow) {
        StorageBird bird = new StorageBird();
        bird.setId(0);
        bird.setRoleId(role.getRoleId());
        bird.setBirdId(baseBird.getId());
        bird.setLevel((short) 1);
        bird.setExp(0L);
        bird.setTotalExp(0L);
        bird.setSex(sex);
        bird.setWeight(weight);
        bird.setInitWeight(weight);
        bird.setStar(star);
        bird.setGrow(grow);
        bird.setKo(CommonUtils.randNum(0, ConstData.KO_MAX).shortValue());
        bird.setIsLock((byte) 0);
        bird.setTime(CommonUtils.nowTime());
        storageBirdService.save(bird);
        return bird;
    }

    /** 随机性别（1 雄 / 2 雌，与 test_account 的约定一致）。 */
    public byte randomSex() {
        return (byte) (CommonUtils.randNum(1, 100) <= 50 ? 1 : 2);
    }

    /** 小鸟的展示名：X斤的雄性麻雀(3星)。 */
    public static String birdText(StorageBird bird) {
        return bird.getWeight() / 100.0 + "斤的" + com.gamebird.game.bird.util.GameUtils.getBirdSexName(bird.getSex())
                + "小鸟(" + com.gamebird.game.bird.util.GameUtils.getStarName(bird.getStar()) + ")";
    }

    /** 礼包条目的物品名（按包裹类别查对应配置）。 */
    private String propKindName(int type, int id) {
        String name = switch (type) {
            case 1 -> {
                var bait = configGateway.getBaseBait(id);
                yield bait == null ? null : bait.getName();
            }
            case 2 -> {
                var trap = configGateway.getBaseTrap(id);
                yield trap == null ? null : trap.getName();
            }
            case 3 -> {
                var cage = configGateway.getBaseCage(id);
                yield cage == null ? null : cage.getName();
            }
            case 4 -> {
                var nest = configGateway.getBaseNest(id);
                yield nest == null ? null : nest.getName();
            }
            case 5 -> {
                var train = configGateway.getBaseTrain(id);
                yield train == null ? null : train.getName();
            }
            case 6 -> {
                var prop2 = configGateway.getBaseProp(id);
                yield prop2 == null ? null : prop2.getName();
            }
            default -> null;
        };
        return name == null ? "物品#" + id : name;
    }
}
