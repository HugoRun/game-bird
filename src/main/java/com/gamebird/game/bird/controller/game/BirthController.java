package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleBirth;
import com.gamebird.game.bird.service.RoleBirthService;
import com.gamebird.common.util.CommonUtils;
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
 * 孵化场（role_birth）。
 *
 * <p>原项目空壳控制器，路由 404。数据来自已存在的 {@code role_birth} 表，与
 * {@link RoleController} 建角时初始化的孵化场槽位一致。提供「领取孵化成果」——
 * 孵化到期（birth_end_time 已达）后给角色结算经验并清空该槽位。
 */
@RequestMapping("/birth")
@Controller
public class BirthController extends BaseController {

    @Resource
    public RoleBirthService roleBirthService;

    /** 孵化场列表。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RoleBirth> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleBirth::getRoleId, role.getRoleId());
        queryWrapper.orderByAsc(RoleBirth::getIdx);
        List<RoleBirth> births = roleBirthService.list(queryWrapper);
        model.addAttribute("births", births);
        model.addAttribute("now", CommonUtils.nowTime());
        return "birth/list";
    }

    /** 领取孵化成果：孵化已到期则结算经验并清空槽位。 */
    @PostMapping(value = "/collect")
    public String collect(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleBirth birth = roleBirthService.getById(id);
        if (null == birth || !role.getRoleId().equals(birth.getRoleId())) {
            model.addAttribute("message", "孵化槽位不存在或不属于当前角色。");
            return list(session, model);
        }
        int now = CommonUtils.nowTime();
        if (birth.getBirthEndTime() == null || birth.getBirthEndTime() <= 0
                || birth.getBirthEndTime() > now) {
            model.addAttribute("message", "孵化尚未完成，请耐心等待。");
            return list(session, model);
        }
        long reward = 1000L;
        addRoleExp(role, reward);
        roleService.updateById(role);
        saveSessionRole(session, role);
        birth.setBirdId(0);
        birth.setBirdBaseId(0);
        birth.setMatchRoleId(0);
        birth.setMatchRoleName("");
        birth.setMatchBirdId(0);
        birth.setMatchBirdBaseId(0);
        birth.setBirthTime(0);
        birth.setBirthEndTime(0);
        birth.setUseNestId(0);
        birth.setUseNestNum(0);
        roleBirthService.updateById(birth);
        model.addAttribute("message", "孵化完成，获得经验 " + reward + "。");
        return list(session, model);
    }
}
