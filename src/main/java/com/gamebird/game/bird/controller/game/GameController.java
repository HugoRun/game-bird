package com.gamebird.game.bird.controller.game;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebird.common.annotation.SkipRole;
import com.gamebird.common.util.CommonUtils;

import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseField;
import com.gamebird.game.bird.model.dbo.Board;
import com.gamebird.game.bird.model.dbo.LogChat;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleBirth;
import com.gamebird.game.bird.model.dbo.RoleCage;
import com.gamebird.game.bird.model.dbo.RoleTrain;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.model.dbo.Task;
import com.gamebird.game.bird.service.RoleCageService;
import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.model.vo.ChatMessageVO;
import com.gamebird.game.bird.model.vo.RoleBirthVO;
import com.gamebird.game.bird.model.vo.RoleTrainVO;
import com.gamebird.game.bird.model.vo.RoleTrapVO;
import com.gamebird.game.bird.service.BoardService;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.TaskService;
import com.gamebird.game.bird.service.impl.SignInService;
import com.gamebird.game.bird.util.GameUtils;

/**
 * 游戏 todo
 * 1. 当前人数可以放在缓存中, 玩家注册角色后触发刷新
 * 2. 最近加入玩家可以放在缓存中
 * 3. 汽车配置可以存放再缓存中，定时器或者后台手动重新加载
 */

@Controller
public class GameController extends BaseController {

    /** 游戏配置读取入口，由构造器注入。 */
    @Resource
    public GameConfigGateway configGateway;

    /** 公告（主页公告栏数据源）。 */
    @Resource
    public BoardService boardService;

    /** 任务（主页任务位数据源）。 */
    @Resource
    public TaskService taskService;

    /** 游戏事件（树林记录 / 世界动态数据源）。 */
    @Resource
    public GameEventService gameEventService;

    /** 每日签到（主页福利位状态）。 */
    @Resource
    public SignInService signInService;

    /** 出战栏位（入侵守卫用）。 */
    @Resource
    public RoleCageService cageService;

    /** 图鉴（角色页统计）。 */
    @Resource
    public com.gamebird.game.bird.service.impl.CollectionService collectionService;

    /** 战斗记录（角色页统计）。 */
    @Resource
    public com.gamebird.game.bird.service.FightLogService fightLogService;

    /** 游戏参数（仓库上限）。 */
    @Resource
    public com.gamebird.game.bird.config.GameProperties gameProperties;
    // 日志句柄

