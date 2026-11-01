package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleCage;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.service.BaseBirdService;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.RoleCageService;
import com.gamebird.game.bird.service.StorageBirdService;
import com.gamebird.game.bird.service.TaskTriggerService;
import com.gamebird.game.bird.service.impl.CollectionService;
import com.gamebird.game.bird.service.impl.QuestService;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小鸟仓库（storage_bird）。
 *
 * <p>原项目里这是个只有类级 {@code @RequestMapping("/storage")} 的空壳控制器，
 * 导致其前缀下所有路由 404。现已补齐列表、出售（含确认页）、设置出战鸟；
 * 训练/配对中的小鸟会被 {@code is_lock} 锁定，禁止出售。
 */
@RequestMapping("/storage")
@Controller
public class StorageController extends BaseController {

    @Resource
    public StorageBirdService storageBirdService;

    @Resource
    public BaseBirdService baseBirdService;

    @Resource
    public RoleCageService cageService;

    @Resource
    public GameConfigGateway configGateway;

    @Resource
    public GameEventService gameEventService;

    @Resource
    public QuestService questService;

    @Resource
    public CollectionService collectionService;

    @Resource
    public TaskTriggerService taskTriggerService;

    /** 图鉴页：全部小鸟配置按场景分组，已收集的高亮。 */
    @GetMapping("/book")
    public String book(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<BaseBird> allBirds = configGateway.getBaseBirdList().stream()
                .sorted(java.util.Comparator.comparing(BaseBird::getField, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .thenComparing(BaseBird::getId))
                .toList();
        java.util.Set<Integer> collected = new java.util.HashSet<>(collectionService.collectedBirdIds(role.getRoleId()));
        model.addAttribute("allBirds", allBirds);
        model.addAttribute("collected", collected);
        model.addAttribute("collectedCount", collected.size());
        model.addAttribute("totalCount", allBirds.size());
        return "storage/book";
    }

    /** 放生确认页：返还爱心，锁定鸟不可放生。 */
    @GetMapping("/releaseConfirm")
    public String releaseConfirm(HttpSession session, Model model, @RequestParam Integer birdId) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        if (bird.getIsLock() != null && bird.getIsLock() != 0) {
            model.addAttribute("message", "该小鸟正在训练或配对中，不能放生。");
            return list(session, model, "weight", null);
        }
        BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
        model.addAttribute("bird", bird);
        model.addAttribute("baseBird", baseBird);
        model.addAttribute("loveBack", releaseLove(bird));
        return "storage/releaseConfirm";
    }

