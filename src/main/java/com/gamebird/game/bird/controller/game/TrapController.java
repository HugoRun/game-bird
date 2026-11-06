package com.gamebird.game.bird.controller.game;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.model.vo.RolePackVO;
import com.gamebird.game.bird.model.vo.TrapResultVO;
import com.gamebird.game.bird.service.BirdRaffleService;
import com.gamebird.game.bird.service.TrapService;
import com.gamebird.game.bird.util.GameUtils;

/**
 * 陷阱相关路由。
 *
 * <p>下饵/收网的业务规则已下沉 {@link TrapService}，本类只做参数接收、
 * 页面装配与消息渲染。收网由 GET 改为 POST（原为已知设计债：GET 写操作
 * 会被浏览器预取或刷新重复触发）。
 */
@Controller
@RequestMapping("/trap")
public class TrapController extends BaseController {

    /** 游戏配置读取入口，由字段注入。 */
    @Resource
    public GameConfigGateway configGateway;

    /** 按权重随机抽鸟（下饵页展示诱饵可捕捉的鸟时使用）。 */
    @Resource
    public BirdRaffleService birdRaffleService;

    /** 捕捉循环核心业务。 */
    @Resource
    public TrapService trapBizService;

    /** 游戏配置（加速单价等）。 */
    @Resource
    public GameProperties gameProperties;