    /**
     * 游戏主界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping(value = {"/", "/game"})
    public String game(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        // 当前角色数量
        model.addAttribute("roleCount", roleService.count());
        //
        model.addAttribute("nowTime", CommonUtils.nowTime());

        // 金蛋数据
        // 徒弟数据
        // 婚姻数据
        // 戒指数据
        // 主线任务数据
        // 日常任务数据
        // 公告数据
        // 奖励数据
        // 福利数据

        // 陷阱数据
        LambdaQueryWrapper<RoleTrap> trapQueryWrapper = new LambdaQueryWrapper<>();
        trapQueryWrapper.eq(RoleTrap::getRoleId, role.getRoleId());
        trapQueryWrapper.orderByAsc(RoleTrap::getIdx);
        List<RoleTrap> trapList1 = trapService.list(trapQueryWrapper);
        //
        List<RoleTrapVO> trapList = new ArrayList<>();
        for (RoleTrap trap : trapList1) {
            RoleTrapVO trapVO = new RoleTrapVO();
            BeanUtils.copyProperties(trap, trapVO);
            trapVO.setUseTrap(configGateway.getBaseTrap(trap.getUseTrapId() == 0 ? ConstData.DEFAULT_TRAP_ID : trap.getUseTrapId()));
            if (trap.getCatchBirdId() > 0) {
                trapVO.setCatchBird(configGateway.getBaseBird(trap.getCatchBirdId()));
            }
            trapList.add(trapVO);
        }
        model.addAttribute("trapList", trapList);
        // 孵化器数据
        LambdaQueryWrapper<RoleBirth> birthQueryWrapper = new LambdaQueryWrapper<>();
        birthQueryWrapper.eq(RoleBirth::getRoleId, role.getRoleId());
        birthQueryWrapper.orderByAsc(RoleBirth::getIdx);
        List<RoleBirth> birthList1 = birthService.list(birthQueryWrapper);
        //
        List<RoleBirthVO> birthList = new ArrayList<>();
        for (RoleBirth trap : birthList1) {
            RoleBirthVO trapVO = new RoleBirthVO();
            BeanUtils.copyProperties(trap, trapVO);
            trapVO.setUseNest(configGateway.getBaseNest(trap.getUseNestId() == 0 ? ConstData.DEFAULT_NEST_ID : trap.getUseNestId()));
            if (trap.getBirdBaseId() > 0) {
                trapVO.setBaseBird(configGateway.getBaseBird(trap.getBirdBaseId()));
            }
            birthList.add(trapVO);
        }
        model.addAttribute("birthList", birthList);
        // 训练场数据
        LambdaQueryWrapper<RoleTrain> trainQueryWrapper = new LambdaQueryWrapper<>();
        trainQueryWrapper.eq(RoleTrain::getRoleId, role.getRoleId());
        trainQueryWrapper.orderByAsc(RoleTrain::getIdx);
        List<RoleTrain> trainList1 = trainService.list(trainQueryWrapper);
        //
        List<RoleTrainVO> trainList = new ArrayList<>();
        for (RoleTrain trap : trainList1) {
            RoleTrainVO trainVO = new RoleTrainVO();
            BeanUtils.copyProperties(trap, trainVO);
            trainVO.setUseTrain(configGateway.getBaseTrain(trap.getUseTrainId() == 0 ? ConstData.DEFAULT_TRAIN_ID : trap.getUseTrainId()));
            if (trap.getBirdBaseId() > 0) {
                trainVO.setBaseBird(configGateway.getBaseBird(trap.getBirdBaseId()));
            }
            trainList.add(trainVO);
        }
        model.addAttribute("trainList", trainList);
        // 世界聊天数据
        LambdaQueryWrapper<LogChat> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.gt(LogChat::getTime, CommonUtils.nowDate() - ConstData.CHAT_TIME * 86400);
        queryWrapper2.orderByDesc(LogChat::getTime);
        queryWrapper2.last("LIMIT 5");
        List<LogChat> chatMessageList = chatService.list(queryWrapper2);
        model.addAttribute("chatMessageList", chatMessageList);
        // 树林记录：个人事件流（log_tree 按角色过滤），原 mock 数据源已被真实事件替代
        model.addAttribute("treeEvents", gameEventService.pageByRole(role.getRoleId(), 1).getRecords());
        // 世界动态：全服事件流（log_tree 不过滤角色）
        model.addAttribute("globalEvents", gameEventService.latestGlobal(5));
        // 主页公告栏：最新一条公告
        model.addAttribute("latestBoard", boardService.list(
                new LambdaQueryWrapper<Board>().orderByDesc(Board::getId).last("LIMIT 1")).stream().findFirst().orElse(null));
        // 主页任务位：进行中/可领取的最新任务
        model.addAttribute("activeTask", taskService.list(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getRoleId, role.getRoleId())
                        .in(Task::getStatus, 0, 1)
                        .orderByDesc(Task::getId)
                        .last("LIMIT 1")).stream().findFirst().orElse(null));
        // 福利位：今天是否已签到
        model.addAttribute("signedToday", signInService.isSignedToday(role.getRoleId()));
        // 仓库余量（满仓时收网会被拒，提前让玩家看到）
        long warehouseCount = storageBirdService.count(
                new LambdaQueryWrapper<StorageBird>().eq(StorageBird::getRoleId, role.getRoleId()));
        model.addAttribute("warehouseCount", warehouseCount);
        model.addAttribute("warehouseCapacity", gameProperties.getBirdStorageCapacity());
        model.addAttribute("warehouseFull", warehouseCount >= gameProperties.getBirdStorageCapacity());
        // 诱饵余量（主页空闲陷阱提示）
        long baitTotal = packService.list(new LambdaQueryWrapper<RolePack>()
                .eq(RolePack::getRoleId, role.getRoleId())
                .eq(RolePack::getTypeId, ConstData.PACK_TYPE_BAIT)
                .gt(RolePack::getNum, 0)).stream().mapToLong(p -> p.getNum() == null ? 0 : p.getNum()).sum();
        model.addAttribute("baitTotal", baitTotal);
        // 小鸟入侵（守卫 PVE）：以 2 小时为窗口做确定性判定，无需定时器。
        // 同一窗口内每个角色的入侵状态固定；击退后写固定文案事件防重复。
        int windowIndex = CommonUtils.nowTime() / INVASION_WINDOW_SECONDS;
        int windowStart = windowIndex * INVASION_WINDOW_SECONDS;
        boolean invasionActive = Math.abs((role.getRoleId() + "_" + windowIndex).hashCode()) % 10 < INVASION_CHANCE;
        boolean invasionDefended = gameEventService.hasEventSince(role.getRoleId(),
                INVASION_DEFENDED_MESSAGE, windowStart);
        model.addAttribute("invasionActive", invasionActive && !invasionDefended);
        model.addAttribute("invasionEndTime", windowStart + INVASION_WINDOW_SECONDS);
        return ConstData.VIEW_GAME;
    }

    /** 入侵窗口时长（2 小时）。 */
    public static final int INVASION_WINDOW_SECONDS = 2 * 3600;
    /** 入侵出现概率（按窗口与角色 hash 判定，30%）。 */
    public static final int INVASION_CHANCE = 3;
    /** 击退入侵的固定文案（写入与识别必须同源，勿改字面量）。 */
    public static final String INVASION_DEFENDED_MESSAGE = "击退了猪头大军的入侵";

