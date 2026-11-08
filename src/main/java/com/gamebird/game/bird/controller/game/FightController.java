package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.FightLog;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.service.FightLogService;
import com.gamebird.common.util.CommonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Random;

/**
 * 战斗相关路由（fight）。
 *
 * <p>原项目空壳控制器，路由 404。战斗记录写入新增的 {@code fight_log} 表。
 * 挑战时随机挑选一只 {@code base_bird} 作为对手，按角色等级与小鸟等级比较决定胜率。</p>
 */
@Controller
@RequestMapping("/fight")
public class FightController extends BaseController {

    @Resource
    public FightLogService fightLogService;

    /** 战斗记录列表：当前角色的战斗记录。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        LambdaQueryWrapper<FightLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FightLog::getRoleId, role.getRoleId());
        queryWrapper.orderByDesc(FightLog::getFightTime);
        List<FightLog> logs = fightLogService.list(queryWrapper);
        model.addAttribute("logs", logs);
        model.addAttribute("now", CommonUtils.nowTime());
        return "fight/list";
    }

    /** 发起挑战：模拟一场战斗并写入记录。 */
    @PostMapping("/challenge")
    public String challenge(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<BaseBird> birds = baseBirdService.list();
        if (birds == null || birds.isEmpty()) {
            model.addAttribute("message", "暂无可挑战的小鸟，请稍后再试。");
            return list(session, model);
        }
        Random random = new Random();
        BaseBird opponent = birds.get(random.nextInt(birds.size()));
        int roleLevel = role.getLevel() == null ? 0 : role.getLevel();
        int birdLv = opponent.getLv() == null ? 0 : opponent.getLv();
        // 角色等级高则 70% 胜，否则 40% 胜
        int winRate = roleLevel > birdLv ? 70 : 40;
        boolean win = random.nextInt(100) < winRate;

        FightLog log = new FightLog();
        log.setRoleId(role.getRoleId());
        log.setOpponentName(opponent.getName());
        log.setFightTime(CommonUtils.nowTime());
        if (win) {
            long reward = (long) birdLv * 100;
            log.setResult(1);
            log.setRewardCoin(reward);
            long coin = role.getCoin() == null ? 0L : role.getCoin();
            role.setCoin(coin + reward);
            roleService.updateById(role);
            saveSessionRole(session, role);
            model.addAttribute("message", "挑战 " + opponent.getName() + " 胜利！获得金钱 " + reward + "。");
        } else {
            log.setResult(0);
            log.setRewardCoin(0L);
            model.addAttribute("message", "挑战 " + opponent.getName() + " 失败，再接再厉！");
        }
        fightLogService.save(log);
        return list(session, model);
    }
}
