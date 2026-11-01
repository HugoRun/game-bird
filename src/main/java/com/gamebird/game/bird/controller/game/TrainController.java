package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseTrain;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleTrain;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.RoleTrainService;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.util.GameUtils;
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
 * <p>完整的训练循环：包裹装入训练场 → {@code /train/start} 把一只空闲小鸟送进
 * 训练槽（锁定）→ 到期后 {@code /train/collect} 按 {@code base_train.min_exp}
 * 折算经验给小鸟升级并解锁；{@code /train/stop} 允许中途取回按已完成分钟折算。
 * 此前只有「领取成果」却没有任何开始训练的入口，且结算经验是写死的 1000。
 */
@RequestMapping("/train")
@Controller
public class TrainController extends BaseController {

    /** 游戏配置读取入口。 */
    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public RoleTrainService roleTrainService;

    @Resource
    public GameEventService gameEventService;

    /** 训练场列表。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<RoleTrain> trains = listTrainSlots(role);
        model.addAttribute("trains", trains);
        model.addAttribute("now", CommonUtils.nowTime());
        return "train/list";
    }

    /** 开始训练页：列出空闲小鸟供选择。 */
    @GetMapping("/start")
    public String pageStart(HttpSession session, Model model, @RequestParam(defaultValue = "1") int idx) {
        Role role = getSessionRole(session);
        RoleTrain slot = getSlot(role, idx);
        if (slot == null) {
            model.addAttribute("message", "训练槽位不存在。");
            return list(model, role);
        }
        if (slot.getBirdId() > 0) {
            model.addAttribute("message", "该槽位已有小鸟在训练。");
            return list(model, role);
        }
        if (slot.getUseTrainId() == null || slot.getUseTrainId() <= 0) {
            model.addAttribute("message", "请先到包裹把训练场装入训练槽。");
            return list(model, role);
        }
        if (slot.getUseTrainExpireTime() != null && slot.getUseTrainExpireTime() > 0
                && slot.getUseTrainExpireTime() < CommonUtils.nowTime()) {
            model.addAttribute("message", "训练场已过期，请到包裹重新装入。");
            return list(model, role);
        }
        BaseTrain baseTrain = configGateway.getBaseTrain(slot.getUseTrainId());
        model.addAttribute("slot", slot);
        model.addAttribute("baseTrain", baseTrain);
        model.addAttribute("durationText", ConstData.TRAIN_DURATION_SECONDS / 3600 + "小时");
        // 空闲小鸟：未锁定
        LambdaQueryWrapper<StorageBird> birdWrapper = new LambdaQueryWrapper<>();
        birdWrapper.eq(StorageBird::getRoleId, role.getRoleId());
        birdWrapper.eq(StorageBird::getIsLock, (byte) 0);
        birdWrapper.orderByDesc(StorageBird::getWeight);
        model.addAttribute("birds", storageBirdService.list(birdWrapper));
        return "train/start";
    }

