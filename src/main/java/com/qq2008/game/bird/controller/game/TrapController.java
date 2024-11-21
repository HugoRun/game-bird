package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.common.util.CommonUtils;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.model.dbo.*;
import com.qq2008.game.bird.model.vo.RolePackVO;
import com.qq2008.game.bird.model.vo.TrapResultVO;
import com.qq2008.game.bird.util.GameUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/***
 * 陷阱相关路由
 */
@Controller
@RequestMapping("/trap")
public class TrapController extends BaseController {

    /***
     * 下饵界面
     * @param session session
     * @param model model
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/useBait.do")
    public String pageUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId){
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
     * 下饵
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @PostMapping("/useBait.do")
    public String doUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){
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
        BaseBait baseBait = GameConfigManager.getInstance().getBaseBait(packBait.getBaseId());
        if(baseBait == null || baseBait.getBirdSet() == null || baseBait.getBirdSet().trim().isEmpty()){
            model.addAttribute("message", MessageVO.error("诱饵配置错误"));
            return "common/message";
        }
        int nowTime = CommonUtils.nowTime();
        // 随机小鸟
        BaseBird baseBird = randBird(baseBait.getBirdSet().split(","));
        if(baseBird == null){
            model.addAttribute("message", MessageVO.error("诱饵或者小鸟配置错误"));
            return "common/message";
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

        return redirectTo("/game.do");
    }

    /***
     * 一键下饵界面
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/autoUseBait.do")
    public String pageAutoUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){

        return redirectTo("/game.do");
    }

    /***
     * 一键下饵
     * @param session session
     * @param model model
     * @param baitId 诱饵Id
     * @param trapId 陷阱Id
     * @return view
     */
    @PostMapping("/autoUseBait.do")
    public String doAutoUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId, @RequestParam(defaultValue = "0") int trapId){


        model.addAttribute("message", MessageVO.success(""));
        return "common/message";
    }

    /***
     * 收网
     * @param session session
     * @param model model
     * @param trapId 陷阱Id
     * @return view
     */
    @GetMapping("/over.do")
    public String doOver(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId){
        Role role = getSessionRole(session);
        // 仓库鸟上限 todo
        // 陷阱未找到或者陷阱不是该角色的
        RoleTrap roleTrap = trapService.getById(trapId);
        if(roleTrap == null || !roleTrap.getRoleId().equals(role.getRoleId())){
            model.addAttribute("message", MessageVO.error("没有该陷阱"));
            return "common/message";
        }
        // 陷阱处于空闲中
        if(roleTrap.getBaitId() == 0){
            model.addAttribute("message", MessageVO.error("当前陷阱正处于空闲中。"));
            return "common/message";
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
        BaseBird baseBird = GameConfigManager.getInstance().getBaseBird(bird.getBirdId());
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
        BaseLevel baseLevel = GameConfigManager.getInstance().getBaseLevel(role.getLevel() + 1);
        if(baseLevel != null){
            needExp = baseLevel.getTotalExp() - role.getTotalExp();
        }
        // 构造结果数据
        TrapResultVO resultVO = new TrapResultVO();
        resultVO.setBirdId(birdId);
        resultVO.setBirdBaseId( baseBird.getId());
        resultVO.setNameText(bird.getWeight() / 100.0 + "斤的" + GameUtils.getBirdSexName(bird.getSex()) + baseBird.getName());
        resultVO.setStarText(GameUtils.getStarName(bird.getStar()));
        resultVO.setGrowText("2.78%(0.01~5.00)");
        resultVO.setKoText("196%(1~300)");
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

    /***
     * 小鸟详情
     * @param session session
     * @param model model
     * @return view
     */
    @GetMapping("/birdInfo.do")
    public String pageBirdInfo(HttpSession session, Model model, @RequestParam(defaultValue = "0") int birdId){
        BaseBird baseBird = GameConfigManager.getInstance().getBaseBird(birdId);
        model.addAttribute("baseBird", baseBird);
        return "trap/birdInfo";
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
        // 总权重
        int totalRatio = 0;
        for(String birdId : birdIdList){
            BaseBird baseBird = GameConfigManager.getInstance().getBaseBird(Integer.valueOf(birdId.trim()));
            if(baseBird != null && baseBird.getProbability() > 0){
                baseBirdList.add(baseBird);
                totalRatio += baseBird.getProbability();
            }
        }
        int randRatio =  (int)(Math.random() * totalRatio + 1);
        // 获取总权重
        int i = 0;
        for(BaseBird bird : baseBirdList){
            i += bird.getProbability();
            if(randRatio <= i){
                return bird;
            }
        }
        return null;
    }
}
