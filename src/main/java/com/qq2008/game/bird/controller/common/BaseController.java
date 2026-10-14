package com.qq2008.game.bird.controller.common;

import java.util.Objects;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.service.AccountService;
import com.qq2008.game.bird.service.BaseBaitService;
import com.qq2008.game.bird.service.BaseBirdService;
import com.qq2008.game.bird.service.BaseCageService;
import com.qq2008.game.bird.service.BaseFieldService;
import com.qq2008.game.bird.service.BaseGeneService;
import com.qq2008.game.bird.service.BaseLevelService;
import com.qq2008.game.bird.service.BaseNestService;
import com.qq2008.game.bird.service.BasePropService;
import com.qq2008.game.bird.service.BaseTrainService;
import com.qq2008.game.bird.service.BaseTrapService;
import com.qq2008.game.bird.service.LogChatService;
import com.qq2008.game.bird.service.RoleBirthService;
import com.qq2008.game.bird.service.RolePackService;
import com.qq2008.game.bird.service.RoleService;
import com.qq2008.game.bird.service.RoleTrainService;
import com.qq2008.game.bird.service.RoleTrapService;
import com.qq2008.game.bird.service.RoleValueService;
import com.qq2008.game.bird.service.StorageBirdService;

/**
 * 所有Controller的基类
 * 包含一些公共处理方法
 */
public class BaseController {

    // service使用继承后的，便与代码生成器覆盖代码 todo
    //
    // 注意：下面 19 个字段刻意保持 public。20+ 个子类 Controller 直接以继承方式访问它们，
    // 改成 private 会导致子类编译失败，且逐个改为 protected 属于本轮范围外的机械改动。
    @Resource
    public BaseBaitService baseBaitService;
    @Resource
    public BaseBirdService baseBirdService;
    @Resource
    public BaseCageService baseCageService;
    @Resource
    public BaseFieldService baseFieldService;
    @Resource
    public BaseGeneService baseGeneService;
    @Resource
    public BaseLevelService baseLevelService;
    @Resource
    public BaseNestService baseNestService;
    @Resource
    public BasePropService basePropService;
    @Resource
    public BaseTrainService baseTrainService;
    @Resource
    public BaseTrapService baseTrapService;
    @Resource
    public AccountService accountService;
    @Resource
    public RoleService roleService;
    @Resource
    public RoleBirthService birthService;
    @Resource
    public RolePackService packService;
    @Resource
    public RoleTrainService trainService;
    @Resource
    public RoleTrapService trapService;
    @Resource
    public LogChatService chatService;
    @Resource
    public StorageBirdService storageBirdService;

    /**
     * 角色数值服务。9 个数值方法已下沉至此，本类只保留一行委托，
     * 以免 20+ 个子类 Controller 的调用点全部需要改动。
     */
    @Resource
    public RoleValueService roleValueService;

    /**
     * 转发到路由
     * 会携带相关的参数
     * @param path 转发目标路由
     * @return forward:path
     */
    public String forwardTo(String path) {
        return "forward:" + path;
    }

    /**
     * 跳转到到路由
     * 会丢弃携带的参数
     * @param path 跳转目标路由
     * @return redirect:path
     */
    public String redirectTo(String path) {
        return "redirect:" + path;
    }

    /**
     * 获取已登录角色
     * @param session session
     * @return 已登录角色
     */
    public Role getSessionRole(HttpSession session) {
        return (Role) session.getAttribute(ConstData.SESSION_ROLE_KEY);
    }

    /**
     * 更新已登录角色
     * @param session session
     * @param role 已登录角色
     */
    public void saveSessionRole(HttpSession session, Role role) {
        session.setAttribute(ConstData.SESSION_ROLE_KEY, role);
    }

    /**
     * 增加用户经验值
     * @param role 角色信息
     * @param addExp 增加经验值
     */
    public void addRoleExp(Role role, Long addExp) {
        roleValueService.addRoleExp(role, addExp);
    }

    /**
     * 增加用户金钱
     * @param role 角色信息
     * @param addCoin 增加金钱
     */
    public void addRoleCoin(Role role, Long addCoin) {
        roleValueService.addRoleCoin(role, addCoin);
    }

    /**
     * 扣除用户金钱
     * @param role 角色信息
     * @param costCoin 消耗金钱
     */
    public void costRoleCoin(Role role, Long costCoin) {
        roleValueService.costRoleCoin(role, costCoin);
    }

    /**
     * 增加用户钻石
     * @param role 角色信息
     * @param addDiamond 增加钻石
     */
    public void addRoleDiamond(Role role, int addDiamond) {
        roleValueService.addRoleDiamond(role, addDiamond);
    }

    /**
     * 扣除用户钻石
     * @param role 角色信息
     * @param costDiamond 消耗钻石
     */
    public void costRoleDiamond(Role role, int costDiamond) {
        roleValueService.costRoleDiamond(role, costDiamond);
    }

    /**
     * 增加用户爱心值
     * @param role 角色信息
     * @param addLovePoint 增加爱心值
     */
    public void addRoleLovePoint(Role role, int addLovePoint) {
        roleValueService.addRoleLovePoint(role, addLovePoint);
    }

    /**
     * 扣除用户爱心值
     * @param role 角色信息
     * @param costLovePoint 消耗爱心值
     */
    public void costRoleLovePoint(Role role, Long costLovePoint) {
        roleValueService.costRoleLovePoint(role, costLovePoint);
    }

    /**
     * 增加包裹物品数量
     * @param pack 物品信息
     * @param addNum 增加数量
     */
    public void addPackNum(RolePack pack, int addNum) {
        roleValueService.addPackNum(pack, addNum);
    }

    /**
     * 扣除背包物品数量
     * @param pack 物品信息
     * @param costPackNum 扣除物品数量
     */
    public void costPackNum(RolePack pack, int costPackNum) {
        roleValueService.costPackNum(pack, costPackNum);
    }

}
