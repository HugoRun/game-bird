package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qq2008.common.entiy.vo.MessageLink;
import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.*;
import com.qq2008.game.bird.util.GameUtils;
import com.qq2008.game.bird.model.dbo.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

/***
 * 商店相关路由
 */
@RequestMapping("/shop")
@Controller
public class ShopController extends BaseController {

    // 秒杀界面 shopSec
    // 秒杀
    // 购买成功 shopBuy

    // 诱饵商店界面
    @GetMapping(value = "/bait/list.do")
    public String pageShopBait(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "0") int feeType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("feeType", feeType);
        // 查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<BaseBait> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(BaseBait::getLu, role.getLevel());
        queryWrapper.eq(BaseBait::getIsShopping, 1);
        if (feeType > 0) {
            queryWrapper.eq(BaseBait::getFeeType, feeType);
        }
        queryWrapper.orderByDesc(BaseBait::getLu);
        Page<BaseBait> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        baseBaitService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "shop/bait";
    }

    // 诱饵商店详情界面
    @GetMapping(value = "/bait/detail.do")
    public String pageShopBaitDetail(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id) {
        return "shop/baitDetail";
    }

    // 购买诱饵
    @PostMapping(value = "/bait/buy.do")
    public String buyShopBait(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id, @RequestParam(defaultValue = "1") int payNum) {
        Role role = getSessionRole(session);
        // 诱饵配置校验
        BaseBait baseBait = baseBaitService.getById(id);
        if (baseBait == null) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 商品是否可购买
        if (isFeeTypeError(baseBait.getFeeType())) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 等级不足
        if (baseBait.getLu() > role.getLevel()) {
            model.addAttribute("message", MessageVO.error("您的等级不足。"));
            return "common/message";
        }
        // 总金额
        int totalFeePrice = baseBait.getFeePrice() * payNum;
        // 金钱校验
        if (Objects.equals(baseBait.getFeeType(), ConstData.FEE_TYPE_COIN)) {
            //
            if (role.getCoin() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的金钱不足哦(" + role.getCoin() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除金钱
            costRoleCoin(role, (long) totalFeePrice);
        }
        // 钻石校验
        if (Objects.equals(baseBait.getFeeType(), ConstData.FEE_TYPE_DIAMOND)) {
            //
            if (role.getDiamond() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的钻石不足(" + role.getDiamond() + "/" + totalFeePrice + ")。"));
                return "common/message";

            }
            // 扣除钻石
            costRoleDiamond(role, totalFeePrice);
        }
        // 爱心值校验
        if (Objects.equals(baseBait.getFeeType(), ConstData.FEE_TYPE_LOVE_POINT)) {
            //
            if (role.getLovePoint() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的爱心值不足(" + role.getLovePoint() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除爱心值
            costRoleLovePoint(role, (long) totalFeePrice);
        }
        // 增加诱饵数量
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_BAIT);
        RolePack packBait = packService.getOne(queryWrapper);
        if (packBait == null) {
            packBait = new RolePack();
            packBait.setId(0);
            packBait.setRoleId(role.getRoleId());
            packBait.setTypeId(ConstData.PACK_TYPE_BAIT);
            packBait.setBaseId(id);
            packBait.setNum(0);
        }
        // 修改用户诱饵数量
        addPackNum(packBait,payNum);
        // 回写数据
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        packService.saveOrUpdate(packBait);
        // 消息
        String message = "购买成功，您花费" + totalFeePrice + GameUtils.getFeeTypeName(baseBait.getFeeType()) + "购买了" + baseBait.getName() + "×" + payNum;
        // 写日志
        MessageVO messageVO = MessageVO.success(message);
        messageVO.addLink(new MessageLink("去包裹看看", "/pack/bait/list.do"));
        model.addAttribute("message", messageVO);
        return "common/message";
    }

    // 陷阱商店界面
    @GetMapping(value = "/trap/list.do")
    public String pageShopTrap(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "0") int feeType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("feeType", feeType);
        // 查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<BaseTrap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(BaseTrap::getLu, role.getLevel());
        queryWrapper.eq(BaseTrap::getIsShopping, 1);
        if (feeType > 0) {
            queryWrapper.eq(BaseTrap::getFeeType, feeType);
        }
        queryWrapper.orderByDesc(BaseTrap::getLu);
        Page<BaseTrap> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        baseTrapService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "shop/trap";
    }

    // 陷阱商店详情界面
    @GetMapping(value = "/trap/detail.do")
    public String pageShopTrapDetail(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id) {
        return "shop/trapDetail";
    }

    // 购买陷阱
    @PostMapping(value = "/trap/buy.do")
    public String buyShopTrap(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id, @RequestParam(defaultValue = "1") int payNum) {
        Role role = getSessionRole(session);
        // 陷阱配置校验
        BaseTrap baseTrap = baseTrapService.getById(id);
        if (baseTrap == null) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 商品是否可购买
        if (isFeeTypeError(baseTrap.getFeeType())) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 等级不足
        if (baseTrap.getLu() > role.getLevel()) {
            model.addAttribute("message", MessageVO.error("您的等级不足。"));
            return "common/message";
        }
        // 总金额
        int totalFeePrice = baseTrap.getFeePrice() * payNum;
        // 金钱校验
        if (Objects.equals(baseTrap.getFeeType(), ConstData.FEE_TYPE_COIN)) {
            //
            if (role.getCoin() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的金钱不足哦(" + role.getCoin() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除金钱
            costRoleCoin(role, (long) totalFeePrice);
        }
        // 钻石校验
        if (Objects.equals(baseTrap.getFeeType(), ConstData.FEE_TYPE_DIAMOND)) {
            //
            if (role.getDiamond() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的钻石不足(" + role.getDiamond() + "/" + totalFeePrice + ")。"));
                return "common/message";

            }
            // 扣除钻石
            costRoleDiamond(role, totalFeePrice);
        }
        // 爱心值校验
        if (Objects.equals(baseTrap.getFeeType(), ConstData.FEE_TYPE_LOVE_POINT)) {
            //
            if (role.getLovePoint() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的爱心值不足(" + role.getLovePoint() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除爱心值
            costRoleLovePoint(role, (long) totalFeePrice);
        }
        // 增加陷阱数量
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_TRAP);
        RolePack packTrap = packService.getOne(queryWrapper);
        if (packTrap == null) {
            packTrap = new RolePack();
            packTrap.setId(0);
            packTrap.setRoleId(role.getRoleId());
            packTrap.setTypeId(ConstData.PACK_TYPE_TRAP);
            packTrap.setBaseId(id);
            packTrap.setNum(0);
        }
        // 修改用户陷阱数量
        addPackNum(packTrap,payNum);
        // 回写数据
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        packService.saveOrUpdate(packTrap);
        // 消息
        String message = "购买成功，您花费" + totalFeePrice + GameUtils.getFeeTypeName(baseTrap.getFeeType()) + "购买了" + baseTrap.getName() + "×" + payNum;
        // 写日志
        MessageVO messageVO = MessageVO.success(message);
        messageVO.addLink(new MessageLink("去包裹看看", "/pack/trap/list.do"));
        model.addAttribute("message", messageVO);
        return "common/message";
    }

    // 鸟笼商店界面
    @GetMapping(value = "/cage/list.do")
    public String pageShopCage(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "0") int feeType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("feeType", feeType);
        // 查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<BaseCage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(BaseCage::getLu, role.getLevel());
        queryWrapper.eq(BaseCage::getIsShopping, 1);
        if (feeType > 0) {
            queryWrapper.eq(BaseCage::getFeeType, feeType);
        }
        queryWrapper.orderByDesc(BaseCage::getLu);
        Page<BaseCage> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        baseCageService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "shop/cage";
    }

    // 鸟笼商店详情界面
    @GetMapping(value = "/cage/detail.do")
    public String pageShopCageDetail(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id) {
        return "shop/cageDetail";
    }

    // 购买鸟笼
    @PostMapping(value = "/cage/buy.do")
    public String buyShopCage(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id, @RequestParam(defaultValue = "1") int payNum) {
        Role role = getSessionRole(session);
        // 鸟笼配置校验
        BaseCage baseCage = baseCageService.getById(id);
        if (baseCage == null) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 商品是否可购买
        if (isFeeTypeError(baseCage.getFeeType())) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 等级不足
        if (baseCage.getLu() > role.getLevel()) {
            model.addAttribute("message", MessageVO.error("您的等级不足。"));
            return "common/message";
        }
        // 总金额
        int totalFeePrice = baseCage.getFeePrice() * payNum;
        // 金钱校验
        if (Objects.equals(baseCage.getFeeType(), ConstData.FEE_TYPE_COIN)) {
            //
            if (role.getCoin() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的金钱不足哦(" + role.getCoin() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除金钱
            costRoleCoin(role, (long) totalFeePrice);
        }
        // 钻石校验
        if (Objects.equals(baseCage.getFeeType(), ConstData.FEE_TYPE_DIAMOND)) {
            //
            if (role.getDiamond() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的钻石不足(" + role.getDiamond() + "/" + totalFeePrice + ")。"));
                return "common/message";

            }
            // 扣除钻石
            costRoleDiamond(role, totalFeePrice);
        }
        // 爱心值校验
        if (Objects.equals(baseCage.getFeeType(), ConstData.FEE_TYPE_LOVE_POINT)) {
            //
            if (role.getLovePoint() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的爱心值不足(" + role.getLovePoint() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除爱心值
            costRoleLovePoint(role, (long) totalFeePrice);
        }
        // 增加鸟笼数量
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_CAGE);
        RolePack packCage = packService.getOne(queryWrapper);
        if (packCage == null) {
            packCage = new RolePack();
            packCage.setId(0);
            packCage.setRoleId(role.getRoleId());
            packCage.setTypeId(ConstData.PACK_TYPE_CAGE);
            packCage.setBaseId(id);
            packCage.setNum(0);
        }
        // 修改用户鸟笼数量
        addPackNum(packCage,payNum);
        // 回写数据
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        packService.saveOrUpdate(packCage);
        // 消息
        String message = "购买成功，您花费" + totalFeePrice + GameUtils.getFeeTypeName(baseCage.getFeeType()) + "购买了" + baseCage.getName() + "×" + payNum;
        // 写日志
        MessageVO messageVO = MessageVO.success(message);
        messageVO.addLink(new MessageLink("去包裹看看", "/pack/cage/list.do"));
        model.addAttribute("message", messageVO);
        return "common/message";
    }

    // 巢穴商店界面
    @GetMapping(value = "/nest/list.do")
    public String pageShopNest(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "0") int feeType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("feeType", feeType);
        // 查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<BaseNest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(BaseNest::getLu, role.getLevel());
        queryWrapper.eq(BaseNest::getIsShopping, 1);
        if (feeType > 0) {
            queryWrapper.eq(BaseNest::getFeeType, feeType);
        }
        queryWrapper.orderByDesc(BaseNest::getLu);
        Page<BaseNest> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        baseNestService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "shop/nest";
    }

    // 巢穴商店详情界面
    @GetMapping(value = "/nest/detail.do")
    public String pageShopNestDetail(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id) {
        return "shop/nestDetail";
    }

    // 购买巢穴
    @PostMapping(value = "/nest/buy.do")
    public String buyShopNest(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id, @RequestParam(defaultValue = "1") int payNum) {
        Role role = getSessionRole(session);
        // 巢配置校验
        BaseNest baseNest = baseNestService.getById(id);
        if (baseNest == null) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 商品是否可购买
        if (isFeeTypeError(baseNest.getFeeType())) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 等级不足
        if (baseNest.getLu() > role.getLevel()) {
            model.addAttribute("message", MessageVO.error("您的等级不足。"));
            return "common/message";
        }
        // 总金额
        int totalFeePrice = baseNest.getFeePrice() * payNum;
        // 金钱校验
        if (Objects.equals(baseNest.getFeeType(), ConstData.FEE_TYPE_COIN)) {
            //
            if (role.getCoin() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的金钱不足哦(" + role.getCoin() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除金钱
            costRoleCoin(role, (long) totalFeePrice);
        }
        // 钻石校验
        if (Objects.equals(baseNest.getFeeType(), ConstData.FEE_TYPE_DIAMOND)) {
            //
            if (role.getDiamond() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的钻石不足(" + role.getDiamond() + "/" + totalFeePrice + ")。"));
                return "common/message";

            }
            // 扣除钻石
            costRoleDiamond(role, totalFeePrice);
        }
        // 爱心值校验
        if (Objects.equals(baseNest.getFeeType(), ConstData.FEE_TYPE_LOVE_POINT)) {
            //
            if (role.getLovePoint() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的爱心值不足(" + role.getLovePoint() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除爱心值
            costRoleLovePoint(role, (long) totalFeePrice);
        }
        // 增加巢数量
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_NEST);
        RolePack packNest = packService.getOne(queryWrapper);
        if (packNest == null) {
            packNest = new RolePack();
            packNest.setId(0);
            packNest.setRoleId(role.getRoleId());
            packNest.setTypeId(ConstData.PACK_TYPE_NEST);
            packNest.setBaseId(id);
            packNest.setNum(0);
        }
        // 修改用户巢数量
        addPackNum(packNest,payNum);
        // 回写数据
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        packService.saveOrUpdate(packNest);
        // 消息
        String message = "购买成功，您花费" + totalFeePrice + GameUtils.getFeeTypeName(baseNest.getFeeType()) + "购买了" + baseNest.getName() + "×" + payNum;
        // 写日志
        MessageVO messageVO = MessageVO.success(message);
        messageVO.addLink(new MessageLink("去包裹看看", "/pack/nest/list.do"));
        model.addAttribute("message", messageVO);
        return "common/message";
    }

    // 训练场商店界面
    @GetMapping(value = "/train/list.do")
    public String pageShopTrain(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "0") int feeType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("feeType", feeType);
        // 查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<BaseTrain> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(BaseTrain::getLu, role.getLevel());
        queryWrapper.eq(BaseTrain::getIsShopping, 1);
        if (feeType > 0) {
            queryWrapper.eq(BaseTrain::getFeeType, feeType);
        }
        queryWrapper.orderByDesc(BaseTrain::getLu);
        Page<BaseTrain> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        baseTrainService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "shop/train";
    }

    // 训练场商店详情界面
    @GetMapping(value = "/train/detail.do")
    public String pageShopTrainDetail(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id) {
        return "shop/trainDetail";
    }

    // 购买训练场
    @PostMapping(value = "/train/buy.do")
    public String buyShopTrain(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id, @RequestParam(defaultValue = "1") int payNum) {
        Role role = getSessionRole(session);
        // 训练场配置校验
        BaseTrain baseTrain = baseTrainService.getById(id);
        if (baseTrain == null) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 商品是否可购买
        if (isFeeTypeError(baseTrain.getFeeType())) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 等级不足
        if (baseTrain.getLu() > role.getLevel()) {
            model.addAttribute("message", MessageVO.error("您的等级不足。"));
            return "common/message";
        }
        // 总金额
        int totalFeePrice = baseTrain.getFeePrice() * payNum;
        // 金钱校验
        if (Objects.equals(baseTrain.getFeeType(), ConstData.FEE_TYPE_COIN)) {
            //
            if (role.getCoin() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的金钱不足哦(" + role.getCoin() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除金钱
            costRoleCoin(role, (long) totalFeePrice);
        }
        // 钻石校验
        if (Objects.equals(baseTrain.getFeeType(), ConstData.FEE_TYPE_DIAMOND)) {
            //
            if (role.getDiamond() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的钻石不足(" + role.getDiamond() + "/" + totalFeePrice + ")。"));
                return "common/message";

            }
            // 扣除钻石
            costRoleDiamond(role, totalFeePrice);
        }
        // 爱心值校验
        if (Objects.equals(baseTrain.getFeeType(), ConstData.FEE_TYPE_LOVE_POINT)) {
            //
            if (role.getLovePoint() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的爱心值不足(" + role.getLovePoint() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除爱心值
            costRoleLovePoint(role, (long) totalFeePrice);
        }
        // 增加训练场数量
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_TRAIN);
        RolePack packTrain = packService.getOne(queryWrapper);
        if (packTrain == null) {
            packTrain = new RolePack();
            packTrain.setId(0);
            packTrain.setRoleId(role.getRoleId());
            packTrain.setTypeId(ConstData.PACK_TYPE_TRAIN);
            packTrain.setBaseId(id);
            packTrain.setNum(0);
        }
        // 修改用户训练场数量
        addPackNum(packTrain,payNum);
        // 回写数据
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        packService.saveOrUpdate(packTrain);
        // 消息
        String message = "购买成功，您花费" + totalFeePrice + GameUtils.getFeeTypeName(baseTrain.getFeeType()) + "购买了" + baseTrain.getName() + "×" + payNum;
        // 写日志
        MessageVO messageVO = MessageVO.success(message);
        messageVO.addLink(new MessageLink("去包裹看看", "/pack/train/list.do"));
        model.addAttribute("message", messageVO);
        return "common/message";
    }

    // 道具商店界面
    @GetMapping(value = "/prop/list.do")
    public String pageShopProp(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "0") int feeType) {
        // 参数兼容
        if (pageNo < 1) {
            pageNo = 1;
        }
        // 参数回写
        model.addAttribute("feeType", feeType);
        // 查询数据
        Role role = getSessionRole(session);
        LambdaQueryWrapper<BaseProp> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(BaseProp::getLu, role.getLevel());
        queryWrapper.eq(BaseProp::getIsShopping, 1);
        if (feeType > 0) {
            queryWrapper.eq(BaseProp::getFeeType, feeType);
        }
        queryWrapper.orderByDesc(BaseProp::getLu);
        Page<BaseProp> pageInfo = new Page<>();
        pageInfo.setCurrent(pageNo);
        pageInfo.setSize(5);
        basePropService.page(pageInfo, queryWrapper);
        model.addAttribute("page", pageInfo);
        return "shop/prop";
    }

    // 道具商店详情界面
    @GetMapping(value = "/prop/detail.do")
    public String pageShopPropDetail(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id) {
        return "shop/propDetail";
    }

    // 购买道具
    @PostMapping(value = "/prop/buy.do")
    public String buyShopProp(HttpSession session, Model model, @RequestParam(defaultValue = "1") int id, @RequestParam(defaultValue = "1") int payNum) {
        Role role = getSessionRole(session);
        // 道具配置校验
        BaseProp baseProp = basePropService.getById(id);
        if (baseProp == null) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 商品是否可购买
        if (isFeeTypeError(baseProp.getFeeType())) {
            model.addAttribute("message", MessageVO.error("没有该商品哦。"));
            return "common/message";
        }
        // 等级不足
        if (baseProp.getLu() > role.getLevel()) {
            model.addAttribute("message", MessageVO.error("您的等级不足。"));
            return "common/message";
        }
        // 总金额
        int totalFeePrice = baseProp.getFeePrice() * payNum;
        // 金钱校验
        if (Objects.equals(baseProp.getFeeType(), ConstData.FEE_TYPE_COIN)) {
            //
            if (role.getCoin() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的金钱不足哦(" + role.getCoin() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除金钱
            costRoleCoin(role, (long) totalFeePrice);
        }
        // 钻石校验
        if (Objects.equals(baseProp.getFeeType(), ConstData.FEE_TYPE_DIAMOND)) {
            //
            if (role.getDiamond() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的钻石不足(" + role.getDiamond() + "/" + totalFeePrice + ")。"));
                return "common/message";

            }
            // 扣除钻石
            costRoleDiamond(role, totalFeePrice);
        }
        // 爱心值校验
        if (Objects.equals(baseProp.getFeeType(), ConstData.FEE_TYPE_LOVE_POINT)) {
            //
            if (role.getLovePoint() < totalFeePrice) {
                model.addAttribute("message", MessageVO.error("您的爱心值不足(" + role.getLovePoint() + "/" + totalFeePrice + ")。"));
                return "common/message";
            }
            // 扣除爱心值
            costRoleLovePoint(role, (long) totalFeePrice);
        }
        // 增加道具数量
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, role.getRoleId());
        queryWrapper.eq(RolePack::getBaseId, id);
        queryWrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        RolePack packProp = packService.getOne(queryWrapper);
        if (packProp == null) {
            packProp = new RolePack();
            packProp.setId(0);
            packProp.setRoleId(role.getRoleId());
            packProp.setTypeId(ConstData.PACK_TYPE_PROP);
            packProp.setBaseId(id);
            packProp.setNum(0);
        }
        // 修改用户道具数量
        addPackNum(packProp,payNum);
        // 回写数据
        roleService.saveOrUpdate(role);
        saveSessionRole(session, role);
        packService.saveOrUpdate(packProp);
        // 消息
        String message = "购买成功，您花费" + totalFeePrice + GameUtils.getFeeTypeName(baseProp.getFeeType()) + "购买了" + baseProp.getName() + "×" + payNum;
        // 写日志
        MessageVO messageVO = MessageVO.success(message);
        messageVO.addLink(new MessageLink("去包裹看看", "/pack/prop/list.do"));
        model.addAttribute("message", messageVO);
        return "common/message";
    }
}
