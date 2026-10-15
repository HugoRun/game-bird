package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Activity;
import com.gamebird.game.bird.service.ActivityService;
import com.gamebird.common.util.CommonUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 活动相关路由（act）。
 *
 * <p>原项目空壳控制器，路由 404。活动数据来自新增的 {@code activity} 表，
 * 仅展示当前处于上线状态且时间区间包含 {@code now} 的活动。</p>
 */
@Controller
@RequestMapping("/act")
public class ActivityController extends BaseController {

    @Resource
    public ActivityService activityService;

    /** 活动列表：status=1 且 now 在区间内的活动。 */
    @GetMapping(value = {"", "/list"})
    public String list(Model model) {
        int now = CommonUtils.nowTime();
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Activity::getStatus, 1);
        queryWrapper.le(Activity::getStartTime, now);
        queryWrapper.ge(Activity::getEndTime, now);
        queryWrapper.orderByAsc(Activity::getEndTime);
        List<Activity> activities = activityService.list(queryWrapper);
        model.addAttribute("activities", activities);
        model.addAttribute("now", now);
        return "act/list";
    }
}