    /**
     * 迎战入侵的猪头大军：用出战鸟（或角色裸等级）防守，胜利得金币与钻石。
     * 同一窗口只能迎战一次（以固定文案事件为凭证）。
     */
    @PostMapping("/game/defend")
    public String defend(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        int now = CommonUtils.nowTime();
        int windowIndex = now / INVASION_WINDOW_SECONDS;
        int windowStart = windowIndex * INVASION_WINDOW_SECONDS;
        boolean invasionActive = Math.abs((role.getRoleId() + "_" + windowIndex).hashCode()) % 10 < INVASION_CHANCE;
        if (!invasionActive) {
            model.addAttribute("message", MessageVO.error("现在没有入侵，安心抓鸟吧。"));
            return ConstData.VIEW_MESSAGE;
        }
        if (gameEventService.hasEventSince(role.getRoleId(), INVASION_DEFENDED_MESSAGE, windowStart)) {
            model.addAttribute("message", MessageVO.warn("本窗口的猪头大军已经被你击退过了。"));
            return ConstData.VIEW_MESSAGE;
        }
        // 双方战力：我方 = 出战鸟 + 角色等级（同战斗公式）；猪头 = 角色等级×10 ± 20%
        StorageBird myBird = getDefendBird(role.getRoleId());
        int myPower = (role.getLevel() == null ? 0 : role.getLevel()) * 10;
        if (myBird != null) {
            myPower += (myBird.getLevel() == null ? 1 : myBird.getLevel()) * 20
                    + (myBird.getStar() == null ? 0 : myBird.getStar()) * 50
                    + (myBird.getGrow() == null ? 0 : myBird.getGrow()) / 10;
        }
        int pigPower = Math.max(10, (int) (myPower * (0.8 + CommonUtils.randNum(0, 40) / 100.0)));
        int winRate = Math.max(15, Math.min(85, myPower * 100 / Math.max(1, myPower + pigPower)));
        boolean win = CommonUtils.randNum(1, 100) <= winRate;
        MessageVO message;
        if (win) {
            long coin = 50 + (role.getLevel() == null ? 1 : role.getLevel()) * 20L;
            int diamond = CommonUtils.randNum(3, 10);
            roleValueService.addRoleCoin(role, coin);
            roleValueService.addRoleDiamond(role, diamond);
            roleService.updateById(role);
            saveSessionRole(session, role);
            gameEventService.record(role.getRoleId(), INVASION_DEFENDED_MESSAGE
                    + "，缴获金币" + coin + "、钻石" + diamond + "。");
            message = MessageVO.success("你率出战鸟击退了猪头大军！（胜率 " + winRate + "%）缴获金币 " + coin
                    + "、钻石 " + diamond + "。");
        } else {
            gameEventService.record(role.getRoleId(), "没能挡住猪头大军，它们抢走了一些诱饵扬长而去。");
            message = MessageVO.warn("战败了……猪头大军抢走了一些诱饵（无其他损失）。（胜率 " + winRate
                    + "%）去训练场练练鸟再来复仇吧。");
        }
        message.addLink(new MessageLink("回主页", "/game"));
        model.addAttribute("message", message);
        return ConstData.VIEW_MESSAGE;
    }

