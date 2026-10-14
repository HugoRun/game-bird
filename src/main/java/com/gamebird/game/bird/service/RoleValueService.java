package com.gamebird.game.bird.service;

import org.springframework.stereotype.Service;

import com.gamebird.common.entity.BizException;
import com.gamebird.common.entity.ResultCode;

import com.gamebird.game.bird.config.GameConfigManager;
import com.gamebird.game.bird.model.dbo.BaseLevel;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.shop.FeeKind;

/**
 * 角色数值（经验 / 金币 / 钻石 / 爱心）与包裹数量的原子增减，以及等级计算。
 *
 * <p><b>为什么独立成 Service</b>：这些逻辑原先是 {@code BaseController} 里
 * 9 个逐字复制的 try/catch 模板（约 180 行），而它们本质是业务规则，
 * 不该寄居在 Controller 基类里。
 *
 * <p><b>可测试性</b>：{@code GameConfigManager} 通过构造器注入，测试只需
 * {@code new RoleValueService(mockConfigManager)}，完全不需要 Spring 容器 ——
 * 这正是把静态依赖剥出的目的。
 */
@Service
public class RoleValueService {

    private final GameConfigManager configManager;

    /**
     * 构造注入。
     *
     * @param configManager 配置管理器，提供等级门槛配置
     */
    public RoleValueService(GameConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * 增加用户经验值，并按配置重算等级。
     *
     * @param role   角色信息
     * @param addExp 增加的经验值，必须大于 0
     * @throws BizException 角色为空或增加值非法
     */
    public void addRoleExp(Role role, Long addExp) {
        if (role == null || addExp == null || addExp <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "增加的经验值必须大于 0。");
        }
        role.setExp(role.getExp() + addExp);
        role.setTotalExp(role.getTotalExp() + addExp);
        role.setLevel(calcRoleLevel(role.getLevel(), role.getTotalExp()));
    }

