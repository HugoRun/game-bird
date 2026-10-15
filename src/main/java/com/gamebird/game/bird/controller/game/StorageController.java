package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.service.BaseBirdService;
import com.gamebird.game.bird.service.StorageBirdService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小鸟仓库（storage_bird）。
 *
 * <p>原项目里这是个只有类级 {@code @RequestMapping("/storage")} 的空壳控制器，
 * 导致其前缀下所有路由 404。这里补齐列表与出售逻辑，数据全部来自已存在的
 * {@code storage_bird} 表，无需新增表结构。
 */
@RequestMapping("/storage")
@Controller
public class StorageController extends BaseController {

    @Resource
    public StorageBirdService storageBirdService;

    @Resource
    public BaseBirdService baseBirdService;

    /** 小鸟列表：按重量倒序，方便玩家优先查看重鸟。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        LambdaQueryWrapper<StorageBird> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageBird::getRoleId, role.getRoleId());
        queryWrapper.orderByDesc(StorageBird::getWeight);
        List<StorageBird> birds = storageBirdService.list(queryWrapper);
        Map<Integer, BaseBird> baseBirdMap = baseBirdService.list().stream()
                .collect(Collectors.toMap(BaseBird::getId, b -> b, (a, b) -> a));
        model.addAttribute("birds", birds);
        model.addAttribute("baseBirdMap", baseBirdMap);
        model.addAttribute("total", birds.size());
        return "storage/list";
    }

    /** 出售一只小鸟：按 base_bird.price（每斤价格）× 重量(斤) 折算金钱。 */
    @PostMapping(value = "/sell")
    public String sell(HttpSession session, Model model, @RequestParam Integer birdId,
                      @RequestParam(defaultValue = "1") Integer num) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (null == bird || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model);
        }
        BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
        long pricePerJin = baseBird == null ? 0 : baseBird.getPrice();
        long gain = pricePerJin * (bird.getWeight() / 100L) * num;
        if (gain <= 0) {
            model.addAttribute("message", "该小鸟无法出售（单价或重量为零）。");
            return list(session, model);
        }
        storageBirdService.removeById(birdId);
        // 直接累加金钱并落库（BaseController.addRoleCoin 只改内存，这里一并持久化）
        role.setCoin(role.getCoin() + gain);
        // 同步小鸟数量：排行榜的「小鸟数」维度读的是 role.bird_num，
        // 出售后必须递减，否则榜单显示与实际持有量脱节（bird_num 是
        // smallint unsigned，留个下界保护）。
        int left = (role.getBirdNum() == null ? 0 : role.getBirdNum()) - num;
        role.setBirdNum((short) Math.max(0, left));
        roleService.updateById(role);
        saveSessionRole(session, role);
        model.addAttribute("message", "出售成功，获得金钱 " + gain + "。");
        return list(session, model);
    }
}
