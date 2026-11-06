package com.gamebird.game.bird.controller.game;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseProp;
import com.gamebird.game.bird.model.dbo.Friend;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.service.FriendService;
import com.gamebird.game.bird.service.GameEventService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 好友相关路由。
 *
 * <p>原项目空壳控制器，路由 404。数据来自新增的 {@code friend} 表，提供好友列表展示、
 * 添加好友（同一人不重复添加）与删除好友。角色会话由 {@code RoleAuthHandlerInterceptor}
 * 保证，本类不加 {@code @SkipRole}。
 */
@Controller
@RequestMapping("/friend")
public class FriendController extends BaseController {

    @Resource
    public FriendService friendService;

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public GameEventService gameEventService;

    /** 好友列表。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        String roleId = role.getRoleId();
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getRoleId, roleId);
        List<Friend> friends = friendService.list(wrapper);
        model.addAttribute("friends", friends);
        model.addAttribute("now", CommonUtils.nowTime());
        // 包裹里的玫瑰（赠礼表单）
        LambdaQueryWrapper<RolePack> packWrapper = new LambdaQueryWrapper<>();
        packWrapper.eq(RolePack::getRoleId, roleId);
        packWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        packWrapper.gt(RolePack::getNum, 0);
        List<RolePack> roses = packService.list(packWrapper).stream().filter(p -> {
            BaseProp prop = configGateway.getBaseProp(p.getBaseId());
            return prop != null && prop.getEffectType() != null && prop.getEffectType() == ConstData.PROP_EFFECT_ROSE;
        }).toList();
        model.addAttribute("roses", roses);
        return "friend/list";
    }

    /** 添加好友：同一人不重复添加。 */
    @PostMapping(value = "/add")
    public String add(HttpSession session, Model model,
                      @RequestParam(required = false, defaultValue = "") String friendRoleId,
                      @RequestParam(required = false, defaultValue = "") String friendName) {
        String roleId = getSessionRole(session).getRoleId();
        friendRoleId = (friendRoleId == null ? "" : friendRoleId.trim());
        friendName = (friendName == null ? "" : friendName.trim());
        if (friendRoleId.isEmpty()) {
            model.addAttribute("message", "好友角色Id不能为空。");
            return list(session, model);
        }
        if (friendName.isEmpty()) {
            friendName = friendRoleId;
        }
        long exist = friendService.count(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getRoleId, roleId)
                .eq(Friend::getFriendRoleId, friendRoleId));
        if (exist > 0) {
            model.addAttribute("message", "该好友已在列表中。");
            return list(session, model);
        }
        Friend friend = new Friend();
        friend.setRoleId(roleId);
        friend.setFriendRoleId(friendRoleId);
        friend.setFriendName(friendName);
        friend.setCreateTime(CommonUtils.nowTime());
        friendService.save(friend);
        model.addAttribute("message", "已添加好友 " + friendName + "。");
        return list(session, model);
    }

    /** 删除好友：记录必须属于当前角色。 */
    @PostMapping(value = "/remove")
    public String remove(HttpSession session, Model model, @RequestParam Integer id) {
        String roleId = getSessionRole(session).getRoleId();
        Friend friend = friendService.getById(id);
        if (friend == null || !roleId.equals(friend.getRoleId())) {
            model.addAttribute("message", "好友记录不存在或不属于当前角色。");
            return list(session, model);
        }
        String name = friend.getFriendName();
        friendService.removeById(id);
        model.addAttribute("message", "已删除好友 " + name + "。");
        return list(session, model);
    }

    /** 赠送玫瑰：消耗包裹中的玫瑰，对方获得双倍爱心，双方树林记录各留一笔。 */
    @PostMapping(value = "/gift")
    public String gift(HttpSession session, Model model,
                       @RequestParam Integer id,
                       @RequestParam Integer packId) {
        Role role = getSessionRole(session);
        Friend friend = friendService.getById(id);
        if (friend == null || !role.getRoleId().equals(friend.getRoleId())) {
            model.addAttribute("message", "好友记录不存在或不属于当前角色。");
            return list(session, model);
        }
        RolePack pack = packId == null ? null : packService.getById(packId);
        if (pack == null || !role.getRoleId().equals(pack.getRoleId())
                || pack.getTypeId() == null || pack.getTypeId() != ConstData.PACK_TYPE_PROP
                || pack.getNum() == null || pack.getNum() <= 0) {
            model.addAttribute("message", "请先选择一束玫瑰。");
            return list(session, model);
        }
        BaseProp rose = configGateway.getBaseProp(pack.getBaseId());
        if (rose == null || rose.getEffectType() == null || rose.getEffectType() != ConstData.PROP_EFFECT_ROSE
                || rose.getEffectValue() == null) {
            model.addAttribute("message", "那不是玫瑰，别拿鼻涕糊弄朋友。");
            return list(session, model);
        }
        Role friendRole = roleService.getById(friend.getFriendRoleId());
        if (friendRole == null) {
            model.addAttribute("message", "好友的角色已不存在。");
            return list(session, model);
        }
        int love = rose.getEffectValue() * 2;
        roleValueService.addRoleLovePoint(friendRole, love);
        roleService.updateById(friendRole);
        roleValueService.costPackNum(pack, 1);
        packService.saveOrUpdate(pack);
        gameEventService.record(role.getRoleId(),
                "送给 " + friendRole.getRoleName() + " 一束「" + rose.getName() + "」，花香传情。");
        gameEventService.record(friendRole.getRoleId(),
                role.getRoleName() + " 送来一束「" + rose.getName() + "」，爱心+" + love + "。");
        model.addAttribute("message", "已把「" + rose.getName() + "」送给 " + friendRole.getRoleName()
                + "，对方获得爱心 " + love + "。");
        return list(session, model);
    }
}