    /** 防守用的出战鸟（无则空手应战）。 */
    private StorageBird getDefendBird(String roleId) {
        LambdaQueryWrapper<RoleCage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleCage::getRoleId, roleId);
        wrapper.eq(RoleCage::getIdx, 1);
        RoleCage cage = cageService.getOne(wrapper);
        if (cage == null || cage.getBirdId() == null || cage.getBirdId() <= 0) {
            return null;
        }
        StorageBird bird = storageBirdService.getById(cage.getBirdId());
        return bird != null && roleId.equals(bird.getRoleId()) ? bird : null;
    }

    /**
     * 树林记录页：个人事件流分页（收网/孵化/训练/战斗）。
     */
    @GetMapping("/game/tree")
    public String tree(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        Role role = getSessionRole(session);
        model.addAttribute("eventPage", gameEventService.pageByRole(role.getRoleId(), pageNo));
        return "game/tree";
    }

    /**
     * 场景切换页：列出全部场景，标记当前所在。
     */
    @GetMapping("/game/field")
    public String pageFieldList(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        // base_field 每个场景有两个 id（奇数 id 才被鸟/饵引用），按名字去重，
        // 修复：切换页同一场景渲染两个相同实例
        java.util.LinkedHashMap<String, BaseField> unique = new java.util.LinkedHashMap<>();
        for (BaseField f : configGateway.getBaseFieldList()) {
            unique.putIfAbsent(f.getName() == null ? String.valueOf(f.getId()) : f.getName(), f);
        }
        model.addAttribute("fields", unique.values());
        // 「当前所在」按场景名比较：兼容历史角色落在孪生 id 上的情况
        BaseField current = configGateway.getBaseField(role.getFieldId() == null ? 0 : role.getFieldId().intValue());
        model.addAttribute("currentFieldName", current == null ? "" : current.getName());
        return "field/fieldList";
    }

    /**
     * 切换场景（POST 写操作）。场景决定可用的诱饵区域（base_bait.lb）
     * 与主场捕捉加成（base_bird.field）。
     */
    @PostMapping("/game/field")
    public String doChangeField(HttpSession session, RedirectAttributes model, @RequestParam(defaultValue = "0") int fieldId) {
        Role role = getSessionRole(session);
        BaseField baseField = configGateway.getBaseField(fieldId);
        if (baseField == null) {
            model.addFlashAttribute("message", MessageVO.error("没有这个场景。"));
            return redirectTo("/game/field");
        }
        if (baseField.getId() != null && baseField.getId().intValue() == (role.getFieldId() == null ? 0 : role.getFieldId())) {
            model.addFlashAttribute("message", MessageVO.warn("你已经在这里了。"));
            return redirectTo("/game/field");
        }
        role.setFieldId(baseField.getId().shortValue());
        roleService.updateById(role);
        saveSessionRole(session, role);
        model.addFlashAttribute("message", MessageVO.success("你来到了【" + baseField.getName() + "】。这里的饵料与鸟群和别处不同。"));
        return redirectTo("/game");
    }

    /**
     * 银行界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/bank")
    public String pageBank(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        model.addAttribute("bankLimit", bankLimit(role));
        return "role/bank";
    }

    /** 存款上限：10000 × (1 + VIP 等级)。 */
    private long bankLimit(Role role) {
        return 10000L * (1 + (role.getVip() == null ? 0 : role.getVip()));
    }

    /**
     * 存款
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/bankIn")
    public String doBankIn(HttpSession session, RedirectAttributes model, @RequestParam(defaultValue = "0") int inNum) {
        Role role = getSessionRole(session);
        // 100金币起存
        if (inNum < 100) {
            model.addFlashAttribute("message", MessageVO.error("100金币起存。"));
            return redirectTo("bank");
        }
        if (inNum > role.getCoin()) {
            model.addFlashAttribute("message", MessageVO.error("您的现金不足。"));
            return redirectTo("bank");
        }
        // 存款上限：10000 × (1 + VIP)，超过则拒绝
        long limit = bankLimit(role);
        if (role.getBankCoin() + inNum > limit) {
            model.addFlashAttribute("message", MessageVO.error("超过存款额度上限（" + limit + "金币），VIP 等级越高额度越大。"));
            return redirectTo("bank");
        }
        int addBankCoin = inNum * 99 / 100;
        // 更新并保存数据 todo
        costRoleCoin(role, (long) inNum);
        role.setBankCoin(role.getBankCoin() + addBankCoin);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addFlashAttribute("message", MessageVO.success("存款成功。<br />本次存入现金" + addBankCoin + ", 银行收取了" + (inNum - addBankCoin) + "的手续费"));
        return redirectTo("bank");
    }

    /**
     * 取款
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/bankOut")
    public String doBankOut(HttpSession session, RedirectAttributes model, @RequestParam(defaultValue = "0") int outNum) {
        Role role = getSessionRole(session);
        // 检测陷阱信息
        if (outNum > role.getBankCoin()) {
            model.addFlashAttribute("message", MessageVO.error("银行余额不足。"));
            return redirectTo("bank");
        }
        if (outNum == 0) {
            model.addFlashAttribute("message", MessageVO.error("取款金额错误。"));
            return redirectTo("bank");
        }
        // 查询已拥有的诱饵数据
        role.setBankCoin(role.getBankCoin() - outNum);
        addRoleCoin(role, (long) outNum);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addFlashAttribute("message", MessageVO.success("取款成功。"));
        return redirectTo("bank");
    }

    /**
     * 下饵界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/field/info")
    public String pageField(HttpSession session, Model model, @RequestParam(defaultValue = "0") int fieldId) {
        BaseField baseField = configGateway.getBaseField(fieldId);
        model.addAttribute("baseField", baseField);
        return "field/fieldInfo";
    }

    // ========== 待实现功能备忘（非死代码，勿删） ==========
    // 下列条目是作者列出的功能规划清单，没有对应实现，属于产品待办而非废弃代码。
    // 一键下饵
    // 收获 playFinish
    // 选择场地界面 PackSelectTrap
    // 选择场地

    // 日志 eventLogs
    // 日志 eventChat
    // 信件 eventMessage
    // 信件 eventOut
    // 日志 eventAll
    // 通知 eventNotice
    // 通知 eventSyslog

    // 入侵 invadeInformation
    // 抽奖 invadeAward

    // 任务

    // 阿凡达 taskAfd
    // 万圣塔 taskTower

    // 称号列表
    // 装备称号
    // 修改名称
    // 区域信心
    // 修改区域
    // 选择下饵饵料
    // 下饵
    // 陷阱信息
    // 更换陷阱
    // 强化陷阱
    // 陷阱加速
    // 随别人陷阱祝福
    // 收网
    // 重置小鸟变异属性
    // 更改小鸟性别
    // 重命名小鸟

    /**
     * 角色详情页
     *
     * <p>★ 本方法位于根路径 {@code /profile}，与 {@link RoleController} 的
     * {@code /role/profile} 是<b>同一个功能的两条路径</b>，二者都渲染
     * {@code role/profile} 视图。本类没有类级 {@code @RequestMapping}，所以路径落在根上；
     * 两者技术上的不冲突，靠的是这一层路径前缀差异。
     *
     * <p>之所以不合并成一条：删除其一属于路由行为变更（外部可能已有链接指向它），
     * 不在本轮「仅去后缀」的授权范围内。<b>后人若要合并，务必同时改掉模板中的
     * 对应引用</b>，否则会产出死链。
     *
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/profile")
    public String pageProfile(HttpSession session, Model model) {
        fillProfileStats(session, model);
        return ConstData.VIEW_ROLE_PROFILE;
    }

    /** 角色页统计：图鉴收集、战斗战绩、最佳宠物（与 RoleController 的 /role/profile 同视图）。 */
    private void fillProfileStats(HttpSession session, Model model) {
        Role role = getSessionRole(session);
        model.addAttribute("collectedCount", collectionService.countCollected(role.getRoleId()));
        long total = fightLogService.count(new LambdaQueryWrapper<com.gamebird.game.bird.model.dbo.FightLog>()
                .eq(com.gamebird.game.bird.model.dbo.FightLog::getRoleId, role.getRoleId()));
        long wins = fightLogService.count(new LambdaQueryWrapper<com.gamebird.game.bird.model.dbo.FightLog>()
                .eq(com.gamebird.game.bird.model.dbo.FightLog::getRoleId, role.getRoleId())
                .eq(com.gamebird.game.bird.model.dbo.FightLog::getResult, 1));
        model.addAttribute("fightTotal", total);
        model.addAttribute("fightWins", wins);
        model.addAttribute("winRate", total == 0 ? 0 : wins * 100 / total);
        com.gamebird.game.bird.model.dbo.StorageBird best = storageBirdService.getOne(
                new LambdaQueryWrapper<com.gamebird.game.bird.model.dbo.StorageBird>()
                        .eq(com.gamebird.game.bird.model.dbo.StorageBird::getRoleId, role.getRoleId())
                        .orderByDesc(com.gamebird.game.bird.model.dbo.StorageBird::getWeight)
                        .last("LIMIT 1"));
        model.addAttribute("bestBird", best);
        model.addAttribute("bestBirdName", best == null ? "" : (configGateway.getBaseBird(best.getBirdId()) == null
                ? "神秘小鸟" : configGateway.getBaseBird(best.getBirdId()).getName()));
    }

    /**
     * 充值界面
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/pay")
    public String pagePay(HttpSession session, Model model) {
        return "vip/pay";
    }

    /**
     * 公告消息接口, 用于转发
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping(value = {ConstData.MESSAGE_PATH})
    public String message(HttpSession session, Model model) {
        return ConstData.VIEW_MESSAGE;
    }
}