    /** 放生：返还爱心（星级×50 + 重量/200），写事件。 */
    @PostMapping("/release")
    public String release(HttpSession session, Model model, @RequestParam Integer birdId) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        if (bird.getIsLock() != null && bird.getIsLock() != 0) {
            model.addAttribute("message", "该小鸟正在训练或配对中，不能放生。");
            return list(session, model, "weight", null);
        }
        BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
        long love = releaseLove(bird);
        storageBirdService.removeById(birdId);
        role.setLovePoint((role.getLovePoint() == null ? 0L : role.getLovePoint()) + love);
        int left = (role.getBirdNum() == null ? 0 : role.getBirdNum()) - 1;
        role.setBirdNum((short) Math.max(0, left));
        roleService.updateById(role);
        saveSessionRole(session, role);
        String name = baseBird == null ? "小鸟" : baseBird.getName();
        gameEventService.record(role.getRoleId(),
                "把" + bird.getWeight() / 100.0 + "斤的" + GameUtils.getBirdSexName(bird.getSex()) + name
                        + "放归了树林，天空多了一声鸟鸣（爱心+" + love + "）。");
        model.addAttribute("message", "放生成功，获得爱心 " + love + "。图鉴记录保留。");
        return list(session, model, "weight", null);
    }

    /** 鸟详情页：我方小鸟的完整数值。 */
    @GetMapping("/bird")
    public String birdDetail(HttpSession session, Model model, @RequestParam Integer birdId) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        model.addAttribute("bird", bird);
        model.addAttribute("baseBird", baseBirdService.getById(bird.getBirdId()));
        model.addAttribute("typeNames", Map.of(1, "敏捷", 2, "力量", 3, "魔法", 4, "幻想"));
        return "storage/birdDetail";
    }

    /** 放生返还的爱心。 */
    private long releaseLove(StorageBird bird) {
        int star = bird.getStar() == null ? 0 : bird.getStar();
        long weight = bird.getWeight() == null ? 0 : bird.getWeight();
        return Math.max(10, star * 50L + weight / 200);
    }

    /**
     * 小鸟列表：支持排序（weight/level/star/time/price）与物种关键词搜索。
     * 数据量小（上限 50），排序在内存完成，身价排序依赖配置表联算。
     */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model,
                       @RequestParam(defaultValue = "weight") String sort,
                       @RequestParam(required = false) String q) {
        Role role = getSessionRole(session);
        List<StorageBird> all = listBirds(role);
        Map<Integer, BaseBird> baseBirdMap = baseBirdService.list().stream()
                .collect(Collectors.toMap(BaseBird::getId, b -> b, (a, b) -> a));
        // 仓库总值（不受筛选影响）
        long totalValue = 0;
        for (StorageBird bird : all) {
            totalValue += sellGain(baseBirdMap.get(bird.getBirdId()), bird);
        }
        // 搜索：按物种名包含关键词
        String keyword = q == null ? "" : q.trim();
        List<StorageBird> birds = all;
        if (!keyword.isEmpty()) {
            birds = all.stream().filter(b -> {
                BaseBird cfg = baseBirdMap.get(b.getBirdId());
                return cfg != null && cfg.getName() != null && cfg.getName().contains(keyword);
            }).toList();
        }
        // 排序
        java.util.Comparator<StorageBird> byWeight = java.util.Comparator.comparing(
                StorageBird::getWeight, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed();
        switch (sort == null ? "weight" : sort) {
            case "level" -> birds = birds.stream().sorted(java.util.Comparator
                    .comparing(StorageBird::getLevel, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                    .reversed().thenComparing(byWeight)).toList();
            case "star" -> birds = birds.stream().sorted(java.util.Comparator
                    .comparing(StorageBird::getStar, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                    .reversed().thenComparing(byWeight)).toList();
            case "time" -> birds = birds.stream().sorted(java.util.Comparator
                    .comparing(StorageBird::getTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                    .reversed()).toList();
            case "price" -> birds = birds.stream().sorted(java.util.Comparator
                    .comparing((StorageBird b) -> sellGain(baseBirdMap.get(b.getBirdId()), b)).reversed()
                    .thenComparing(byWeight)).toList();
            default -> birds = birds.stream().sorted(byWeight).toList();
        }
        model.addAttribute("birds", birds);
        model.addAttribute("baseBirdMap", baseBirdMap);
        model.addAttribute("total", all.size());
        model.addAttribute("filteredCount", birds.size());
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("sort", sort == null ? "weight" : sort);
        model.addAttribute("q", keyword);
        model.addAttribute("cagedBirdId", getCagedBirdId(role.getRoleId()));
        return "storage/list";
    }

    /** 出售确认页：展示折算金额，确认后走 POST /storage/sell。 */
    @GetMapping("/sellConfirm")
    public String sellConfirm(HttpSession session, Model model, @RequestParam Integer birdId) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
        long gain = sellGain(baseBird, bird);
        model.addAttribute("bird", bird);
        model.addAttribute("baseBird", baseBird);
        model.addAttribute("gain", gain);
        return "storage/sellConfirm";
    }

    /** 出售一只小鸟：按 base_bird.price（每斤价格）× 重量(斤) 折算金钱。 */
    @PostMapping(value = "/sell")
    public String sell(HttpSession session, Model model, @RequestParam Integer birdId,
                      @RequestParam(defaultValue = "1") Integer num) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (null == bird || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        if (bird.getIsLock() != null && bird.getIsLock() != 0) {
            model.addAttribute("message", "该小鸟正在训练或配对中，不能出售。");
            return list(session, model, "weight", null);
        }
        BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
        long gain = sellGain(baseBird, bird);
        if (gain <= 0) {
            model.addAttribute("message", "该小鸟无法出售（单价或重量为零）。");
            return list(session, model, "weight", null);
        }
        storageBirdService.removeById(birdId);
        questService.onEvent(role, QuestService.EV_SELL, 1);
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
        return list(session, model, "weight", null);
    }

    /** 一键出售确认页：列出全部未锁定小鸟的只数与总价值。 */
    @GetMapping("/sellAllConfirm")
    public String sellAllConfirm(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<StorageBird> birds = listBirds(role).stream()
                .filter(b -> b.getIsLock() == null || b.getIsLock() == 0)
                .toList();
        long totalGain = 0;
        for (StorageBird bird : birds) {
            BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
            totalGain += sellGain(baseBird, bird);
        }
        model.addAttribute("count", birds.size());
        model.addAttribute("totalGain", totalGain);
        return "storage/sellAllConfirm";
    }

    /** 一键出售：卖出全部未锁定的小鸟（锁定/训练配对中的不动）。 */
    @PostMapping("/sellAll")
    public String sellAll(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<StorageBird> birds = listBirds(role).stream()
                .filter(b -> b.getIsLock() == null || b.getIsLock() == 0)
                .toList();
        if (birds.isEmpty()) {
            model.addAttribute("message", "没有可出售的小鸟（训练/配对中的已被锁定保护）。");
            return list(session, model, "weight", null);
        }
        long totalGain = 0;
        for (StorageBird bird : birds) {
            BaseBird baseBird = baseBirdService.getById(bird.getBirdId());
            totalGain += sellGain(baseBird, bird);
            storageBirdService.removeById(bird.getId());
        }
        questService.onEvent(role, QuestService.EV_SELL, birds.size());
        role.setCoin(role.getCoin() + totalGain);
        int left = Math.max(0, (role.getBirdNum() == null ? 0 : role.getBirdNum()) - birds.size());
        role.setBirdNum((short) left);
        roleService.updateById(role);
        saveSessionRole(session, role);
        gameEventService.record(role.getRoleId(),
                "一口气出售了" + birds.size() + "只小鸟，进账金币" + totalGain + "。");
        model.addAttribute("message", "一键出售完成：卖出 " + birds.size() + " 只，共获得金币 " + totalGain + "。");
        return list(session, model, "weight", null);
    }

    // 手动锁定的字段值（训练/配对用 1，手动锁定用 2，两者都受出售/放生保护）
    public static final int LOCK_MANUAL = 2;

    /** 手动锁定/解锁：训练（is_lock=1）中的鸟不能手动解锁。 */
    @PostMapping("/lock")
    public String lock(HttpSession session, Model model, @RequestParam Integer birdId,
                       @RequestParam(defaultValue = "1") int lock) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        if (lock == 1) {
            if (bird.getIsLock() != null && bird.getIsLock() != 0) {
                model.addAttribute("message", "该小鸟已在锁定状态。");
                return list(session, model, "weight", null);
            }
            bird.setIsLock((byte) LOCK_MANUAL);
            storageBirdService.updateById(bird);
            model.addAttribute("message", "已锁定，出售/放生/一键操作都不会再动它。");
        } else {
            if (bird.getIsLock() == null || bird.getIsLock() != LOCK_MANUAL) {
                model.addAttribute("message", "训练/配对中的锁定不能手动解除，等它完成吧。");
                return list(session, model, "weight", null);
            }
            bird.setIsLock((byte) 0);
            storageBirdService.updateById(bird);
            model.addAttribute("message", "已解锁。");
        }
        return list(session, model, "weight", null);
    }

    /** 一键放生确认页：列出全部未锁定小鸟的只数与返还爱心。 */
    @GetMapping("/releaseAllConfirm")
    public String releaseAllConfirm(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<StorageBird> birds = listBirds(role).stream()
                .filter(b -> b.getIsLock() == null || b.getIsLock() == 0)
                .toList();
        long totalLove = 0;
        for (StorageBird bird : birds) {
            totalLove += releaseLove(bird);
        }
        model.addAttribute("count", birds.size());
        model.addAttribute("totalLove", totalLove);
        return "storage/releaseAllConfirm";
    }

    /** 一键放生：放归全部未锁定的小鸟，返还爱心。 */
    @PostMapping("/releaseAll")
    public String releaseAll(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        List<StorageBird> birds = listBirds(role).stream()
                .filter(b -> b.getIsLock() == null || b.getIsLock() == 0)
                .toList();
        if (birds.isEmpty()) {
            model.addAttribute("message", "没有可放生的小鸟（训练/配对中的已被锁定保护）。");
            return list(session, model, "weight", null);
        }
        long totalLove = 0;
        for (StorageBird bird : birds) {
            totalLove += releaseLove(bird);
            storageBirdService.removeById(bird.getId());
        }
        role.setLovePoint((role.getLovePoint() == null ? 0L : role.getLovePoint()) + totalLove);
        int left = Math.max(0, (role.getBirdNum() == null ? 0 : role.getBirdNum()) - birds.size());
        role.setBirdNum((short) left);
        roleService.updateById(role);
        saveSessionRole(session, role);
        gameEventService.record(role.getRoleId(),
                "打开了鸟笼的大门，" + birds.size() + "只小鸟集体飞回了树林（爱心+" + totalLove + "）。");
        model.addAttribute("message", "一键放生完成：放飞 " + birds.size() + " 只，共获得爱心 " + totalLove + "。");
        return list(session, model, "weight", null);
    }

    /** 批量操作确认页：汇总只数与收益，隐藏参数带到执行端。 */
    @GetMapping("/batchConfirm")
    public String batchConfirm(HttpSession session, Model model,
                               @RequestParam String action,
                               @RequestParam String ids) {
        Role role = getSessionRole(session);
        List<StorageBird> birds = resolveOwned(role, ids);
        String label;
        long total = 0;
        switch (action) {
            case "sell" -> {
                label = "批量出售";
                for (StorageBird bird : birds) {
                    total += sellGain(baseBirdService.getById(bird.getBirdId()), bird);
                }
            }
            case "release" -> {
                label = "批量放生";
                for (StorageBird bird : birds) {
                    total += releaseLove(bird);
                }
            }
            case "lock" -> label = "批量锁定";
            case "unlock" -> label = "批量解锁";
            default -> {
                model.addAttribute("message", "未知操作。");
                return list(session, model, "weight", null);
            }
        }
        model.addAttribute("action", action);
        model.addAttribute("actionLabel", label);
        model.addAttribute("idList", ids);
        model.addAttribute("count", birds.size());
        model.addAttribute("total", total);
        model.addAttribute("unit", "sell".equals(action) ? "金币" : ("release".equals(action) ? "爱心" : ""));
        return "storage/batchConfirm";
    }

    /** 执行批量操作（sell/release/lock/unlock）。 */
    @PostMapping("/batch")
    public String batch(HttpSession session, Model model,
                        @RequestParam String action,
                        @RequestParam String ids) {
        Role role = getSessionRole(session);
        List<StorageBird> birds = resolveOwned(role, ids);
        if (birds.isEmpty()) {
            model.addAttribute("message", "没有可操作的小鸟。");
            return list(session, model, "weight", null);
        }
        long total = 0;
        int done = 0;
        int skipped = 0;
        for (StorageBird bird : birds) {
            boolean manualLocked = bird.getIsLock() != null && bird.getIsLock() == LOCK_MANUAL;
            boolean busy = bird.getIsLock() != null && bird.getIsLock() == 1;
            switch (action) {
                case "sell" -> {
                    if (busy) {
                        skipped++;
                        continue;
                    }
                    total += sellGain(baseBirdService.getById(bird.getBirdId()), bird);
                    storageBirdService.removeById(bird.getId());
                    done++;
                }
                case "release" -> {
                    if (busy) {
                        skipped++;
                        continue;
                    }
                    total += releaseLove(bird);
                    storageBirdService.removeById(bird.getId());
                    done++;
                }
                case "lock" -> {
                    if (bird.getIsLock() != null && bird.getIsLock() != 0) {
                        skipped++;
                        continue;
                    }
                    bird.setIsLock((byte) LOCK_MANUAL);
                    storageBirdService.updateById(bird);
                    done++;
                }
                case "unlock" -> {
                    if (!manualLocked) {
                        skipped++;
                        continue;
                    }
                    bird.setIsLock((byte) 0);
                    storageBirdService.updateById(bird);
                    done++;
                }
                default -> {
                    model.addAttribute("message", "未知操作。");
                    return list(session, model, "weight", null);
                }
            }
        }
        if (done > 0 && ("sell".equals(action) || "release".equals(action))) {
            int left = Math.max(0, (role.getBirdNum() == null ? 0 : role.getBirdNum()) - done);
            role.setBirdNum((short) left);
        }
        if ("sell".equals(action) && total > 0) {
            role.setCoin(role.getCoin() + total);
        }
        if ("release".equals(action) && total > 0) {
            role.setLovePoint((role.getLovePoint() == null ? 0L : role.getLovePoint()) + total);
        }
        if (done > 0) {
            roleService.updateById(role);
            saveSessionRole(session, role);
            if ("sell".equals(action)) {
                gameEventService.record(role.getRoleId(),
                        "批量出售了" + done + "只小鸟，进账金币" + total + "。");
            } else if ("release".equals(action)) {
                gameEventService.record(role.getRoleId(),
                        "批量放生了" + done + "只小鸟，收获爱心" + total + "。");
            }
        }
        String actionLabel = "sell".equals(action) ? "出售" : ("release".equals(action) ? "放生"
                : ("lock".equals(action) ? "锁定" : "解锁"));
        model.addAttribute("message", "批量" + actionLabel + "完成：成功 " + done
                + " 只" + (skipped > 0 ? "，跳过 " + skipped + " 只（锁定/状态不符）" : "")
                + (total > 0 ? "，获得 " + total + ("sell".equals(action) ? " 金币" : " 爱心") : "") + "。");
        return list(session, model, "weight", null);
    }

    /** 按「1,2,3」字符串解析并校验归属的小鸟列表。 */
    private List<StorageBird> resolveOwned(Role role, String ids) {
        List<StorageBird> result = new java.util.ArrayList<>();
        if (ids == null || ids.trim().isEmpty()) {
            return result;
        }
        for (String part : ids.split(",")) {
            try {
                StorageBird bird = storageBirdService.getById(Integer.parseInt(part.trim()));
                if (bird != null && role.getRoleId().equals(bird.getRoleId())) {
                    result.add(bird);
                }
            } catch (NumberFormatException ignored) {
                // 非法 id 忽略
            }
        }
        return result;
    }

    /** 设置出战鸟：战斗时代替角色裸等级出战（与装备的鸟笼共同决定加成）。 */
    @PostMapping("/deploy")
    public String deploy(HttpSession session, Model model, @RequestParam Integer birdId) {
        Role role = getSessionRole(session);
        StorageBird bird = storageBirdService.getById(birdId);
        if (bird == null || !role.getRoleId().equals(bird.getRoleId())) {
            model.addAttribute("message", "小鸟不存在或不属于当前角色。");
            return list(session, model, "weight", null);
        }
        RoleCage cage = getCageSlot(role.getRoleId());
        cage.setBirdId(bird.getId());
        cage.setBaseBridId(bird.getBirdId());
        cageService.saveOrUpdate(cage);
        model.addAttribute("message", "已把它设为出战小鸟，战斗时它将为你而战。");
        return list(session, model, "weight", null);
    }

    // ==================== 私有工具 ====================

    /** 角色仓库小鸟（按重量倒序）。 */
    private List<StorageBird> listBirds(Role role) {
        LambdaQueryWrapper<StorageBird> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StorageBird::getRoleId, role.getRoleId());
        queryWrapper.orderByDesc(StorageBird::getWeight);
        return storageBirdService.list(queryWrapper);
    }

    /** 出售折算：每斤价 × 重量斤。 */
    private long sellGain(BaseBird baseBird, StorageBird bird) {
        long pricePerJin = baseBird == null || baseBird.getPrice() == null ? 0 : baseBird.getPrice();
        return pricePerJin * (bird.getWeight() / 100L);
    }

    /** 当前出战鸟 id（无鸟笼记录则 0）。 */
    private Integer getCagedBirdId(String roleId) {
        RoleCage cage = getCageSlot(roleId);
        return cage == null || cage.getBirdId() == null ? 0 : cage.getBirdId();
    }

    /** 出战栏位（role_cage idx=1），无则创建待插入对象。 */
    private RoleCage getCageSlot(String roleId) {
        LambdaQueryWrapper<RoleCage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleCage::getRoleId, roleId);
        wrapper.eq(RoleCage::getIdx, 1);
        RoleCage cage = cageService.getOne(wrapper);
        if (cage != null) {
            return cage;
        }
        RoleCage created = new RoleCage();
        created.setId(0);
        created.setRoleId(roleId);
        created.setIdx(1);
        created.setBirdId(0);
        created.setBaseBridId(0);
        created.setUseCageId(0);
        created.setUseCageNum0(0);
        return created;
    }
}
