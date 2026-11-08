package com.gamebird.game.bird.controller.game;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Friend;
import com.gamebird.game.bird.service.FriendService;
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

    /** 好友列表。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        String roleId = getSessionRole(session).getRoleId();
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getRoleId, roleId);
        List<Friend> friends = friendService.list(wrapper);
        model.addAttribute("friends", friends);
        model.addAttribute("now", CommonUtils.nowTime());
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
}
