package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qq2008.common.util.JSONUtils;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.model.vo.RolePackVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/***
 * 包裹相关路由
 */
@RequestMapping("/pack")
@Controller
public class PackController extends BaseController {

    /***
     * 诱饵背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/bait/list.do")
    public String pageShopBait(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 数据库查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_BAIT);
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        packService.page(pageInfo, queryWrapper);
        // 数据转换(增加配置属性)
        Page<RolePackVO> pageInfo2 = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageInfo2);
        List<RolePackVO> baitRecordList = new ArrayList<>();
        for (RolePack item : pageInfo.getRecords()) {
            RolePackVO roleBaitVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleBaitVO);
            roleBaitVO.setBaseBait(GameConfigManager.getInstance().getBaseBait(roleBaitVO.getBaseId()));
            baitRecordList.add(roleBaitVO);
        }
        pageInfo2.setRecords(baitRecordList);
        model.addAttribute("page", pageInfo2);
        return "pack/bait";
    }

    /***
     * 鸟笼背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/cage/list.do")
    public String pageShopCage(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 数据库查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_CAGE);
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        packService.page(pageInfo, queryWrapper);
        // 数据转换(增加配置属性)
        Page<RolePackVO> pageInfo2 = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageInfo2);
        List<RolePackVO> cageRecordList = new ArrayList<>();
        for (RolePack item : pageInfo.getRecords()) {
            RolePackVO roleCageVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleCageVO);
            roleCageVO.setBaseCage(GameConfigManager.getInstance().getBaseCage(roleCageVO.getBaseId()));
            cageRecordList.add(roleCageVO);
        }
        pageInfo2.setRecords(cageRecordList);
        model.addAttribute("page", pageInfo2);
        return "pack/cage";
    }

    /***
     * 鸟巢背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/nest/list.do")
    public String pageShopNest(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 数据库查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_NEST);
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        packService.page(pageInfo, queryWrapper);
        // 数据转换(增加配置属性)
        Page<RolePackVO> pageInfo2 = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageInfo2);
        List<RolePackVO> nestRecordList = new ArrayList<>();
        for (RolePack item : pageInfo.getRecords()) {
            RolePackVO roleNestVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleNestVO);
            roleNestVO.setBaseNest(GameConfigManager.getInstance().getBaseNest(roleNestVO.getBaseId()));
            nestRecordList.add(roleNestVO);
        }
        pageInfo2.setRecords(nestRecordList);
        model.addAttribute("page", pageInfo2);
        return "pack/nest";
    }

    /***
     * 道具背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/prop/list.do")
    public String pageShopProp(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 数据库查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        packService.page(pageInfo, queryWrapper);
        // 数据转换(增加配置属性)
        Page<RolePackVO> pageInfo2 = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageInfo2);
        List<RolePackVO> propRecordList = new ArrayList<>();
        for (RolePack item : pageInfo.getRecords()) {
            RolePackVO rolePropVO = new RolePackVO();
            BeanUtils.copyProperties(item, rolePropVO);
            rolePropVO.setBaseProp(GameConfigManager.getInstance().getBaseProp(rolePropVO.getBaseId()));
            propRecordList.add(rolePropVO);
        }
        pageInfo2.setRecords(propRecordList);
        model.addAttribute("page", pageInfo2);
        return "pack/prop";
    }

    /***
     * 训练场背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/train/list.do")
    public String pageShopTrain(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 数据库查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_TRAIN);
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        packService.page(pageInfo, queryWrapper);
        // 数据转换(增加配置属性)
        Page<RolePackVO> pageInfo2 = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageInfo2);
        List<RolePackVO> trainRecordList = new ArrayList<>();
        for (RolePack item : pageInfo.getRecords()) {
            RolePackVO roleTrainVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleTrainVO);
            roleTrainVO.setBaseTrain(GameConfigManager.getInstance().getBaseTrain(roleTrainVO.getBaseId()));
            trainRecordList.add(roleTrainVO);
        }
        pageInfo2.setRecords(trainRecordList);
        System.out.println(JSONUtils.toJSON(trainRecordList));
        model.addAttribute("page", pageInfo2);
        return "pack/train";
    }

    /***
     * 陷阱背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/trap/list.do")
    public String pageShopTrap(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 数据库查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_TRAP);
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        packService.page(pageInfo, queryWrapper);
        // 数据转换(增加配置属性)
        Page<RolePackVO> pageInfo2 = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageInfo2);
        List<RolePackVO> trapRecordList = new ArrayList<>();
        for (RolePack item : pageInfo.getRecords()) {
            RolePackVO roleTrapVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleTrapVO);
            roleTrapVO.setBaseTrap(GameConfigManager.getInstance().getBaseTrap(roleTrapVO.getBaseId()));
            trapRecordList.add(roleTrapVO);
        }
        pageInfo2.setRecords(trapRecordList);
        model.addAttribute("page", pageInfo2);
        return "pack/trap";
    }

    // 使用道具
}
