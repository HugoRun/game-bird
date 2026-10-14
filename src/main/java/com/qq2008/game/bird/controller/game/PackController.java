package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.qq2008.game.bird.assembler.RolePackAssembler;
import com.qq2008.game.bird.config.GameProperties;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.model.vo.RolePackVO;

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

    // 使用道具
}