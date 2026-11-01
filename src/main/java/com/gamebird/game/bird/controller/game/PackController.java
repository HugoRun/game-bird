package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gamebird.game.bird.assembler.RolePackAssembler;
import com.gamebird.game.bird.config.GameProperties;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.RoleBirth;
import com.gamebird.game.bird.model.dbo.RolePack;
import com.gamebird.game.bird.model.dbo.RoleTrain;
import com.gamebird.game.bird.model.dbo.RoleTrap;
import com.gamebird.game.bird.model.dbo.StorageBird;
import com.gamebird.game.bird.model.message.MessageVO;
import com.gamebird.game.bird.model.vo.RolePackVO;
import com.gamebird.game.bird.model.vo.UseResult;
import com.gamebird.game.bird.service.impl.PackUseService;

/**
 * 包裹相关路由
 *
 * <p>6 个列表方法已由 {@link RolePackAssembler#assemble} 统一装配视图对象，
 * 本类只剩「查哪一页、渲染哪个模板」两件事，因此每个方法只有 4 行有效逻辑。
 * 物品种类与配置字段的映射关系集中在 {@link RolePackAssembler.PackItemKind}。
 */
@RequestMapping("/pack")
@Controller
public class PackController extends BaseController {

    /** 包裹页面视图装配器，由注入取得。 */
    @Resource
    public RolePackAssembler packAssembler;

    /** 游戏配置（提供包裹总容量）。 */
    @Resource
    public GameProperties gameProperties;

    /** 包裹「使用」业务（装载陷阱/鸟笼/巢穴/训练场、道具效果分派）。 */
    @Resource
    public PackUseService packUseService;

    /**
     * 诱饵背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/bait/items")
    public String pagePackBait(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        return listPack(RolePackAssembler.PackItemKind.BAIT, session, model, pageNo, "pack/bait");
    }

    /**
     * 鸟笼背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/cage/items")
    public String pagePackCage(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        return listPack(RolePackAssembler.PackItemKind.CAGE, session, model, pageNo, "pack/cage");
    }

    /**
     * 鸟巢背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/nest/items")
    public String pagePackNest(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        return listPack(RolePackAssembler.PackItemKind.NEST, session, model, pageNo, "pack/nest");
    }

    /**
     * 道具背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/prop/items")
    public String pagePackProp(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        return listPack(RolePackAssembler.PackItemKind.PROP, session, model, pageNo, "pack/prop");
    }

    /**
     * 训练场背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/train/items")
    public String pagePackTrain(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        return listPack(RolePackAssembler.PackItemKind.TRAIN, session, model, pageNo, "pack/train");
    }

    /**
     * 陷阱背包界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @return view
     */
    @GetMapping(value = "/trap/items")
    public String pagePackTrap(HttpSession session, Model model, @RequestParam(defaultValue = "1") int pageNo) {
        return listPack(RolePackAssembler.PackItemKind.TRAP, session, model, pageNo, "pack/trap");
    }

