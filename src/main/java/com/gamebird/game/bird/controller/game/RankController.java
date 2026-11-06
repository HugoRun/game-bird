package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.service.RoleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 排行榜（rank）。
 *
 * <p>原项目空壳控制器，路由 404。排行榜可直接基于已存在的 {@code role} 表按不同维度
 * 排序得出，无需新增表结构。维度：等级 / 金钱 / 爱心值 / 小鸟数。
 */
@RequestMapping("/rank")
@Controller
public class RankController extends BaseController {

    @Resource
    public RoleService roleService;

    /** 排行榜首页，默认按等级排序。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model,
                      @RequestParam(defaultValue = "level") String type) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        switch (type) {
            case "coin" -> queryWrapper.orderByDesc(Role::getCoin);
            case "love" -> queryWrapper.orderByDesc(Role::getLovePoint);
            case "bird" -> queryWrapper.orderByDesc(Role::getBirdNum);
            default -> queryWrapper.orderByDesc(Role::getLevel);
        }
        queryWrapper.last("LIMIT 100");
        List<Role> roles = roleService.list(queryWrapper);
        model.addAttribute("roles", roles);
        model.addAttribute("type", type);
        return "rank/list";
    }
}
