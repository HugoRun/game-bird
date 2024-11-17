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
        model.addAttribute("nowTime", CommonUtils.nowTimestamp());

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
            if(trap.getBaseBirdId() > 0){
                trapVO.setCatchBird(GameConfigManager.getInstance().getBaseBird(trap.getBaseBirdId()));
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
            if(trap.getBirdBaseId() > 0){
                trainVO.setBaseBird(GameConfigManager.getInstance().getBaseBird(trap.getBirdBaseId()));
            }
            trainList.add(trainVO);
        }
        model.addAttribute("trainList", trainList);
        // 世界聊天数据
        LambdaQueryWrapper<LogChat> queryWrapper2 = new LambdaQueryWrapper<>();
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


    // 存款界面 userBank
    // 存款
    // 银行 userBankUp

    /***
     * 下饵界面
     * @param session session
     * @param model model
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/bait/use")
    public String pageChooseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId){
        Role role = getSessionRole(session);
        // 检测陷阱信息
        RoleTrap roleTrap = trapService.getById(trapId);
        if(roleTrap == null || !roleTrap.getRoleId().equals(role.getRoleId())){
            model.addAttribute("message", MessageVO.error("陷阱错误，请重新选择一个陷阱捕鸟。"));
            return "common/message";
        }
        if(roleTrap.getBaitId() > 0 ){
            model.addAttribute("message", MessageVO.error("当前陷阱正在捕鸟中。"));
            return "common/message";
        }
        if(roleTrap.getUseTrapId() > 0 && roleTrap.getUseTrapNum() == 0){
            model.addAttribute("message", MessageVO.error("陷阱已过期，请切换陷阱。"));
            return "common/message";
        }
        model.addAttribute("trapId", trapId);
        // 查询已拥有的诱饵数据
        LambdaQueryWrapper<RolePack> baitQueryWrapper = new LambdaQueryWrapper<>();
        baitQueryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        baitQueryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_BAIT);
        baitQueryWrapper.gt(RolePack::getNum, 0);
        List<RolePack> packList1 = packService.list(baitQueryWrapper);
        //
        List<RolePackVO> packList = new ArrayList<>();
        for (RolePack item : packList1) {
            RolePackVO roleBaitVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleBaitVO);
            roleBaitVO.setBaseBait(GameConfigManager.getInstance().getBaseBait(roleBaitVO.getBaseId()));
            packList.add(roleBaitVO);
        }
        model.addAttribute("baitList", packList);
        return "trap/bait";
    }

    /***
     * 下饵界面
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @PostMapping("/bait/use")
    public String useBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){
        Role role = getSessionRole(session);
        // 未选择陷阱
        RoleTrap roleTrap = trapService.getById(trapId);
        if(roleTrap == null || !roleTrap.getRoleId().equals(role.getRoleId())){
            model.addAttribute("message", MessageVO.error("陷阱错误，请重新选择一个陷阱捕鸟。"));
            return "common/message";
        }
        if(roleTrap.getBaitId() > 0 ){
            model.addAttribute("message", MessageVO.error("当前陷阱正在捕鸟中。"));
            return "common/message";
        }
        if(roleTrap.getUseTrapId() > 0 && roleTrap.getUseTrapNum() == 0){
            model.addAttribute("message", MessageVO.error("陷阱已过期，请切换陷阱。"));
            return "common/message";
        }
        //
        RolePack packBait = packService.getById(baitId);
        // 诱饵数量不足
        if(packBait == null || !packBait.getRoleId().equals(role.getRoleId()) || packBait.getNum() == 0){
            model.addAttribute("message", MessageVO.error("诱饵数量不足"));
            return "common/message";
        }
        BaseBait baseBait = GameConfigManager.getInstance().getBaseBait(baitId);
        if(baseBait == null || baseBait.getBirdSet() == null || baseBait.getBirdSet().trim().isEmpty()){
            model.addAttribute("message", MessageVO.error("诱饵配置错误"));
            return "common/message";
        }

        int nowTime = CommonUtils.nowTimestamp();
        // 随机小鸟
        BaseBird baseBird = randBird(baseBait.getBirdSet().split(","));
        if(baseBird == null){
            model.addAttribute("message", MessageVO.error("诱饵或者小鸟配置错误"));
            return "common/message";
        }

        // 扣除诱饵数量
        costPackNum(packBait, 1);
        packService.saveOrUpdate(packBait);

        roleTrap.setBaitId(baitId);
        roleTrap.setBaseBirdId(baseBird.getId());
        roleTrap.setCatchTime(nowTime);
        roleTrap.setCatchEndTime(nowTime + baseBird.getCatchMin() * 60);
        trapService.saveOrUpdate(roleTrap);

        return redirectTo("/game.do");
    }

    /***
     * 随机一只小鸟
     * @param birdIdList 小鸟Id,...
     * @return BaseBird 小鸟配置
     */
    BaseBird randBird(String[] birdIdList){
        //
        if(birdIdList == null || birdIdList.length == 0){
            return null;
        }
        // 获取配置列表
        List<BaseBird> baseBirdList = new ArrayList<>();
        int totalWeight = 0;
        for(String birdId : birdIdList){
            BaseBird baseBird = GameConfigManager.getInstance().getBaseBird(Integer.valueOf(birdId.trim()));
            if(baseBird != null && baseBird.getProbability() > 0){
                baseBirdList.add(baseBird);
                totalWeight += baseBird.getProbability();
            }
        }
        int randWeight =  (int)(Math.random() * totalWeight + 1);
        // 获取总权重
        int i = 0;
        for(BaseBird bird : baseBirdList){
            i += bird.getProbability();
            if(randWeight <= i){
                return bird;
            }
        }
        return null;
    }

    // 下饵
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
