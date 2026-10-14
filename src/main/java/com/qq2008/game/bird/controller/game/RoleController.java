package com.qq2008.game.bird.controller.game;

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

import com.qq2008.common.annotation.SkipRole;
import com.qq2008.common.entity.vo.MessageLink;
import com.qq2008.common.entity.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;

import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigGateway;
import com.qq2008.game.bird.model.dbo.Account;
import com.qq2008.game.bird.model.dbo.BaseTitle;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RoleBirth;
import com.qq2008.game.bird.model.dbo.RoleTrain;
import com.qq2008.game.bird.model.dbo.RoleTrap;
import com.qq2008.game.bird.util.UUIDUtils;

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


    /**
     * 创建角色界面
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/create")
    public String postCreateRole(Model model) {
        model.addAttribute("randNickName", "");
        return "role/create";
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
            return "role/create";
        }
        // 角色名长度和铭感词检测 todo
        // 获取账户信息
        Account account = (Account) session.getAttribute("account");
        // 检测角色数量
        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getUserId, account.getUserId());
        List<Role> roles = roleService.list(roleQueryWrapper);
        if (null != roles && !roles.isEmpty()) {
            model.addAttribute("message", MessageVO.error("您已经创建角色。"));
            return "role/create";
        }
        roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleName, roleName);
        roles = roleService.list(roleQueryWrapper);
        if (null != roles && !roles.isEmpty()) {
            model.addAttribute("message", MessageVO.error("角色名已被注册。"));
            return "role/create";
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
        return "common/message";
    }

    /**
     * 登陆角色
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/login")
    public String postLoginRole(HttpSession session, Model model) {
        // 获取已登录账号
        Account account = (Account) session.getAttribute("account");
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
        return "role/profile";
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
        // 检测是否存在
        // 更新并保存数据
        role.setRoleName(roleName);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功。"));
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
        // 更新并保存数据 todo
        role.setSex(sex);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addAttribute("message", MessageVO.success("设置成功。"));
        return "role/settingResult";
    }

    /**
     * 退出
     * @param session session
     * @return view
     */
    @RequestMapping(value = {"/quit"})
    public String quit(HttpSession session) {
        // 注销当前登录角色数据
        session.removeAttribute("sRole"); // 红线：session key 必须叫 sRole，原代码写成 role，等于没删干净
        // 注销当前登录账号数据
        session.removeAttribute("account");
        return redirectTo("/login");
    }

}
