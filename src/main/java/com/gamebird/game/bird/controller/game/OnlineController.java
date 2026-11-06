package com.gamebird.game.bird.controller.game;

import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.service.impl.OnlineCounterService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 「谁在玩」：当前在线（有活跃 session 且已选择角色）的玩家列表。
 */
@Controller
public class OnlineController extends BaseController {

    @Resource
    public OnlineCounterService onlineCounterService;

    @GetMapping("/online")
    public String list(HttpSession session, Model model) {
        List<Role> roles = onlineCounterService.onlineRoles();
        model.addAttribute("roles", roles);
        model.addAttribute("sessionCount", onlineCounterService.count());
        return "game/online";
    }
}
