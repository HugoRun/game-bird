package com.gamebird.game.bird.controller.game;

import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Role;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 教堂 / 婚姻（role.marriage_type）。
 *
 * <p>原项目空壳控制器，路由 404。婚姻状态直接记录在已存在的 {@code role} 表
 * {@code marriage_type} 字段上（0无/1喜鹊之恋/2鸳鸯之恋/3比翼鸟之恋/4天使之恋），
 * 无需新增表。这里提供状态展示与「结为伴侣」入口（设置婚姻类型）作为可用的基础版本。
 */
@RequestMapping("/marriage")
@Controller
public class MarriageController extends BaseController {

    /** 婚姻状态展示。 */
    @GetMapping(value = {"", "/info"})
    public String info(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        model.addAttribute("marriageType", role.getMarriageType());
        return "marriage/info";
    }

    /** 结为伴侣：设置婚姻类型（1~4）。 */
    @PostMapping(value = "/set")
    public String set(HttpSession session, Model model, @RequestParam(defaultValue = "1") Byte type) {
        Role role = getSessionRole(session);
        if (type < 1 || type > 4) {
            type = 1;
        }
        role.setMarriageType(type);
        roleService.updateById(role);
        saveSessionRole(session, role);
        model.addAttribute("message", "婚姻状态已更新。");
        model.addAttribute("marriageType", role.getMarriageType());
        return "marriage/info";
    }
}
