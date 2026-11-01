package com.gamebird.game.bird.controller.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gamebird.common.annotation.SkipAccount;
import com.gamebird.common.annotation.SkipRole;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.common.util.PasswordHasher;
import com.gamebird.common.util.UUIDUtils;

import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.Account;
import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.service.AccountService;

/**
 * 账号路由处理器
 * 对账号的处理使用的路由分支
 */
@Controller
public class AccountController extends BaseController {

    @Resource
    AccountService accountService;

    /** 游戏开关配置（是否允许注册）。原先是ConstData 里的 public static 字段，任何代码都能改。 */
    @Resource
    GameProperties gameProperties;

    /**
     * 登录界面
     * @param session session
     * @param model model
     * @return view
     */
    @SkipAccount
    @SkipRole
    @GetMapping(value = {"/login"})
    public String pageLogin(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (null == account) {
            return "account/login";
        }
        model.addAttribute("account", account);
        return redirectTo("/role/login");
    }

    /**
     * 登录
     * @param session session
     * @param model model
     * @param username 账号
     * @param password 密码
     * @return view
     */
    @SkipAccount
    @SkipRole
    @PostMapping(value = "/login")
    public String doLogin(HttpSession session, Model model, @RequestParam String username, @RequestParam String password) {
        // 参数校验
        if (null == username || username.isEmpty()) {
            model.addAttribute("message", MessageVO.error("登陆失败,账号或密码错误。"));
            return "account/login";
        }
        if (null == password || password.isEmpty()) {
            model.addAttribute("message", MessageVO.error("登陆失败,账号或密码错误。"));
            return "account/login";
        }
        // 数据库查询角色列表
        LambdaQueryWrapper<Account> accountQueryWrapper = new LambdaQueryWrapper<>();
        accountQueryWrapper.eq(Account::getUsername, username);
        Account account = accountService.getOne(accountQueryWrapper);
        // 账户不存在或者密码错误
        if (null == account || !PasswordHasher.matches(password, account.getPassword())) {
            model.addAttribute("message", MessageVO.error("登陆失败,账号或密码错误。"));
            return "account/login";
        }
        // 账户不存在或者密码错误
        if (account.getIsBan() == 1) {
            model.addAttribute("message", MessageVO.error("账号已被封禁"));
            return "account/login";
        }
        // 透明升级：存量明文口令验证通过后立即改写为 BCrypt 哈希落库
        if (!PasswordHasher.isHashed(account.getPassword())) {
            account.setPassword(PasswordHasher.hash(password));
            accountService.updateById(account);
        }
        // 存储账户信息到session
        session.setAttribute("account", account);
        model.addAttribute("account", account);
        return redirectTo("/role/login");
    }

    /**
     * 注册账号界面
     * @param session session
     * @param model model
     * @return view
     */
    @SkipAccount
    @SkipRole
    @GetMapping(value = "/register")
    public String pageRegister(HttpSession session, Model model) {
        // 已禁止注册账号
        if (!gameProperties.isAllowRegister()) {
            model.addAttribute("message", MessageVO.error("服务器已禁止注册。"));
            return ConstData.VIEW_MESSAGE;
        }
        Account account = (Account) session.getAttribute("account");
        if (null != account) {
            return redirectTo("/role/login");
        }
        return "account/register";
    }

    /**
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
    @PostMapping(value = "/register")
    public String doRegister(HttpSession session, Model model, @RequestParam String username, @RequestParam String password, @RequestParam String passwordRe) {
        // 已禁止注册账号
        if (!gameProperties.isAllowRegister()) {
            model.addAttribute("message", MessageVO.error("服务器已禁止注册。"));
            return ConstData.VIEW_MESSAGE;
        }
        // 用户名长度和敏感词检测 todo
        // 账号和密码长度和字符检测 todo
        // 参数校验
        if (null == username || username.isEmpty()) {
            model.addAttribute("message", MessageVO.error("注册失败,账号或密码错误。"));
            return "account/register";
        }
        if (null == password || password.isEmpty()) {
            model.addAttribute("message", MessageVO.error("注册失败,账号或密码错误。"));
            return "account/register";
        }
        if (null == passwordRe || passwordRe.isEmpty() || !password.equals(passwordRe)) {
            model.addAttribute("message", MessageVO.error("注册失败,密码和重复密码不匹配。"));
            return "account/register";
        }
        // 数据库查询角色列表
        LambdaQueryWrapper<Account> accountQueryWrapper = new LambdaQueryWrapper<>();
        accountQueryWrapper.eq(Account::getUsername, username);
        Account account = accountService.getOne(accountQueryWrapper);

        // 账户不存在或者密码错误
        if (null != account) {
            model.addAttribute("message", MessageVO.error("注册失败,账号已存在。"));
            return "account/register";
        }

        int nowTime = CommonUtils.nowTime();
        // 创建账号
        account = new Account();
        account.setUserId(UUIDUtils.genUserId("bird"));
        // 平台信息
        account.setPlatform("bird");
        account.setUsername(username);
        account.setPassword(PasswordHasher.hash(password));
        // 基本信息
        account.setAvatarUrl("/assets/images/avatar/d.jpg");
        account.setNickname("无名氏"); //todo 随机用户名
        account.setCreateTime(nowTime);
        accountService.save(account);
        // 存储账户信息到session
        session.setAttribute("account", account);
        model.addAttribute("account", account);
        return redirectTo("/role/login");
    }

    /**
     * 修改密码界面（需已登录）。
     */
    @SkipRole
    @GetMapping(value = "/account/password")
    public String pageChangePassword(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (null == account) {
            return redirectTo("/login");
        }
        return "account/password";
    }

    /**
     * 修改密码：验旧设新，成功后新口令以 BCrypt 哈希落库。
     */
    @SkipRole
    @PostMapping(value = "/account/password")
    public String doChangePassword(HttpSession session, Model model,
                                   @RequestParam String oldPassword,
                                   @RequestParam String newPassword,
                                   @RequestParam String newPasswordRe) {
        Account account = (Account) session.getAttribute("account");
        if (null == account) {
            return redirectTo("/login");
        }
        // 从库中取最新数据，避免 session 里的口令快照过期
        Account dbAccount = accountService.getById(account.getUserId());
        if (null == dbAccount || !PasswordHasher.matches(oldPassword, dbAccount.getPassword())) {
            model.addAttribute("message", MessageVO.error("原密码错误。"));
            return "account/password";
        }
        if (null == newPassword || newPassword.isEmpty()) {
            model.addAttribute("message", MessageVO.error("新密码不能为空。"));
            return "account/password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("message", MessageVO.error("新密码至少 6 位。"));
            return "account/password";
        }
        if (!newPassword.equals(newPasswordRe)) {
            model.addAttribute("message", MessageVO.error("两次输入的新密码不一致。"));
            return "account/password";
        }
        if (PasswordHasher.matches(newPassword, dbAccount.getPassword())) {
            model.addAttribute("message", MessageVO.error("新密码不能与原密码相同。"));
            return "account/password";
        }
        dbAccount.setPassword(PasswordHasher.hash(newPassword));
        accountService.updateById(dbAccount);
        session.setAttribute("account", dbAccount);
        MessageVO messageVO = MessageVO.success("密码修改成功，下次登录请使用新密码。");
        messageVO.addLink(new MessageLink("回主页", "/game"));
        model.addAttribute("message", messageVO);
        return ConstData.VIEW_MESSAGE;
    }
}