    /**
     * 包裹列表的公共实现：查一页记录，装配视图对象，写入模型。
     *
     * @param kind    物品种类
     * @param session 会话
     * @param model   模型
     * @param pageNo  页码，小于 1 时按 1 处理
     * @param view    模板名
     * @return 视图名
     */
    private String listPack(RolePackAssembler.PackItemKind kind, HttpSession session, Model model, int pageNo, String view) {
        // 参数兼容
        int current = pageNo < 1 ? 1 : pageNo;
        // 数据库查询数据
        LambdaQueryWrapper<RolePack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RolePack::getRoleId, getSessionRole(session).getRoleId());
        queryWrapper.eq(RolePack::getTypeId, kind.packTypeId());
        queryWrapper.orderByAsc(RolePack::getBaseId);
        Page<RolePack> pageInfo = new Page<>();
        pageInfo.setCurrent(current);
        pageInfo.setSize(ConstData.PACK_PAGE_SIZE);
        packService.page(pageInfo, queryWrapper);
        // 数据转换（增加配置属性）
        Page<RolePackVO> viewPage = packAssembler.assemble(pageInfo, kind);
        model.addAttribute("page", viewPage);
        // U12：原先 6 个包裹模板各写死一个 3000，现统一由 game.pack-total-capacity 下发。
        // 只下发不校验 —— 当前版本本就没有限制包裹上限的逻辑，此处不得凭空加入校验。
        model.addAttribute("packCapacity", gameProperties.getPackTotalCapacity());
        return view;
    }

    /**
     * 道具目标选择页：药剂/加速类道具需要指定作用对象（小鸟/陷阱/训练槽/孵化槽）。
     * 页面按道具效果类型渲染对应的选择区，提交仍走 {@code POST /pack/prop/use}。
     */
    @GetMapping(value = "/prop/target")
    public String pagePropTarget(HttpSession session, Model model, @RequestParam Integer packId) {
        Role role = getSessionRole(session);
        RolePack pack = packService.getById(packId);
        if (pack == null || !role.getRoleId().equals(pack.getRoleId())
                || pack.getTypeId() == null || pack.getTypeId() != ConstData.PACK_TYPE_PROP
                || pack.getNum() == null || pack.getNum() <= 0) {
            model.addAttribute("message", MessageVO.error("道具不存在或数量不足。"));
            return ConstData.VIEW_MESSAGE;
        }
        RolePackVO packVO = packAssembler.assembleOne(pack);
        model.addAttribute("pack", pack);
        model.addAttribute("baseProp", packVO == null ? null : packVO.getBaseProp());
        // 小鸟（药剂目标）
        LambdaQueryWrapper<StorageBird> birdWrapper = new LambdaQueryWrapper<>();
        birdWrapper.eq(StorageBird::getRoleId, role.getRoleId());
        birdWrapper.orderByDesc(StorageBird::getWeight);
        model.addAttribute("birds", storageBirdService.list(birdWrapper));
        // 陷阱/训练/孵化槽（加速目标）
        model.addAttribute("trapSlots", trapService.list(new LambdaQueryWrapper<RoleTrap>()
                .eq(RoleTrap::getRoleId, role.getRoleId()).orderByAsc(RoleTrap::getIdx)));
        model.addAttribute("trainSlots", trainService.list(new LambdaQueryWrapper<RoleTrain>()
                .eq(RoleTrain::getRoleId, role.getRoleId()).orderByAsc(RoleTrain::getIdx)));
        model.addAttribute("birthSlots", birthService.list(new LambdaQueryWrapper<RoleBirth>()
                .eq(RoleBirth::getRoleId, role.getRoleId()).orderByAsc(RoleBirth::getIdx)));
        return "pack/propTarget";
    }

    // ==================== 使用（POST）：原先 5 类道具的使用按钮全部置灰 ====================

    /**
     * 使用陷阱：装入指定陷阱槽位。
     *
     * @param session session
     * @param model   model
     * @param packId  包裹记录 id
     * @param trapIdx 陷阱槽位序号（默认 1 号槽）
     * @return view
     */
    @PostMapping(value = "/trap/use")
    public String useTrap(HttpSession session, Model model, @RequestParam Integer packId,
                          @RequestParam(defaultValue = "1") int trapIdx) {
        return renderUseResult(session, model, packUseService.useTrap(getSessionRole(session), packId, trapIdx));
    }

    /**
     * 装备鸟笼（出战栏位，提供收网加成）。
     */
    @PostMapping(value = "/cage/use")
    public String useCage(HttpSession session, Model model, @RequestParam Integer packId) {
        return renderUseResult(session, model, packUseService.useCage(getSessionRole(session), packId));
    }

    /**
     * 使用巢穴：装入指定孵化槽位。
     */
    @PostMapping(value = "/nest/use")
    public String useNest(HttpSession session, Model model, @RequestParam Integer packId,
                          @RequestParam(defaultValue = "1") int birthIdx) {
        return renderUseResult(session, model, packUseService.useNest(getSessionRole(session), packId, birthIdx));
    }

    /**
     * 使用训练场：装入指定训练槽位。
     */
    @PostMapping(value = "/train/use")
    public String useTrainGround(HttpSession session, Model model, @RequestParam Integer packId,
                                 @RequestParam(defaultValue = "1") int trainIdx) {
        return renderUseResult(session, model, packUseService.useTrainGround(getSessionRole(session), packId, trainIdx));
    }

    /**
     * 使用道具：按 effect_type 分派。需要目标的效果（药剂/加速）经可选参数指定。
     */
    @PostMapping(value = "/prop/use")
    public String useProp(HttpSession session, Model model, @RequestParam Integer packId,
                          @RequestParam(required = false) Integer birdId,
                          @RequestParam(required = false) Integer trapId,
                          @RequestParam(required = false) Integer trainId,
                          @RequestParam(required = false) Integer birthId) {
        return renderUseResult(session, model,
                packUseService.useProp(getSessionRole(session), packId, birdId, trapId, trainId, birthId));
    }

    /**
     * 统一渲染使用结果：普通结果进消息页；带 redirectPath 的直接跳转。
     * 角色内存数值（爱心/金币）可能已被消耗类道具改动，这里同步回 session。
     */
    private String renderUseResult(HttpSession session, Model model, UseResult result) {
        saveSessionRole(session, getSessionRole(session));
        if (result.redirectPath() != null) {
            return redirectTo(result.redirectPath());
        }
        model.addAttribute("message", result.success()
                ? MessageVO.success(result.text())
                : MessageVO.error(result.text()));
        return ConstData.VIEW_MESSAGE;
    }

    // 使用道具
}