    /**
     * 下饵界面。
     *
     * <p>trapId 缺省时自动选第一个可用陷阱槽 —— 包裹诱饵页的「使用」
     * 不再因漏传槽位而必然报「陷阱错误」。
     *
     * @param session session
     * @param model   model
     * @param trapId  陷阱槽位 id（role_trap.id），0 表示自动选择
     * @return view
     */
    @GetMapping("/useBait")
    public String pageUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId) {
        Role role = getSessionRole(session);
        RoleTrap roleTrap = selectUsableSlot(role, trapId);
        if (roleTrap == null) {
            model.addAttribute("message", MessageVO.error("没有可用的陷阱，请先更换或购买陷阱。"));
            return ConstData.VIEW_MESSAGE;
        }
        model.addAttribute("trapId", roleTrap.getId());
        model.addAttribute("trapIdx", roleTrap.getIdx());
        // 是否携带粘性道具（决定下饵页是否展示「涂抹粘性道具」选项）
        model.addAttribute("hasSticky", hasStickyProp(role));
        // 查询已拥有的诱饵数据
        List<RolePack> packList1 = listOwnedPack(role, ConstData.PACK_TYPE_BAIT);
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
     * 下饵。
     *
     * @param session   session
     * @param model     model
     * @param baitId    诱饵包裹记录 id（role_pack.id）
     * @param trapId    陷阱槽位 id
     * @param useSticky 是否涂抹粘性道具
     * @return view
     */
    @PostMapping("/useBait")
    public String doUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId,
                            @RequestParam(defaultValue = "0") int trapId,
                            @RequestParam(defaultValue = "false") boolean useSticky) {
        Role role = getSessionRole(session);
        try {
            trapBizService.useBait(role, trapId, baitId, useSticky);
        } catch (Exception e) {
            model.addAttribute("message", MessageVO.error(e.getMessage()));
            return ConstData.VIEW_MESSAGE;
        }
        saveSessionRole(session, role);
        return redirectTo("/game");
    }

    /**
     * 一键下饵：选一份诱饵，自动投入第一个空闲陷阱槽。
     *
     * @param session session
     * @param model   model
     * @param baitId  诱饵包裹记录 id；0 表示进入选择页
     * @return view
     */
    @GetMapping("/autoUseBait")
    public String pageAutoUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId) {
        Role role = getSessionRole(session);
        model.addAttribute("baitId", baitId);
        model.addAttribute("hasSticky", hasStickyProp(role));
        List<RolePack> packList1 = listOwnedPack(role, ConstData.PACK_TYPE_BAIT);
        List<RolePackVO> packList = new ArrayList<>();
        for (RolePack item : packList1) {
            RolePackVO roleBaitVO = new RolePackVO();
            BeanUtils.copyProperties(item, roleBaitVO);
            roleBaitVO.setBaseBait(configGateway.getBaseBait(roleBaitVO.getBaseId()));
            packList.add(roleBaitVO);
        }
        model.addAttribute("baitList", packList);
        return "trap/autoBait";
    }

    /**
     * 一键下饵提交。
     */
    @PostMapping("/autoUseBait")
    public String doAutoUseBait(HttpSession session, Model model, @RequestParam(defaultValue = "0") int baitId,
                                @RequestParam(defaultValue = "false") boolean useSticky) {
        Role role = getSessionRole(session);
        // 找第一个空闲且陷阱可用的槽位
        RoleTrap target = selectUsableSlot(role, 0);
        if (target == null) {
            model.addAttribute("message", MessageVO.error("没有空闲的陷阱槽，请先收网或更换陷阱。"));
            return ConstData.VIEW_MESSAGE;
        }
        try {
            trapBizService.useBait(role, target.getId(), baitId, useSticky);
        } catch (Exception e) {
            model.addAttribute("message", MessageVO.error(e.getMessage()));
            return ConstData.VIEW_MESSAGE;
        }
        saveSessionRole(session, role);
        MessageVO message = MessageVO.success("一键下饵成功，小鸟上钩后会通知你。");
        message.addLink(new MessageLink("回主页", "/game"));
        model.addAttribute("message", message);
        return ConstData.VIEW_MESSAGE;
    }

    /**
     * 收网（POST：原 GET 写操作会因浏览器预取/刷新重复触发）。
     *
     * @param session session
     * @param model   model
     * @param trapId  陷阱槽位 id
     * @return view
     */
    @PostMapping("/over")
    public String doOver(HttpSession session, Model model, @RequestParam(defaultValue = "0") int trapId) {
        Role role = getSessionRole(session);
        TrapResultVO resultVO;
        try {
            resultVO = trapBizService.over(role, trapId);
        } catch (Exception e) {
            model.addAttribute("message", MessageVO.error(e.getMessage()));
            return ConstData.VIEW_MESSAGE;
        }
        saveSessionRole(session, role);
        model.addAttribute("result", resultVO);
        return "trap/over";
    }

    /**
     * 陷阱加速：消耗钻石把等待时间缩短。
     *
     * @param session session
     * @param model   model
     * @param trapId  陷阱槽位 id
     * @return view
     */
    @PostMapping("/hasten")
    public String doHasten(HttpSession session, RedirectAttributes model, @RequestParam(defaultValue = "0") int trapId) {
        Role role = getSessionRole(session);
        RoleTrap slot = trapId <= 0 ? null : trapService.getById(trapId);
        if (slot == null || !role.getRoleId().equals(slot.getRoleId())) {
            model.addAttribute("message", MessageVO.error("没有该陷阱。"));
            return ConstData.VIEW_MESSAGE;
        }
        int now = CommonUtils.nowTime();
        if (slot.getBaitId() == 0) {
            model.addAttribute("message", MessageVO.error("当前陷阱正处于空闲中。"));
            return ConstData.VIEW_MESSAGE;
        }
        if (slot.getCatchEndTime() <= now) {
            model.addAttribute("message", MessageVO.error("小鸟已经落网，直接收网即可。"));
            return ConstData.VIEW_MESSAGE;
        }
        // 加速按 30 分钟粒度计价（每 30 分钟 5 钻）；裁剪以秒为单位，
        // 修复：剩余不足 1 分钟时分钟取整丢余数 → 扣了钻石时间却不变
        int remain = slot.getCatchEndTime() - now;
        int units = Math.max(1, (remain + 1799) / 1800);
        int cost = Math.max(1, units * gameProperties.getHastenDiamondPerHour() / 2);
        int cutSeconds = Math.min(remain, units * 1800);
        if (role.getDiamond() == null || role.getDiamond() < cost) {
            // 钻石不足：给出赚钻渠道引导，而不是让玩家卡在死路
            MessageVO message = MessageVO.error("缩短这段等待需要 " + cost + " 钻石，您的钻石只有 "
                    + (role.getDiamond() == null ? 0 : role.getDiamond()) + "。");
            message.addLink(new MessageLink("🗼 去试炼之塔赢钻石", "/fight/tower"));
            message.addLink(new MessageLink("🎁 每日签到有钻石", "/signin"));
            message.addLink(new MessageLink("回主页", "/game"));
            model.addAttribute("message", message);
            return ConstData.VIEW_MESSAGE;
        }
        roleValueService.costRoleDiamond(role, cost);
        slot.setCatchEndTime(slot.getCatchEndTime() - cutSeconds);
        trapService.updateById(slot);
        roleService.updateById(role);
        saveSessionRole(session, role);
        // 扣费与生效一致：提示带上实际裁剪时长
        String cutText = cutSeconds >= 60
                ? (cutSeconds / 60) + " 分钟"
                : cutSeconds + " 秒";
        // 成功后直接回主页，动线一步到位
        model.addFlashAttribute("message",
                MessageVO.success("消耗" + cost + "钻石，捕捉提前了 " + cutText + "！"));
        return redirectTo("/game");
    }

    /**
     * 小鸟详情（只读 base_bird 配置）。
     */
    @GetMapping("/birdInfo")
    public String pageBirdInfo(HttpSession session, Model model, @RequestParam(defaultValue = "0") int birdId) {
        BaseBird baseBird = configGateway.getBaseBird(birdId);
        model.addAttribute("baseBird", baseBird);
        return "trap/birdInfo";
    }

    // ==================== 私有工具 ====================

    /**
     * 选择一个可下饵的陷阱槽：trapId 精确匹配优先；否则取第一个
     * 空闲（未在下饵）且陷阱耐久可用的槽位。
     *
     * @return 可用槽位；无可用的返回 null
     */
    private RoleTrap selectUsableSlot(Role role, int trapId) {
        if (trapId > 0) {
            RoleTrap slot = trapService.getById(trapId);
            if (slot != null && role.getRoleId().equals(slot.getRoleId())
                    && slot.getBaitId() == 0
                    && (slot.getUseTrapId() == 0 || slot.getUseTrapNum() > 0)) {
                return slot;
            }
            return null;
        }
        LambdaQueryWrapper<RoleTrap> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleTrap::getRoleId, role.getRoleId());
        wrapper.orderByAsc(RoleTrap::getIdx);
        for (RoleTrap slot : trapService.list(wrapper)) {
            if (slot.getBaitId() == 0 && (slot.getUseTrapId() == 0 || slot.getUseTrapNum() > 0)) {
                return slot;
            }
        }
        return null;
    }

    /** 当前角色是否携带粘性道具（下饵页展示选项用）。 */
    private boolean hasStickyProp(Role role) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, ConstData.PACK_TYPE_PROP);
        wrapper.gt(RolePack::getNum, 0);
        for (RolePack pack : packService.list(wrapper)) {
            var prop = configGateway.getBaseProp(pack.getBaseId());
            if (prop != null && prop.getEffectType() != null && prop.getEffectType() == ConstData.PROP_EFFECT_STICKY) {
                return true;
            }
        }
        return false;
    }

    /** 查询当前角色指定类别且数量大于 0 的包裹记录。 */
    private List<RolePack> listOwnedPack(Role role, byte typeId) {
        LambdaQueryWrapper<RolePack> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePack::getRoleId, role.getRoleId());
        wrapper.eq(RolePack::getTypeId, typeId);
        wrapper.gt(RolePack::getNum, 0);
        return packService.list(wrapper);
    }
}
