package com.qq2008.game.bird.controller.game;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.qq2008.common.util.CommonUtils;

import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigGateway;
import com.qq2008.game.bird.model.dbo.BaseBait;
import com.qq2008.game.bird.model.dbo.BaseBird;
import com.qq2008.game.bird.model.dbo.BaseLevel;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.model.dbo.RoleTrap;
import com.qq2008.game.bird.model.dbo.StorageBird;
import com.qq2008.game.bird.model.message.MessageVO;
import com.qq2008.game.bird.model.vo.RolePackVO;
import com.qq2008.game.bird.model.vo.TrapResultVO;
import com.qq2008.game.bird.service.BirdRaffleService;
import com.qq2008.game.bird.util.BirdTextUtils;
import com.qq2008.game.bird.util.GameUtils;

/**
 * 陷阱相关路由
 */
@Controller
@RequestMapping("/trap")
public class TrapController extends BaseController {

    /** 游戏配置读取入口，由字段注入。 */
    @Resource
    public GameConfigGateway configGateway;

    /** 按权重随机抽鸟。抽离自本类，使算法可脱离 Spring 容器做单元测试。 */
    @Resource
    public BirdRaffleService birdRaffleService;

    /**
     * 下饵界面
     * @param session session
     * @param model model
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/useBait")
    public String pageUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId){
        Role role = getSessionRole(session);
        // 检测陷阱信息
        RoleTrap roleTrap = trapService.getById(trapId);
        if (roleTrap == null || !roleTrap.getRoleId().equals(role.getRoleId())){
            model.addAttribute("message", MessageVO.error("陷阱错误，请重新选择一个陷阱捕鸟。"));
            return ConstData.VIEW_MESSAGE;
        }
        if (roleTrap.getBaitId() > 0 ){
            model.addAttribute("message", MessageVO.error("当前陷阱正在捕鸟中。"));
            return ConstData.VIEW_MESSAGE;
        }
        if (roleTrap.getUseTrapId() > 0 && roleTrap.getUseTrapNum() == 0){
            model.addAttribute("message", MessageVO.error("陷阱已过期，请切换陷阱。"));
            return ConstData.VIEW_MESSAGE;
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
            roleBaitVO.setBaseBait(configGateway.getBaseBait(roleBaitVO.getBaseId()));
            packList.add(roleBaitVO);
        }
        model.addAttribute("baitList", packList);
        return "trap/bait";
    }

    /**
     * 下饵
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @PostMapping("/useBait")
    public String doUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){
        Role role = getSessionRole(session);
        // 未选择陷阱
        RoleTrap roleTrap = trapService.getById(trapId);
        if (roleTrap == null || !roleTrap.getRoleId().equals(role.getRoleId())){
            model.addAttribute("message", MessageVO.error("陷阱错误，请重新选择一个陷阱捕鸟。"));
            return ConstData.VIEW_MESSAGE;
        }
        if (roleTrap.getBaitId() > 0 ){
            model.addAttribute("message", MessageVO.error("当前陷阱正在捕鸟中。"));
            return ConstData.VIEW_MESSAGE;
        }
        if (roleTrap.getUseTrapId() > 0 && roleTrap.getUseTrapNum() == 0){
            model.addAttribute("message", MessageVO.error("陷阱已过期，请切换陷阱。"));
            return ConstData.VIEW_MESSAGE;
        }
        //
        RolePack packBait = packService.getById(baitId);
        // 诱饵数量不足
        if (packBait == null || !packBait.getRoleId().equals(role.getRoleId()) || packBait.getNum() == 0){
            model.addAttribute("message", MessageVO.error("诱饵数量不足"));
            return ConstData.VIEW_MESSAGE;
        }
        BaseBait baseBait = configGateway.getBaseBait(packBait.getBaseId());
        if (baseBait == null || baseBait.getBirdSet() == null || baseBait.getBirdSet().trim().isEmpty()){
            model.addAttribute("message", MessageVO.error("诱饵配置错误"));
            return ConstData.VIEW_MESSAGE;
        }
        int nowTime = CommonUtils.nowTime();
        // 随机小鸟
        BaseBird baseBird = birdRaffleService.randBird(baseBait.getBirdSet().split(","));
        if (baseBird == null){
            model.addAttribute("message", MessageVO.error("诱饵或者小鸟配置错误"));
            return ConstData.VIEW_MESSAGE;
        }
        // 随机性别
        Byte sex = (byte) (CommonUtils.randNum(1, 100) <= 50 ? 0 : 1);
        // 随机重量
        long weight = CommonUtils.randNum(baseBird.getMinWeight(), baseBird.getMaxWeight());
        // 获取星级
        Byte star = (byte) (weight / (baseBird.getMaxWeight() / 5));
        // 更新陷阱信息
        roleTrap.setBaitId(baitId);
        roleTrap.setCatchBirdId(baseBird.getId());
        roleTrap.setCatchBirdSex(sex);
        roleTrap.setCatchBirdWeight(weight);
        roleTrap.setCatchBirdStar(star);
        roleTrap.setCatchTime(nowTime);
        roleTrap.setCatchEndTime(nowTime + baseBird.getCatchMin() * 60);
        // 扣除诱饵数量
        costPackNum(packBait, 1);
        packService.saveOrUpdate(packBait);
        // 更新陷阱数据到数据库
        trapService.saveOrUpdate(roleTrap);

        return redirectTo("/game");
    }

    /**
     * 一键下饵界面
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/autoUseBait")
    public String pageAutoUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){

        return redirectTo("/game");
    }

    /**
     * 一键下饵
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @PostMapping("/autoUseBait")
    public String doAutoUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){


        model.addAttribute("message", MessageVO.success(""));
        return ConstData.VIEW_MESSAGE;
    }

    /**
     * 收网
     *
     * <p>★ 已知设计债（本轮不改）：本方法会写库（消耗陷阱、结算捕获），
     * 但仍是 {@code @GetMapping}。按 REST 语义，写操作应为 POST。
     * 本轮**有意不改** —— 改POST 需要同步改造 trap/over 页的触发方式，
     * 属于路由规范化批次的后续范围，此处仅记录，避免后人误以为是疏漏。
     * 已知缺陷：GET 收网可被浏览器预取或刷新重复触发。
     *
     * @param session session
     * @param model model
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/over")
    public String doOver(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId){
        Role role = getSessionRole(session);
        // 仓库鸟上限 todo
        // 陷阱未找到或者陷阱不是该角色的
        RoleTrap roleTrap = trapService.getById(trapId);
        if (roleTrap == null || !roleTrap.getRoleId().equals(role.getRoleId())){
            model.addAttribute("message", MessageVO.error("没有该陷阱"));
            return ConstData.VIEW_MESSAGE;
        }
        // 陷阱处于空闲中
        if (roleTrap.getBaitId() == 0){
            model.addAttribute("message", MessageVO.error("当前陷阱正处于空闲中。"));
            return ConstData.VIEW_MESSAGE;
        }
        StorageBird bird = new StorageBird();
        bird.setId(0);
        bird.setRoleId(role.getRoleId());
        bird.setBirdId(roleTrap.getCatchBirdId());
        bird.setLevel((short) 1);
        bird.setExp(0L);
        bird.setTotalExp(0L);
        bird.setSex(roleTrap.getCatchBirdSex());
        bird.setWeight(roleTrap.getCatchBirdWeight());
        bird.setInitWeight(roleTrap.getCatchBirdWeight());
        bird.setStar(roleTrap.getCatchBirdStar());
        bird.setGrow((short) 1000); // todo 随机成长值
        bird.setKo((short) 0); // todo 随机克制值
        bird.setIsLock((byte) 0);
        bird.setTime(CommonUtils.nowTime());
        // 获取小鸟配置
        BaseBird baseBird = configGateway.getBaseBird(bird.getBirdId());
        long addExp = baseBird.getCatchExp();
        // 更新陷阱信息
        roleTrap.setBaitId(0);
        roleTrap.setCatchBirdId(0);
        roleTrap.setCatchBirdSex((byte) 0);
        roleTrap.setCatchBirdWeight(0L);
        roleTrap.setCatchBirdStar((byte) 0);
        roleTrap.setCatchTime(0);
        roleTrap.setCatchEndTime(0);
        trapService.saveOrUpdate(roleTrap);
        // 增加小鸟数据
        int birdId = storageBirdService.insertStorageBird(bird);
        // 增加角色经验值
        int oldLevel = role.getLevel();
        addRoleExp(role, addExp);
        roleService.saveOrUpdate(role);
        // 计算升到下一级所需要的经验值
        long needExp = 0;
        BaseLevel baseLevel = configGateway.getBaseLevel(role.getLevel() + 1);
        if (baseLevel != null){
            needExp = baseLevel.getTotalExp() - role.getTotalExp();
        }
        // 构造结果数据
        TrapResultVO resultVO = new TrapResultVO();
        resultVO.setBirdId(birdId);
        resultVO.setBirdBaseId( baseBird.getId());
        resultVO.setNameText(bird.getWeight() / 100.0 + "斤的" + GameUtils.getBirdSexName(bird.getSex()) + baseBird.getName());
        resultVO.setStarText(GameUtils.getStarName(bird.getStar()));
        resultVO.setGrowText(BirdTextUtils.growText(bird.getGrow()));
        resultVO.setKoText(BirdTextUtils.koText(bird.getKo()));
        resultVO.setMinWeight(baseBird.getMinWeight());
        resultVO.setMaxWeight(baseBird.getMaxWeight());
        resultVO.setAddExp(addExp);
        resultVO.setNeedExp(needExp);
        resultVO.setWishNum(0);
        resultVO.setOldLevel(oldLevel);
        resultVO.setNewLevel(role.getLevel());
        model.addAttribute("result", resultVO);
        // 增加小鸟日志 todo
        return "trap/over";
    }

    /**
     * 小鸟详情
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/birdInfo")
    public String pageBirdInfo(HttpSession session, Model model, @RequestParam(defaultValue = "0") int birdId){
        BaseBird baseBird = configGateway.getBaseBird(birdId);
        model.addAttribute("baseBird", baseBird);
        return "trap/birdInfo";
    }

}
