package com.gamebird.game.bird.controller.game;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gamebird.common.annotation.SkipRole;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.common.util.UUIDUtils;

import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.Account;
import com.gamebird.game.bird.model.dbo.BaseTitle;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleBirth;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.RoleTrain;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.service.GameEventService;
import com.gamebird.game.bird.service.impl.QuestService;
import com.gamebird.game.bird.service.TaskTriggerService;

/**
 * 角色
 *
 * <p><b>路由命名（RT-3）</b>：本类 16 条映射去掉了历史遗留的路由后缀，
 * 并把「设置页」与「设置动作」收敛到同一路径、靠 HTTP method 区分：
 * {@code GET /role/avatar} 列出 19 个头像，{@code POST /role/avatar} 落库；
 * {@code GET /role/title} 列出称号，{@code POST /role/title} 落库。
 *
 * <p>原先的「设置头像 2」「设置称号 2」两条路径是历史遗留的分裂 ——
 * 同一功能的「进入页」与「提交」被拆成两条路径，只有后缀可区分。
 * 它们同时是<b>GET 写操作</b>（会 {@code roleService.saveOrUpdate}），本次一并转为 POST。
 * 合并后误用 {@code <a href>} 只会 GET 到设置页而不会改数据，比原先更安全。
 *
 * <p>{@code /quit} 刻意保留不限定 method 的 {@code @RequestMapping}，
 * 改 method 语义不属于本轮的路由后缀范围。
 */
@RequestMapping("/role")
@Controller
public class RoleController extends BaseController {

    /** 游戏配置读取入口，由构造器注入。 */
    @Resource
    public GameConfigGateway configGateway;

    /** 任务触发器（每日登录任务）。 */
    @Resource
    public TaskTriggerService taskTriggerService;

    /** 游戏事件（树林记录/一次性标记）。 */
    @Resource
    public GameEventService gameEventService;

    /** 奋进目标引擎。 */
    @Resource
    public QuestService questService;

    /** 图鉴（角色页统计）。 */
    @Resource
    public com.gamebird.game.bird.service.impl.CollectionService collectionService;

    /** 战斗记录（角色页统计）。 */
    @Resource
    public com.gamebird.game.bird.service.FightLogService fightLogService;


