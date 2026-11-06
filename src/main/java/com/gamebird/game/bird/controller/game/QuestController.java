package com.gamebird.game.bird.controller.game;

import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.service.impl.QuestService;
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
 * 奋进目标（主线任务链 + 每日任务）。
 *
 * <p>页面按「主线 / 每日」分组展示进度与奖励；达成后在此交付，
 * 奖励为金币 + 钻石 + 可选道具（第九章奖励「神兽盛宴」饵料×2）。
 */
@Controller
@RequestMapping("/quest")
public class QuestController extends BaseController {

    @Resource
    public QuestService questService;

    /** 奋进目标页。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<QuestService.QuestView> views = questService.listFor(role);
        model.addAttribute("views", views);
        // 主线进度概览
        long done = views.stream().filter(v -> v.getDef().getChapter() != null
                && v.getDef().getChapter() == 1
                && v.getProgress() != null
                && v.getProgress().getStatus() != null
                && v.getProgress().getStatus() == 2).count();
        model.addAttribute("mainDone", done);
        return "quest/list";
    }

    /** 交付任务奖励。 */
    @PostMapping("/deliver")
    public String deliver(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        String result = id == null ? null : questService.deliver(role, id);
        saveSessionRole(session, role);
        if (result == null) {
            MessageVO message = MessageVO.warn("该任务尚未达成或已交付。");
            message.addLink(new MessageLink("回奋进目标", "/quest/list"));
            model.addAttribute("message", message);
            return ConstData.VIEW_MESSAGE;
        }
        MessageVO message = MessageVO.success(result);
        message.addLink(new MessageLink("回奋进目标", "/quest/list"));
        message.addLink(new MessageLink("回主页", "/game"));
        model.addAttribute("message", message);
        return ConstData.VIEW_MESSAGE;
    }
}
