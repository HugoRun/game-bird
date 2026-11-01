package com.gamebird.game.bird.controller.game;

import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.SignLog;
import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.service.impl.SignInService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 每日签到（福利）。
 *
 * <p>主页「福利:签到」此前是置灰占位。GET 展示签到状态与奖励规则，
 * POST 执行签到（写操作统一 POST）。
 */
@Controller
public class SignInController extends BaseController {

    @Resource
    public SignInService signInService;

    /** 签到页。 */
    @GetMapping("/signin")
    public String pageSign(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        SignLog today = signInService.getByDay(role.getRoleId(), today());
        SignLog latest = signInService.latest(role.getRoleId());
        model.addAttribute("signedToday", today != null);
        model.addAttribute("todayLog", today);
        model.addAttribute("continuous", latest == null || isNotYesterday(latest) ? 0 : latest.getContinuous());
        return "role/signin";
    }

    /** 执行签到。 */
    @PostMapping("/signin")
    public String doSign(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        String result = signInService.sign(role);
        saveSessionRole(session, role);
        if (result == null) {
            MessageVO message = MessageVO.warn("今天已经签到过啦，明天再来。");
            message.addLink(new MessageLink("回主页", "/game"));
            model.addAttribute("message", message);
            return ConstData.VIEW_MESSAGE;
        }
        MessageVO message = MessageVO.success(result);
        message.addLink(new MessageLink("回主页", "/game"));
        model.addAttribute("message", message);
        return ConstData.VIEW_MESSAGE;
    }

    private String today() {
        return java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"))
                .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
    }

    /** 最近一次签到是否不是昨天（断签重置展示）。 */
    private boolean isNotYesterday(SignLog latest) {
        String yesterday = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).minusDays(1)
                .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        return !yesterday.equals(latest.getSignDate());
    }
}
