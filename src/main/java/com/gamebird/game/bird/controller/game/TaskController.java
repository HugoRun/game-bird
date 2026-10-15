package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.Task;
import com.gamebird.game.bird.service.TaskService;
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
 * 任务相关路由。
 *
 * <p>原项目空壳控制器，路由 404。任务数据来自新建的 {@code task} 表，
 * 归属当前登录角色（role_id）。提供「任务列表」与「领取已完成任务奖励」。
 */
@Controller
@RequestMapping("/task")
public class TaskController extends BaseController {

    @Resource
    public TaskService taskService;

    /** 任务列表：列出当前角色的全部任务（按 id 倒序）。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getRoleId, role.getRoleId());
        queryWrapper.orderByDesc(Task::getId);
        List<Task> tasks = taskService.list(queryWrapper);
        model.addAttribute("tasks", tasks);
        model.addAttribute("now", CommonUtils.nowTime());
        return "task/list";
    }

    /** 领取奖励：仅当任务 status=1（已完成未领）时发放奖励并把状态置为2。 */
    @PostMapping(value = "/claim")
    public String claim(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        Task task = taskService.getById(id);
        if (task == null || !role.getRoleId().equals(task.getRoleId())) {
            model.addAttribute("message", "任务不存在或不属于当前角色。");
            return list(session, model);
        }
        if (task.getStatus() == null || task.getStatus() != 1) {
            model.addAttribute("message", "该任务暂不可领取。");
            return list(session, model);
        }
        long reward = task.getRewardCoin() == null ? 0L : task.getRewardCoin();
        role.setCoin((role.getCoin() == null ? 0L : role.getCoin()) + reward);
        roleService.updateById(role);
        saveSessionRole(session, role);
        task.setStatus(2);
        taskService.updateById(task);
        model.addAttribute("message", "领取成功，获得金钱 " + reward + "。");
        return list(session, model);
    }
}