    /**
     * 增加用户金币。
     *
     * @param role    角色信息
     * @param addCoin 增加的金币，必须大于 0
     * @throws BizException 角色为空或增加值非法
     */
    public void addRoleCoin(Role role, Long addCoin) {
        if (role == null || addCoin == null || addCoin <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "增加的金币必须大于 0。");
        }
        role.setCoin(role.getCoin() + addCoin);
    }

    /**
     * 扣除用户金币。
     *
     * @param role     角色信息
     * @param costCoin 消耗金币，必须大于 0 且不超过余额
     * @throws BizException 参数非法或余额不足
     */
    public void costRoleCoin(Role role, Long costCoin) {
        if (role == null || costCoin == null || costCoin <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "扣除的金币必须大于 0。");
        }
        if (costCoin > role.getCoin()) {
            throw new BizException(ResultCode.CURRENCY_NOT_ENOUGH,
                    "您的金钱不足哦(" + role.getCoin() + "/" + costCoin + ")。");
        }
        role.setCoin(role.getCoin() - costCoin);
    }

    /**
     * 增加用户钻石。
     *
     * @param role       角色信息
     * @param addDiamond 增加的钻石，必须大于 0
     * @throws BizException 角色为空或增加值非法
     */
    public void addRoleDiamond(Role role, int addDiamond) {
        if (role == null || addDiamond <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "增加的钻石必须大于 0。");
        }
        role.setDiamond(role.getDiamond() + addDiamond);
    }

    /**
     * 扣除用户钻石。
     *
     * @param role        角色信息
     * @param costDiamond 消耗钻石，必须大于 0 且不超过余额
     * @throws BizException 参数非法或余额不足
     */
    public void costRoleDiamond(Role role, int costDiamond) {
        if (role == null || costDiamond <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "扣除的钻石必须大于 0。");
        }
        if (costDiamond > role.getDiamond()) {
            throw new BizException(ResultCode.CURRENCY_NOT_ENOUGH,
                    "您的钻石不足(" + role.getDiamond() + "/" + costDiamond + ")。");
        }
        role.setDiamond(role.getDiamond() - costDiamond);
    }

    /**
     * 增加用户爱心值。
     *
     * @param role         角色信息
     * @param addLovePoint 增加的爱心值，必须大于 0
     * @throws BizException 角色为空或增加值非法
     */
    public void addRoleLovePoint(Role role, int addLovePoint) {
        if (role == null || addLovePoint <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "增加的爱心值必须大于 0。");
        }
        role.setLovePoint(role.getLovePoint() + addLovePoint);
    }

    /**
     * 扣除用户爱心值。
     *
     * @param role          角色信息
     * @param costLovePoint 消耗爱心值，必须大于 0 且不超过余额
     * @throws BizException 参数非法或余额不足
     */
    public void costRoleLovePoint(Role role, Long costLovePoint) {
        if (role == null || costLovePoint == null || costLovePoint <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "扣除的爱心值必须大于 0。");
        }
        if (costLovePoint > role.getLovePoint()) {
            throw new BizException(ResultCode.CURRENCY_NOT_ENOUGH,
                    "您的爱心值不足(" + role.getLovePoint() + "/" + costLovePoint + ")。");
        }
        role.setLovePoint(role.getLovePoint() - costLovePoint);
    }

    /**
     * 按货币种类扣除对应余额。
     *
     * <p><b>为什么委托给 {@link FeeKind}</b>：购买流程原先在 {@code ShopController} 里
     * 用 3 个彼此独立的 {@code if} 扣 3 种货币（实测 {@code ShopController} 原文），
     * 校验通过后都不 return 直接往下走，一旦 {@code feeType} 出现第 4 种取值，
     * 三个分支都不进 → 道具白送且不扣费。改为按枚举穷举分派后，
     * 该隐患从结构上消失（见 {@link FeeKind} 类注释）。
     *
     * <p><b>先校验后扣除</b>：余额不足时抛异常且<b>不改角色</b>，
     * 因此不会发生「扣了一半」的状态。
     *
     * @param feeKind 货币种类
     * @param role    角色
     * @param fee     扣除金额，必须大于 0
     * @throws BizException 参数非法或余额不足
     */
    public void cost(FeeKind feeKind, Role role, long fee) {
        if (feeKind == null || role == null || fee <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "扣除的金额必须大于 0。");
        }
        if (feeKind.balance(role) < fee) {
            throw new BizException(ResultCode.CURRENCY_NOT_ENOUGH, feeKind.notEnoughText(role, fee));
        }
        feeKind.deduct(role, fee);
    }

    /**
     * 增加包裹物品数量。
     *
     * @param pack   物品信息
     * @param addNum 增加数量，必须大于 0
     * @throws BizException 物品为空或增加数量非法
     */
    public void addPackNum(RolePack pack, int addNum) {
        if (pack == null || addNum <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "增加的数量必须大于 0。");
        }
        pack.setNum(pack.getNum() + addNum);
    }

    /**
     * 扣除包裹物品数量。
     *
     * @param pack        物品信息
     * @param costPackNum 扣除数量，必须大于 0 且不超过持有量
     * @throws BizException 参数非法或持有量不足
     */
    public void costPackNum(RolePack pack, int costPackNum) {
        if (pack == null || costPackNum <= 0) {
            throw new BizException(ResultCode.PARAM_INVALID, "扣除的数量必须大于 0。");
        }
        if (pack.getNum() < costPackNum) {
            throw new BizException(ResultCode.PARAM_INVALID,
                    "包裹数量不足(" + pack.getNum() + "/" + costPackNum + ")。");
        }
        pack.setNum(pack.getNum() - costPackNum);
    }

    /**
     * 按累计经验重算等级：从当前等级向后扫描到 199，取满足经验门槛的最大等级。
     *
     * <p>算法逐字保留自原 {@code BaseController#calcRoleLevel}，唯一改动是配置来源
     * 从静态调用改为注入实例。
     *
     * @param oldLevel 旧等级（扫描起点）
     * @param totalExp 累计经验
     * @return 新的等级；配置缺失时返回 oldLevel
     */
    int calcRoleLevel(int oldLevel, long totalExp) {
        int tempLevel = oldLevel;
        for (int i = oldLevel; i < 200; i++) {
            BaseLevel baseLevel = configManager.getBaseLevel(i);
            if (baseLevel != null && totalExp >= baseLevel.getTotalExp()) {
                tempLevel = i;
            }
        }
        return tempLevel;
    }
}