    /**
     * 创建角色界面
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/create")
    public String getCreateRole(Model model) {
        model.addAttribute("randNickName", "");
        return ConstData.VIEW_ROLE_CREATE;
    }

    /**
     * 创建角色
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @PostMapping("/create")
    public String postCreateRole(HttpSession session, String roleName, Model model) {
        // 参数校验
        if (null == roleName || roleName.isEmpty()) {
            model.addAttribute("message", MessageVO.error("角色名不能为空。"));
            return ConstData.VIEW_ROLE_CREATE;
        }
        // 角色名长度和铭感词检测 todo
        // 获取账户信息（本方法标注 @SkipRole，不受 RoleAuthInterceptor 保护，
        // 但理论上仍受 AccountAuthInterceptor 保护；此处显式判空，避免拦截器
        // 顺序变动或旁路时直接 NPE）
        Account account = (Account) session.getAttribute("account");
        if (null == account) {
            return redirectTo("/login");
        }
        // 检测角色数量
        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getUserId, account.getUserId());
        List<Role> roles = roleService.list(roleQueryWrapper);
        if (null != roles && !roles.isEmpty()) {
            model.addAttribute("message", MessageVO.error("您已经创建角色。"));
            return ConstData.VIEW_ROLE_CREATE;
        }
        roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleName, roleName);
        roles = roleService.list(roleQueryWrapper);
        if (null != roles && !roles.isEmpty()) {
            model.addAttribute("message", MessageVO.error("角色名已被注册。"));
            return ConstData.VIEW_ROLE_CREATE;
        }
        // 创建角色
        int nowTime = CommonUtils.nowTime();
        Role role = new Role();
        role.setUserId(account.getUserId());
        role.setRoleId(UUIDUtils.genRoleId());
        role.setRoleName(roleName);
        role.setFieldId(ConstData.DEFAULT_FIELD_ID);
        role.setLevel(1);
        role.setExp(0L);
        role.setCoin(10000L);
        role.setDiamond(0);
        role.setLovePoint(0L);
        role.setLastLoginTime(nowTime);
        role.setRegTime(nowTime);
        role.setSex((byte) 0);
        role.setAvatarUrl("/assets/images/head/1.png");
        roleService.save(role);
        // 保存角色到缓存
        saveSessionRole(session, role);
        // 初始化陷阱
        RoleTrap roleTrap1 = new RoleTrap();
        roleTrap1.setId(0);
        roleTrap1.setRoleId(role.getRoleId());
        roleTrap1.setIdx(1);
        trapService.save(roleTrap1);
        // 初始化训练场
        RoleTrain roleTrain1 = new RoleTrain();
        roleTrain1.setId(0);
        roleTrain1.setRoleId(role.getRoleId());
        roleTrain1.setIdx(1);
        trainService.save(roleTrain1);
        // 初始化孵化场
        RoleBirth roleBirth1 = new RoleBirth();
        roleBirth1.setId(0);
        roleBirth1.setRoleId(role.getRoleId());
        roleBirth1.setIdx(1);
        birthService.save(roleBirth1);
        // 赠送一张改名卡与一张性别卡（改名/换性别从此需要卡片）
        grantStarterProp(role.getRoleId(), 152);
        grantStarterProp(role.getRoleId(), 153);
        //
        MessageLink link = new MessageLink("开始游戏", "/game");
        MessageVO messageVO = MessageVO.success("""
                青鸟之神创造这世界以后就沉睡去了…
                善良的小鸟们创立了璀璨的文明，快乐祥和地生活着。
                有一天，邪恶的猪头大军突然出现在梦想花园，将小鸟们赶出了自己的家园。小鸟们一路败退，直至最后的防线--彩虹森林。
                大祭司[球球]不忍看到子民受苦，以生命为代价召唤了来自异界的救世主，就是你。
                打败邪恶的猪头大军，唤醒青鸟之神拯救善良可爱的小鸟们，一切取决于你的选择
                """);
        messageVO.addLink(link);
        messageVO.setGoBack(false);
        model.addAttribute("message", messageVO);
        return ConstData.VIEW_MESSAGE;
    }

    /**
     * 登陆角色
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/login")
    public String getLoginRole(HttpSession session, Model model) {
        // 获取已登录账号（@SkipRole，显式判空避免 NPE）
        Account account = (Account) session.getAttribute("account");
        if (null == account) {
            return redirectTo("/login");
        }
        // 查询账号关联的角色
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getUserId, account.getUserId());
        Role role = roleService.getOne(queryWrapper);
        // 没有角色跳转到创建角色界面
        if (null == role) {
            model.addAttribute("message", MessageVO.error("未找到相关角色。"));
            return redirectTo("create");
        }
        // 写登录日志(更新登录时间戳)
        int nowTime = CommonUtils.nowTime();
        role.setLastLoginTime(nowTime);
        roleService.saveOrUpdate(role);
        // 每日登录任务触发（当天首次进入创建已完成的登录任务）
        taskTriggerService.onLogin(role);
        // 银行每日利息：余额的 0.1%（封顶 1000），每天首次进入游戏发放
        grantDailyInterest(role, session);
        // 奋进目标：每日登录类事件
        questService.onEvent(role, QuestService.EV_LOGIN, 1);
        // 更新当前登录角色
        saveSessionRole(session, role);
        return redirectTo("/game");
    }

    /**
     * 角色信息界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/profile")
    public String pageProfile(HttpSession session, Model model) {
        fillProfileStats(session, model);
        return ConstData.VIEW_ROLE_PROFILE;
    }

    /** 角色页统计：图鉴收集、战斗战绩、最佳宠物（与 GameController 的 /profile 同视图）。 */
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
     * 设置头像界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/avatar")
    public String pageChangeAvatar(HttpSession session, Model model) {
        return "role/changeAvatar";
    }