    /** 开始训练：锁定小鸟并写入训练时间。 */
    @PostMapping("/start")
    public String start(HttpSession session, Model model, @RequestParam(defaultValue = "1") int idx,
                        @RequestParam Integer birdId) {
        Role role = getSessionRole(session);
        RoleTrain slot = getSlot(role, idx);
        if (slot == null) {
            model.addAttribute("message", "训练槽位不存在。");
            return list(model, role);
        }
        if (slot.getBirdId() > 0) {
            model.addAttribute("message", "该槽位已有小鸟在训练。");
            return list(model, role);
        }
        if (slot.getUseTrainId() == null || slot.getUseTrainId() <= 0) {
            model.addAttribute("message", "请先到包裹把训练场装入训练槽。");
            return list(model, role);
        }
        StorageBird bird = birdId == null ? null : storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(model, role);
        }
        if (bird.getIsLock() != null && bird.getIsLock() != 0) {
            model.addAttribute("message", "该小鸟正在训练或配对中。");
            return list(model, role);
        }
        BaseTrain baseTrain = configGateway.getBaseTrain(slot.getUseTrainId());
        int maxLevel = baseTrain == null || baseTrain.getMaxLevel() == null ? 100 : baseTrain.getMaxLevel();
        if (bird.getLevel() != null && bird.getLevel() >= maxLevel) {
            model.addAttribute("message", "该小鸟已达到" + baseTrain.getName() + "的训练上限（" + maxLevel + "级），换个更好的训练场吧。");
            return list(model, role);
        }
        int now = CommonUtils.nowTime();
        slot.setBirdId(bird.getId());
        slot.setBirdBaseId(bird.getBirdId());
        slot.setTrainTime(now);
        slot.setTrainEndTime(now + ConstData.TRAIN_DURATION_SECONDS);
        roleTrainService.updateById(slot);
        // 锁定小鸟（训练期间不可出售/配对）
        bird.setIsLock((byte) 1);
        storageBirdService.updateById(bird);
        String name = "一只小鸟";
        BaseBird baseBird = configGateway.getBaseBird(bird.getBirdId());
        if (baseBird != null) {
            name = GameUtils.getBirdSexName(bird.getSex()) + baseBird.getName();
        }
        model.addAttribute("message", name + "开始训练了，" + ConstData.TRAIN_DURATION_SECONDS / 3600 + "小时后回来领取成果。");
        return list(model, role);
    }

    /** 领取训练成果：到期后按分钟折算经验给小鸟升级并解锁。 */
    @PostMapping("/collect")
    public String collect(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleTrain train = roleTrainService.getById(id);
        if (null == train || !role.getRoleId().equals(train.getRoleId())) {
            model.addAttribute("message", "训练槽位不存在或不属于当前角色。");
            return list(model, role);
        }
        int now = CommonUtils.nowTime();
        if (train.getBirdId() == null || train.getBirdId() <= 0) {
            model.addAttribute("message", "该槽位没有小鸟在训练。");
            return list(model, role);
        }
        if (train.getTrainEndTime() == null || train.getTrainEndTime() <= 0
                || train.getTrainEndTime() > now) {
            model.addAttribute("message", "训练尚未完成，请耐心等待。");
            return list(model, role);
        }
        long reward = settleTrain(role, train, now, false);
        model.addAttribute("message", "训练完成！小鸟获得经验 " + reward + "。");
        return list(model, role);
    }

    /** 中途停止：按已完成分钟折算经验后取回小鸟。 */
    @PostMapping("/stop")
    public String stop(HttpSession session, Model model, @RequestParam Integer id) {
        Role role = getSessionRole(session);
        RoleTrain train = roleTrainService.getById(id);
        if (null == train || !role.getRoleId().equals(train.getRoleId())) {
            model.addAttribute("message", "训练槽位不存在或不属于当前角色。");
            return list(model, role);
        }
        int now = CommonUtils.nowTime();
        if (train.getBirdId() == null || train.getBirdId() <= 0) {
            model.addAttribute("message", "该槽位没有小鸟在训练。");
            return list(model, role);
        }
        long reward = settleTrain(role, train, now, true);
        model.addAttribute("message", "小鸟已取回，按已完成时长获得经验 " + reward + "。");
        return list(model, role);
    }

    /**
     * 训练结算的公共实现：折算经验 → 小鸟升级（每级需 level×100 经验，受训练场上限）→
     * 解锁小鸟 → 清空槽位 → 写事件日志。
     *
     * @return 小鸟获得的经验
     */
    private long settleTrain(Role role, RoleTrain train, int now, boolean early) {
        BaseTrain baseTrain = train.getUseTrainId() == null ? null : configGateway.getBaseTrain(train.getUseTrainId());
        int minExp = baseTrain == null || baseTrain.getMinExp() == null ? 1 : baseTrain.getMinExp();
        int maxLevel = baseTrain == null || baseTrain.getMaxLevel() == null ? 100 : baseTrain.getMaxLevel();
        int endTime = train.getTrainEndTime() == null ? 0 : train.getTrainEndTime();
        int effectiveEnd = early ? Math.min(now, endTime) : endTime;
        int minutes = Math.max(0, (effectiveEnd - (train.getTrainTime() == null ? 0 : train.getTrainTime())) / 60);
        long expGain = (long) minExp * minutes;
        StorageBird bird = storageBirdService.getById(train.getBirdId());
        if (bird != null && role.getRoleId().equals(bird.getRoleId())) {
            bird.setExp((bird.getExp() == null ? 0L : bird.getExp()) + expGain);
            bird.setTotalExp((bird.getTotalExp() == null ? 0L : bird.getTotalExp()) + expGain);
            // 升级：每级需 level×100 经验，最高不超过训练场上限
            short level = bird.getLevel() == null ? 1 : bird.getLevel();
            long remainExp = bird.getExp();
            while (level < maxLevel && remainExp >= (long) level * ConstData.BIRD_LEVEL_EXP_UNIT) {
                remainExp -= (long) level * ConstData.BIRD_LEVEL_EXP_UNIT;
                level++;
            }
            if (level >= maxLevel) {
                remainExp = 0;
            }
            bird.setLevel(level);
            bird.setExp(remainExp);
            bird.setIsLock((byte) 0);
            storageBirdService.updateById(bird);
            if (!early) {
                gameEventService.record(role.getRoleId(), "一只小鸟完成了在"
                        + (baseTrain == null ? "训练场" : baseTrain.getName()) + "的训练，升到" + level + "级。");
            }
        } else {
            // 鸟已不在（被异常清理）也要解锁槽位
            expGain = 0;
        }
        train.setBirdId(0);
        train.setBirdBaseId(0);
        train.setTrainTime(0);
        train.setTrainEndTime(0);
        roleTrainService.updateById(train);
        return expGain;
    }

    /** 角色全部训练槽（按槽位序号）。 */
    private List<RoleTrain> listTrainSlots(Role role) {
        LambdaQueryWrapper<RoleTrain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTrain::getRoleId, role.getRoleId());
        wrapper.orderByAsc(RoleTrain::getIdx);
        return roleTrainService.list(wrapper);
    }

    /** 按槽位序号取训练槽。 */
    private RoleTrain getSlot(Role role, int idx) {
        LambdaQueryWrapper<RoleTrain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTrain::getRoleId, role.getRoleId());
        wrapper.eq(RoleTrain::getIdx, idx);
        return roleTrainService.getOne(wrapper);
    }

    /** 渲染列表页并带上提示。 */
    private String list(Model model, Role role) {
        model.addAttribute("trains", listTrainSlots(role));
        model.addAttribute("now", CommonUtils.nowTime());
        return "train/list";
    }
}
