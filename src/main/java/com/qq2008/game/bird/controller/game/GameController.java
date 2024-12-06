package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qq2008.common.annotation.SkipRole;
import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.model.dbo.*;
import com.qq2008.game.bird.model.vo.*;
import com.qq2008.game.bird.util.MockUtils;
import com.qq2008.game.bird.model.dbo.*;
import com.qq2008.game.bird.model.vo.*;
import com.qq2008.game.bird.util.MockUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/****
 * 游戏 todo
 * 1. 当前人数可以放在缓存中, 玩家注册角色后触发刷新
 * 2. 最近加入玩家可以放在缓存中
 * 3. 汽车配置可以存放再缓存中，定时器或者后台手动重新加载
 */

@Controller
public class GameController extends BaseController {
    // 日志句柄
    Logger logger = Logger.getLogger(GameController.class.getName());

    /***
     * 游戏主界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping(value = {"/", "/game.do"})
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
            trapVO.setUseTrap(GameConfigManager.getInstance().getBaseTrap(trap.getUseTrapId() == 0 ? ConstData.DEFAULT_TRAP_ID : trap.getUseTrapId()));
            if (trap.getCatchBirdId() > 0) {
                trapVO.setCatchBird(GameConfigManager.getInstance().getBaseBird(trap.getCatchBirdId()));
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
            trapVO.setUseNest(GameConfigManager.getInstance().getBaseNest(trap.getUseNestId() == 0 ? ConstData.DEFAULT_NEST_ID : trap.getUseNestId()));
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
            trainVO.setUseTrain(GameConfigManager.getInstance().getBaseTrain(trap.getUseTrainId() == 0 ? ConstData.DEFAULT_TRAIN_ID : trap.getUseTrainId()));
            if (trap.getBirdBaseId() > 0) {
                trainVO.setBaseBird(GameConfigManager.getInstance().getBaseBird(trap.getBirdBaseId()));
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
        // 树林记录数据
        Page<ChatMessageVO> treeMessagePage = MockUtils.mockTreeMessageList(7, role);
        model.addAttribute("treeMessagePage", treeMessagePage);
        // 世界动态数据
        Page<ChatMessageVO> globalMessagePage = MockUtils.mockTreeMessageList(7, role);
        model.addAttribute("globalMessagePage", globalMessagePage);
        return "game";
    }

    /***
     * 银行界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/bank.do")
    public String pageBank(HttpSession session, Model model) {
        return "role/bank";
    }

    /***
     * 存款
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/bankIn.do")
    public String doBankIn(HttpSession session, RedirectAttributes model, @RequestParam(defaultValue = "0") int inNum) {
        Role role = getSessionRole(session);
        // 100金币起存
        if (inNum < 100) {
            model.addFlashAttribute("message", MessageVO.error("100金币起存。"));
            return redirectTo("bank.do");
        }
        if (inNum > role.getCoin()) {
            model.addFlashAttribute("message", MessageVO.error("您的现金不足。"));
            return redirectTo("bank.do");
        }
        int addBankCoin = inNum * 99 / 100;
        // 更新并保存数据 todo
        costRoleCoin(role, (long) inNum);
        role.setBankCoin(role.getBankCoin() + addBankCoin);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addFlashAttribute("message", MessageVO.success("存款成功。<br />本次存入现金" + addBankCoin + ", 银行收取了" + (inNum - addBankCoin) + "的手续费"));
        return redirectTo("bank.do");
    }

    /***
     * 取款
     * @param session session
     * @param model model
     * @return view
     */
    @PostMapping("/bankOut.do")
    public String doBankOut(HttpSession session, RedirectAttributes model, @RequestParam(defaultValue = "0") int outNum) {
        Role role = getSessionRole(session);
        // 检测陷阱信息
        if (outNum > role.getBankCoin()) {
            model.addFlashAttribute("message", MessageVO.error("银行余额不足。"));
            return redirectTo("bank.do");
        }
        if (outNum == 0) {
            model.addFlashAttribute("message", MessageVO.error("取款金额错误。"));
            return redirectTo("bank.do");
        }
        // 查询已拥有的诱饵数据
        role.setBankCoin(role.getBankCoin() - outNum);
        addRoleCoin(role, (long) outNum);
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        model.addFlashAttribute("message", MessageVO.success("取款成功。"));
        return redirectTo("bank.do");
    }

    /***
     * 下饵界面
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/field/info.do")
    public String pageFile(HttpSession session, Model model, @RequestParam(defaultValue = "0") int fieldId) {
        BaseField baseField = GameConfigManager.getInstance().getBaseField(fieldId);
        model.addAttribute("baseField", baseField);
        return "field/fieldInfo";
    }

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

    @GetMapping("/profile.do")
    public String pageProfile(HttpSession session, Model model) {
        return "role/profile";
    }


    /***
     * 充值界面
     * @param session session
     * @param model model
     * @return view
     */
    @SkipRole
    @GetMapping("/pay.do")
    public String pagePay(HttpSession session, Model model) {
        //        int nowTime = CommonUtils.nowTimestamp();
        //        Role role = getSessionRole(session);
        //        String[] chatList = RandomTextData.getRandChatMessageArray();
        //        for (String s1 : chatList) {
        //            LogChat chat = new LogChat();
        //            chat.setId(0L);
        //            chat.setRoleId(role.getRoleId());
        //            chat.setMessage(s1);
        //            chat.setTime((int) (nowTime - Math.random() * 100000));
        //            chatService.save(chat);
        //        }

        return "vip/pay";
    }

    /***
     * 公告消息接口, 用于转发
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping(value = {"/message.do"})
    public String message(HttpSession session, Model model) {
        return "common/message";
    }
}
