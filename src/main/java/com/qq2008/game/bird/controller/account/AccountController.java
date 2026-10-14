package com.qq2008.game.bird.controller.account;

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

import com.qq2008.common.annotation.SkipAccount;
import com.qq2008.common.annotation.SkipRole;
import com.qq2008.common.entity.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;

import com.qq2008.game.bird.config.GameProperties;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.Account;
import com.qq2008.game.bird.service.AccountService;
import com.qq2008.game.bird.util.UUIDUtils;

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
        if (null == account || !passwordMatches(password, account.getPassword())) {
            model.addAttribute("message", MessageVO.error("登陆失败,账号或密码错误。"));
            return "account/login";
        }
        // 账户不存在或者密码错误
        if (account.getIsBan() == 1) {
            model.addAttribute("message", MessageVO.error("账号已被封禁"));
            return "account/login";
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
        account.setPassword(password);
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
     * 常量时间比较两个口令。
     *
     * <p>原先用 {@code password.equals(stored)}，其短路比较会在首个不等字节处立即返回，
     * 使响应耗时随「猜对前缀长度」线性变化，可被用于逐字节爆破。这里改用
     * {@link MessageDigest#isEqual}，其耗时只与长度相关。
     *
     * <p><b>注意</b>：本方法只做字节比较，口令在数据库中仍是明文（见
     * 「不做密码哈希迁移」红线）。它缩小的是时序侧信道，不是明文存储问题。
     *
     * @param input    用户提交的明文口令
     * @param stored   数据库中存储的口令
     * @return 是否一致
     */
    private static boolean passwordMatches(String input, String stored) {
        if (null == input || null == stored) {
            return false;
        }
        return MessageDigest.isEqual(
                input.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }
}
