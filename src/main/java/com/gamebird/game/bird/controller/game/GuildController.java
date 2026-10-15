package com.gamebird.game.bird.controller.game;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Guild;
import com.gamebird.game.bird.model.dbo.GuildMember;
import com.gamebird.game.bird.service.GuildMemberService;
import com.gamebird.game.bird.service.GuildService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 公会相关路由。
 *
 * <p>原项目空壳控制器，路由 404。数据来自新增的 {@code guild} 与 {@code guild_member} 表，
 * 提供公会列表展示、创建公会（当前角色为会长并写入成员记录）、加入公会、退出公会
 * （若是会长则同时解散公会并清退全部成员）。角色会话由 {@code RoleAuthHandlerInterceptor}
 * 保证，本类不加 {@code @SkipRole}。
 */
@Controller
@RequestMapping("/guild")
public class GuildController extends BaseController {

    @Resource
    public GuildService guildService;
    @Resource
    public GuildMemberService guildMemberService;

    /** 公会列表，并标记当前角色是否已加入某个公会。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        String roleId = getSessionRole(session).getRoleId();
        List<Guild> guilds = guildService.list();
        GuildMember member = guildMemberService.getOne(
                new LambdaQueryWrapper<GuildMember>().eq(GuildMember::getRoleId, roleId), false);
        boolean joined = member != null;
        model.addAttribute("guilds", guilds);
        model.addAttribute("joined", joined);
        model.addAttribute("myGuildId", joined ? member.getGuildId() : 0);
        model.addAttribute("now", CommonUtils.nowTime());
        return "guild/list";
    }

    /** 创建公会：当前角色为会长，并写入一条成员记录。公会名非空。 */
    @PostMapping(value = "/create")
    public String create(HttpSession session, Model model,
                        @RequestParam(required = false, defaultValue = "") String name) {
        String roleId = getSessionRole(session).getRoleId();
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("message", "公会名称不能为空。");
            return list(session, model);
        }
        long joined = guildMemberService.count(new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getRoleId, roleId));
        if (joined > 0) {
            model.addAttribute("message", "你已加入其它公会，无法创建。");
            return list(session, model);
        }
        Guild guild = new Guild();
        guild.setName(name.trim());
        guild.setLeaderRoleId(roleId);
        guild.setLevel(1);
        guild.setCreateTime(CommonUtils.nowTime());
        guildService.save(guild);
        GuildMember member = new GuildMember();
        member.setGuildId(guild.getId());
        member.setRoleId(roleId);
        member.setJoinTime(CommonUtils.nowTime());
        guildMemberService.save(member);
        model.addAttribute("message", "公会「" + name.trim() + "」创建成功。");
        return list(session, model);
    }

    /** 加入指定公会。 */
    @PostMapping(value = "/join")
    public String join(HttpSession session, Model model, @RequestParam Integer id) {
        String roleId = getSessionRole(session).getRoleId();
        Guild guild = guildService.getById(id);
        if (guild == null) {
            model.addAttribute("message", "公会不存在。");
            return list(session, model);
        }
        long joined = guildMemberService.count(new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getRoleId, roleId));
        if (joined > 0) {
            model.addAttribute("message", "你已加入其它公会。");
            return list(session, model);
        }
        GuildMember member = new GuildMember();
        member.setGuildId(id);
        member.setRoleId(roleId);
        member.setJoinTime(CommonUtils.nowTime());
        guildMemberService.save(member);
        model.addAttribute("message", "已加入公会「" + guild.getName() + "」。");
        return list(session, model);
    }

    /** 退出公会；若是会长则解散公会并清退全部成员。 */
    @PostMapping(value = "/leave")
    public String leave(HttpSession session, Model model, @RequestParam Integer id) {
        String roleId = getSessionRole(session).getRoleId();
        Guild guild = guildService.getById(id);
        if (guild == null) {
            model.addAttribute("message", "公会不存在。");
            return list(session, model);
        }
        guildMemberService.remove(new LambdaQueryWrapper<GuildMember>()
                .eq(GuildMember::getGuildId, id)
                .eq(GuildMember::getRoleId, roleId));
        if (roleId.equals(guild.getLeaderRoleId())) {
            guildMemberService.remove(new LambdaQueryWrapper<GuildMember>()
                    .eq(GuildMember::getGuildId, id));
            guildService.removeById(id);
            model.addAttribute("message", "公会「" + guild.getName() + "」已解散。");
        } else {
            model.addAttribute("message", "已退出公会「" + guild.getName() + "」。");
        }
        return list(session, model);
    }
}
