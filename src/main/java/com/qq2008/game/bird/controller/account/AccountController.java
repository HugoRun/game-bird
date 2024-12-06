package com.qq2008.game.bird.controller.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qq2008.common.annotation.SkipAccount;
import com.qq2008.common.annotation.SkipRole;
import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.Account;
import com.qq2008.game.bird.service.IAccountService;
import com.qq2008.game.bird.util.UUIDUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***
 * 账号路由处理器
 * 对账号的处理使用的路由分支
 */
@Controller
public class AccountController extends BaseController {

    @Resource
    IAccountService accountService;

    /***
     * 登录界面
     * @param session session
     * @param model model
     * @return view
     */
    @SkipAccount
    @SkipRole
    @GetMapping(value = {"/login.do"})
    public String pageLogin(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (null == account) {
            return "account/login";
        }
        model.addAttribute("account", account);
        return redirectTo("/role/login.do");
    }

    /***
     * 登录
     * @param session session
     * @param model model
     * @param username 账号
     * @param password 密码
     * @return view
     */
    @SkipAccount
    @SkipRole
    @PostMapping(value = "/login.do")
    public String doLogin(HttpSession session, Model model, @RequestParam String username, @RequestParam String password) {
        // 参数校验
        if (null == username || username.isEmpty()) {
            model.addAttribute("errorMsg", "登陆失败,账号或密码错误。");
            return "account/login";
        }
        if (null == password || password.isEmpty()) {
            model.addAttribute("errorMsg", "登陆失败,账号或密码错误。");
            return "account/login";
        }
        // 数据库查询角色列表
        LambdaQueryWrapper<Account> accountQueryWrapper = new LambdaQueryWrapper<>();
        accountQueryWrapper.eq(Account::getUsername, username);
        Account account = accountService.getOne(accountQueryWrapper);
        // 账户不存在或者密码错误
        if (null == account || !password.equals(account.getPassword())) {
            model.addAttribute("errorMsg", "登陆失败,账号或密码错误。");
            return "account/login";
        }
        // 账户不存在或者密码错误
        if (account.getIsBan() == 1) {
            model.addAttribute("errorMsg", "账号已被封禁");
            return "account/login";
        }
        // 存储账户信息到session
        session.setAttribute("account", account);
        model.addAttribute("account", account);
        return redirectTo("/role/login.do");
    }

    /***
     * 注册账号界面
     * @param session session
     * @param model model
     * @return view
     */
    @SkipAccount
    @SkipRole
    @GetMapping(value = "/register.do")
    public String pageRegister(HttpSession session, Model model) {
        // 已禁止注册账号
        if (!ConstData.IS_ALLOW_REGISTER) {
            model.addAttribute("message", MessageVO.error("服务器已禁止注册。"));
            return "common/message";
        }
        Account account = (Account) session.getAttribute("account");
        if (null != account) {
            return redirectTo("/role/login.do");
        }
        return "account/register";
    }

    /***
     * 注册账号
     * @param session session
     * @param model model
     * @param username 账号
     * @param password 密码
     * @param passwordRe 重复密码
     * @return view
     */
    @SkipAccount
    @SkipRole
    @PostMapping(value = "/register.do")
    public String doRegister(HttpSession session, Model model, @RequestParam String username, @RequestParam String password, @RequestParam String passwordRe) {
        // 已禁止注册账号
        if (!ConstData.IS_ALLOW_REGISTER) {
            model.addAttribute("message", MessageVO.error("服务器已禁止注册。"));
            return "common/message";
        }
        // 用户名长度和敏感词检测 todo
        // 账号和密码长度和字符检测 todo
        // 参数校验
        if (null == username || username.isEmpty()) {
            model.addAttribute("errorMsg", "注册失败,账号或密码错误。");
            return "account/register";
        }
        if (null == password || password.isEmpty()) {
            model.addAttribute("errorMsg", "注册失败,账号或密码错误。");
            return "account/register";
        }
        if (null == passwordRe || passwordRe.isEmpty() || !password.equals(passwordRe)) {
            model.addAttribute("errorMsg", "注册失败,密码和重复密码不匹配。");
            return "account/register";
        }
        // 数据库查询角色列表
        LambdaQueryWrapper<Account> accountQueryWrapper = new LambdaQueryWrapper<>();
        accountQueryWrapper.eq(Account::getUsername, username);
        Account account = accountService.getOne(accountQueryWrapper);

        // 账户不存在或者密码错误
        if (null != account) {
            model.addAttribute("errorMsg", "注册失败,账号已存在。");
            return "account/register";
        }

        int nowTime = CommonUtils.nowTime();
        // 创建账号
        account = new Account();
        account.setUserId(UUIDUtils.genUserId("bird"));
        // 平台信息
        account.setPlatform("bird");
        account.setUsername(username);
        account.setPassword(password);
        // 基本信息
        account.setAvatarUrl("/assets/images/avatar/d.jpg");
        account.setNickname("无名氏"); //todo 随机用户名
        account.setCreateTime(nowTime);
        accountService.save(account);
        // 存储账户信息到session
        session.setAttribute("account", account);
        model.addAttribute("account", account);
        return redirectTo("/role/login.do");
    }

    /***
     * 修改密码界面
     * @return view
     */
    @SkipRole
    @GetMapping(value = "/changePassword.do")
    public String changePassword() {
        return "account/changePassword";
    }

    /***
     * 修改密码操作
     * @param oldPassword 旧密码
     * @param password1 新密码
     * @param password2 新密码重复
     * @return view
     */
    @SkipRole
    @PostMapping(value = "/changePassword.do")
    public String doChangePassword(@RequestParam String oldPassword, @RequestParam String password1, @RequestParam String password2) {
        return "account/changePassword";
    }
}
