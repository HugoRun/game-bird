package com.qq2008.game.bird.controller.common;

import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.model.dbo.BaseLevel;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

import java.util.Objects;

/***
 * 所有Controller的基类
 * 包含一些公共处理方法
 */
public class BaseController {

    // service使用继承后的，便与代码生成器覆盖代码 todo
    @Resource
    public IBaseBaitService baseBaitService;
    @Resource
    public IBaseBirdService baseBirdService;
    @Resource
    public IBaseCageService baseCageService;
    @Resource
    public IBaseFieldService baseFieldService;
    @Resource
    public IBaseGeneService baseGeneService;
    @Resource
    public IBaseLevelService baseLevelService;
    @Resource
    public IBaseNestService baseNestService;
    @Resource
    public IBasePropService basePropService;
    @Resource
    public IBaseTrainService baseTrainService;
    @Resource
    public IBaseTrapService baseTrapService;
    @Resource
    public IAccountService accountService;
    @Resource
    public IRoleService roleService;
    @Resource
    public IRoleBirthService birthService;
    @Resource
    public IRolePackService packService;
    @Resource
    public IRoleTrainService trainService;
    @Resource
    public IRoleTrapService trapService;
    @Resource
    public ILogChatService chatService;
    @Resource
    public IStorageBirdService storageBirdService;

    /***
     * 转发到路由
     * 会携带相关的参数
     * @param path 转发目标路由
     * @return forward:path
     */
    public String forwardTo(String path) {
        return "forward:" + path;
    }

    /***
     * 跳转到到路由
     * 会丢弃携带的参数
     * @param path 跳转目标路由
     * @return redirect:path
     */
    public String redirectTo(String path) {
        return "redirect:" + path;
    }

    /***
     * 获取已登录角色
     * @param session session
     * @return 已登录角色
     */
    public Role getSessionRole(HttpSession session) {
        return (Role) session.getAttribute("sRole");
    }

    /***
     * 更新已登录角色
     * @param session session
     * @param role 已登录角色
     */
    public void saveSessionRole(HttpSession session, Role role) {
        session.setAttribute("sRole", role);
    }

    /***
     * 检测支付类型是否合法
     * @param feeType 支付类型, 0金钱/1钻石/2爱心值
     * @return 是否合法
     */
    public boolean isFeeTypeError(Byte feeType) {
        return !Objects.equals(feeType, ConstData.FEE_TYPE_COIN) && !Objects.equals(feeType, ConstData.FEE_TYPE_DIAMOND) && !Objects.equals(feeType, ConstData.FEE_TYPE_LOVE_POINT);
    }

    /***
     * 增加用户经验值
     * @param role 角色信息
     * @param addExp 增加经验值
     */
    public void addRoleExp(Role role, Long addExp) {
        try {
            // 增加金钱错误
            if (role == null || addExp <= 0) {
                throw new Exception();
            }
            // 增加经验值
            role.setExp(role.getExp() + addExp);
            role.setTotalExp(role.getTotalExp() + addExp);
            int newLevel = calcRoleLevel(role.getLevel(), role.getTotalExp());
            role.setLevel(newLevel);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 计算角色等级
     * @param oldLevel 旧的等级
     * @param totalExp 总经验
     * @return 新的觉得等级
     */
    int calcRoleLevel(int oldLevel, long totalExp){
        int tempLevel = oldLevel;
        for (int i = oldLevel; i < 200; i++) {
            BaseLevel baseLevel = GameConfigManager.getInstance().getBaseLevel(i);
            if(baseLevel != null && totalExp >= baseLevel.getTotalExp()){
                tempLevel = i;
            }
        }
        return tempLevel;
    }

    /***
     * 增加用户金钱
     * @param role 角色信息
     * @param addCoin 增加金钱
     */
    public void addRoleCoin(Role role, Long addCoin) {
        try {
            // 增加金钱错误
            if (role == null || addCoin <= 0) {
                throw new Exception();
            }
            // 增加金钱
            role.setCoin(role.getCoin() + addCoin);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 扣除用户金钱
     * @param role 角色信息
     * @param costCoin 消耗金钱
     */
    public void costRoleCoin(Role role, Long costCoin) {
        try {
            // 金钱不足
            if (role == null || costCoin <= 0 || costCoin > role.getCoin()) {
                throw new Exception();
            }
            // 扣除金钱
            role.setCoin(role.getCoin() - costCoin);
            // 写日志

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 增加用户钻石
     * @param role 角色信息
     * @param addDiamond 增加钻石
     */
    public void addRoleDiamond(Role role, int addDiamond) {
        try {
            // 增加钻石错误
            if (role == null || addDiamond <= 0) {
                throw new Exception();
            }
            // 增加钻石
            role.setDiamond(role.getDiamond() + addDiamond);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 扣除用户钻石
     * @param role 角色信息
     * @param costDiamond 消耗钻石
     */
    public void costRoleDiamond(Role role, int costDiamond) {
        try {
            // 钻石不足
            if (role == null || costDiamond <= 0 || costDiamond > role.getDiamond()) {
                throw new Exception();
            }
            // 扣除钻石
            role.setDiamond(role.getDiamond() - costDiamond);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 增加用户爱心值
     * @param role 角色信息
     * @param addLovePoint 增加爱心值
     */
    public void addRoleLovePoint(Role role, int addLovePoint) {
        try {
            // 增加爱心值错误
            if (role == null || addLovePoint <= 0) {
                throw new Exception();
            }
            // 增加爱心值
            role.setLovePoint(role.getLovePoint() + addLovePoint);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 扣除用户爱心值
     * @param role 角色信息
     * @param costLovePoint 消耗爱心值
     */
    public void costRoleLovePoint(Role role, Long costLovePoint) {
        try {
            // 爱心值不足
            if (role == null || costLovePoint <=  0 || costLovePoint > role.getLovePoint()) {
                throw new Exception();
            }
            // 扣除爱心值
            role.setLovePoint(role.getLovePoint() - costLovePoint);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 增加包裹物品数量
     * @param pack 物品信息
     * @param addNum 增加数量
     */
    public void addPackNum(RolePack pack, int addNum){
        try {
            // 参数错误
            if (pack == null || addNum <= 0) {
                throw new Exception();
            }
            pack.setNum(pack.getNum() + addNum);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * 扣除背包物品数量
     * @param pack 物品信息
     * @param costPackNum 扣除物品数量
     */
    public void costPackNum(RolePack pack, int costPackNum){
        try {
            // 参数错误
            if (pack == null || costPackNum <= 0 || pack.getNum() < costPackNum) {
                throw new Exception();
            }
            pack.setNum(pack.getNum() - costPackNum);
            // 写日志
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
