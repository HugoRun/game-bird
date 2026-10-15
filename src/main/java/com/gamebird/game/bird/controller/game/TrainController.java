package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleTrain;
import com.gamebird.game.bird.service.RoleTrainService;
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
 * 训练场（role_train）。
 *
 * <p>原项目空壳控制器，路由 404。数据来自已存在的 {@code role_train} 表，与
 * {@link RoleController} 建角时初始化的训练场槽位一致。提供「领取训练成果」——
 * 训练到期（train_end_time 已达）后给角色结算经验并清空该槽位。
 */
@RequestMapping("/train")
@Controller
public class TrainController extends BaseController {

    @Resource
    public RoleTrainService roleTrainService;

    /** 训练场列表。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RoleTrain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoleTrain::getRoleId, role.getRoleId());
        queryWrapper.orderByAsc(RoleTrain::getIdx);
        List<RoleTrain> trains = roleTrainService.list(queryWrapper);
        model.addAttribute("trains", trains);
        model.addAttribute("now", CommonUtils.nowTime());
        return "train/list";
    }

    /** 领取训练成果：训练已到期则结算经验并清空槽位。 */
    @PostMapping(value = "/collect")
    public String collect(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleTrain train = roleTrainService.getById(id);
        if (null == train || !role.getRoleId().equals(train.getRoleId())) {
            model.addAttribute("message", "训练槽位不存在或不属于当前角色。");
            return list(session, model);
        }
        int now = CommonUtils.nowTime();
        if (train.getTrainEndTime() == null || train.getTrainEndTime() <= 0
                || train.getTrainEndTime() > now) {
            model.addAttribute("message", "训练尚未完成，请耐心等待。");
            return list(session, model);
        }
        // 训练成果：按使用训练场类型折算经验（每级 1000 经验的占位规则，
        // 真实数值应查 base_train 配置，这里给一个可运行的基线）。
        long reward = 1000L * (train.getUseTrainId() == null ? 1 : Math.max(1, train.getUseTrainId()));
        addRoleExp(role, reward);
        roleService.updateById(role);
        saveSessionRole(session, role);
        // 清空槽位
        train.setBirdId(0);
        train.setBirdBaseId(0);
        train.setTrainTime(0);
        train.setTrainEndTime(0);
        train.setUseTrainId(0);
        train.setUseTrainExpireTime(0);
        roleTrainService.updateById(train);
        model.addAttribute("message", "领取成功，获得经验 " + reward + "。");
        return list(session, model);
    }
}