    /**
     * 设置头像
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/avatar")
    public String doChangeAvatar(HttpSession session, Model model, @RequestParam(defaultValue = "1") int img) {
        Role role = getSessionRole(session);
        // 头像配置错误
        if (img > 19 || img < 1) {
            model.addAttribute("message", MessageVO.error("没有该头像。"));
            return "role/settingResult";
        }
        // 更新头像并保存数据
        role.setAvatarUrl("/assets/images/head/" + img + ".png");
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功。"));
        return "role/settingResult";
    }

    /**
     * 设置称号界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/title")
    public String pageChangeTitle(HttpSession session, Model model) {
        List<BaseTitle> titleList = configGateway.getBaseTitleList();
        model.addAttribute("baseTitleList", titleList);
        return "role/changeTitle";
    }

    /**
     * 设置称号
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/title")
    public String doChangeTitle(HttpSession session, Model model, @RequestParam(defaultValue = "1") int titleId) {
        Role role = getSessionRole(session);
        // 称号配置错误
        BaseTitle baseTitle = configGateway.getBaseTitle(titleId);
        if (baseTitle == null) {
            model.addAttribute("message", MessageVO.error("称号配置错误。"));
            return "role/settingResult";
        }
        // 没有该称号 todo
        // 更新头像并保存数据
        role.setTitleId(titleId);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功。"));
        return "role/settingResult";
    }

    /**
     * 设置心情界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/dynamic")
    public String pageChangeDynamic(HttpSession session, Model model) {
        return "role/changeDynamic";
    }

    /**
     * 设置心情
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/dynamic")
    public String doChangeDynamic(HttpSession session, Model model, String dynamic) {
        Role role = getSessionRole(session);
        // 心情为空
        if (dynamic == null || dynamic.isEmpty()) {
            model.addAttribute("message", MessageVO.error("心情不能为空。"));
            return "role/settingResult";
        }
        // 检测心情长度和铭感词 todo
        // 更新头像并保存数据
        role.setDynamic(dynamic);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功。"));
        return "role/settingResult";
    }

    /**
     * 设置名称界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/name")
    public String pageChangeName(HttpSession session, Model model) {
        return "role/changeName";
    }

    /**
     * 设置名称
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/name")
    public String doChangeName(HttpSession session, Model model, String roleName) {
        Role role = getSessionRole(session);
        // 名称不能为空
        if (roleName == null || roleName.isEmpty()) {
            model.addAttribute("message", MessageVO.error("名称不能为空。"));
            return "role/settingResult";
        }
        // 检测名称长度和铭感词 todo

        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleName, roleName);
        if (roleService.getOne(queryWrapper) != null){
            model.addAttribute("message", MessageVO.error("该名称已被使用。"));
            return "role/settingResult";
        }
        // 改名需消耗一张改名卡（base_prop 152），建角时赠送一张
        if (!consumeProp(role, 152, "改名卡")) {
            model.addAttribute("message", MessageVO.error("改名需要消耗一张改名卡，请到商店购买。"));
            return "role/settingResult";
        }
        // 更新并保存数据
        role.setRoleName(roleName);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功，消耗了一张改名卡。"));
        return "role/settingResult";
    }

    /**
     * 设置性别界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/sex")
    public String pageChangeSex(HttpSession session, Model model) {
        return "role/changeSex";
    }

    /**
     * 设置性别
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/sex")
    public String doChangeSex(HttpSession session, Model model, @RequestParam(defaultValue = "0") Byte sex) {
        Role role = getSessionRole(session);
        // 性别错误
        if (sex != 0 && sex != 1 && sex != 2) {
            model.addAttribute("message", MessageVO.error("性别错误。"));
            return "role/settingResult";
        }
        // 更换性别需消耗一张性别更改卡（base_prop 153），建角时赠送一张
        if (!consumeProp(role, 153, "性别更改卡")) {
            model.addAttribute("message", MessageVO.error("更换性别需要消耗一张性别更改卡，请到商店购买。"));
            return "role/settingResult";
        }
        role.setSex(sex);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功，消耗了一张性别更改卡。"));
        return "role/settingResult";
    }

    /**
     * 消耗一张指定道具（改名卡/性别卡等）。
     *
     * @return true 表示已扣除；false 表示包裹里没有
     */
    private boolean consumeProp(Role role, int basePropId, String displayName) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        wrapper.eq(RolePack::getBaseId, basePropId);
        wrapper.gt(RolePack::getNum, 0);
        RolePack pack = packService.getOne(wrapper);
        if (pack == null) {
            return false;
        }
        roleValueService.costPackNum(pack, 1);
        packService.saveOrUpdate(pack);
        return true;
    }

    /**
     * 退出
     * @param session session
     * @return view
     */
    @RequestMapping(value = {"/quit"})
    public String quit(HttpSession session) {
        // 注销当前登录角色数据
        session.removeAttribute(ConstData.SESSION_ROLE_KEY); // 红线：session key 必须叫 sRole，原代码写成 role，等于没删干净
        // 注销当前登录账号数据
        session.removeAttribute("account");
        return redirectTo("/login");
    }

    /** 利息事件的固定前缀（发放与识别必须同源，勿改字面量）。 */
    private static final String INTEREST_PREFIX = "银行发放了利息";

    /** 每日利息：今天没发过且余额大于 0 时发放 0.1%（封顶 1000）。 */
    private void grantDailyInterest(Role role, HttpSession session) {
        try {
            if (role.getBankCoin() == null || role.getBankCoin() <= 0) {
                return;
            }
            int dayStart = (int) (java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"))
                    .atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toEpochSecond());
            if (gameEventService.hasEventSincePrefix(role.getRoleId(), INTEREST_PREFIX, dayStart)) {
                return;
            }
            long interest = Math.min(1000, role.getBankCoin() / 1000);
            if (interest <= 0) {
                return;
            }
            role.setBankCoin(role.getBankCoin() + interest);
            roleService.updateById(role);
            saveSessionRole(session, role);
            gameEventService.record(role.getRoleId(),
                    INTEREST_PREFIX + interest + "金币（余额" + role.getBankCoin() + "）。");
        } catch (Exception ignored) {
            // 利息旁路失败不影响登录
        }
    }

    /**
     * 建角赠送一张指定道具卡片。
     */
    private void grantStarterProp(String roleId, int basePropId) {
        RolePack pack = new RolePack();
        pack.setId(0);
        pack.setRoleId(roleId);
        pack.setTypeId(ConstData.PACK_TYPE_PROP);
        pack.setBaseId(basePropId);
        pack.setNum(1);
        packService.save(pack);
    }

}
