package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qq2008.common.annotation.SkipRole;
import com.qq2008.common.entiy.vo.MessageLink;
import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.*;
import com.qq2008.game.bird.util.UUIDUtils;
import com.qq2008.game.bird.model.dbo.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/***
 * 角色
 */
@Controller
public class RoleController extends BaseController {

    /***
     * 创建角色界面
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/createRole.do")
    public String postCreateRole(Model model) {
        model.addAttribute("randNickName", "");
        return "role/createRole";
    }

    /***
     * 创建角色
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @PostMapping("/createRole.do")
    public String postCreateRole(HttpSession session, String roleName, Model model) {
        // 参数校验
        if (null == roleName || roleName.isEmpty()) {
            model.addAttribute("errorMsg", "角色名不能为空。");
            return "role/createRole";
        }
        // 角色名长度和铭感词检测 todo
        // 获取账户信息
        Account account = (Account) session.getAttribute("account");
        // 检测角色数量
        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getUserId, account.getUserId());
        List<Role> roles = roleService.list(roleQueryWrapper);
        if (null != roles && !roles.isEmpty()) {
            model.addAttribute("errorMsg", "您已经创建角色。");
            return "role/createRole";
        }
        roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleName, roleName);
        roles = roleService.list(roleQueryWrapper);
        if (null != roles && !roles.isEmpty()) {
            model.addAttribute("errorMsg", "角色名已被注册。");
            return "role/createRole";
        }
        // 创建角色
        int nowTime = CommonUtils.nowTimestamp();
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
        role.setAvatarUrl("/assets/images/avatar/d.jpg");
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
        MessageLink link = new MessageLink("开始游戏", "/game.do");
        MessageVO messageVO = MessageVO.success("恭喜！您已成功开通小鸟情人。");
        messageVO.addLink(link);
        messageVO.setGoBack(false);
        model.addAttribute("message", messageVO);
        return "common/message";
    }

    /***
     * 登陆角色
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @RequestMapping("/loginRole.do")
    public String postLoginRole(HttpSession session, Model model) {
        // 获取已登录账号
        Account account = (Account) session.getAttribute("account");
        // 查询账号关联的角色
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getUserId, account.getUserId());
        Role role = roleService.getOne(queryWrapper);
        // 没有角色跳转到创建角色界面
        if (null == role) {
            model.addAttribute("errorMsg", "未找到相关角色。");
            return redirectTo("/createRole.do");
        }
        // 写登录日志(更新登录时间戳)
        int nowTime = CommonUtils.nowTimestamp();
        role.setLastLoginTime(nowTime);
        roleService.saveOrUpdate(role);
        // 更新当前登录角色
        saveSessionRole(session, role);
        return redirectTo("/game.do");
    }

    // 个人详情界面 userDetail
    // 头像列表界面 userHead
    // 设置头像
    // 称号列表界面 userTitle
    // 设置称号
    // 设置短语界面
    // 设置短语

    /***
     * 退出
     * @param session session
     * @return view
     */
    @RequestMapping(value = {"/quit.do"})
    public String quit(HttpSession session) {
        // 注销当前登录角色数据
        session.removeAttribute("role");
        // 注销当前登录账号数据
        session.removeAttribute("account");
        return redirectTo("/login.do");
    }

}
