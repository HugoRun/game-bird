package com.qq2008.game.bird.controller.game;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qq2008.common.entity.vo.MessageVO;
import com.qq2008.game.bird.controller.common.BaseController;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.ShopItemAccessors;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.shop.PurchaseResult;
import com.qq2008.game.bird.model.shop.ShopKind;
import com.qq2008.game.bird.service.ShopPurchaseService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/***
 * 商店相关路由
 *
 * <p><b>瘦身说明</b>：原先 6 个 {@code buyShopXxx} 各 79 行、共 476 行逐字重复的
 * 「校验 → 扣费 → 发货 → 写库」模板，已收敛为
 * {@link ShopPurchaseService#purchase} 一处实现；6 个列表方法各 23 行的分页模板
 * 已收敛为 {@link ShopItemAccessors#pageShopItems}。本类现在只做三件事：
 * 映射路由、从session 取角色、选视图名。
 *
 * <p><b>路由保持不变</b>：6 条 {@code /xxx/list.do} 与 6 条 {@code /xxx/buy.do}
 * 逐字保留。去后缀的路由规范化是另一批的工作，且必须与模板、字符串引用
 * 一起原子提交，不在本单元范围内。
 */
@RequestMapping("/shop")
@Controller
public class ShopController extends BaseController {

    /** 商店配置访问器（列表分页查询）。 */
    @Resource
    public ShopItemAccessors shopItemAccessors;

    /** 购买流程服务。 */
    @Resource
    public ShopPurchaseService shopPurchaseService;

    // ---------- 6 个列表方法：统一委托 listShopItems ----------

    /***
     * 诱饵商店界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return view
     */
    @GetMapping(value = "/bait/list.do")
    public String pageShopBait(HttpSession session, Model model,
                               @RequestParam(defaultValue = "1") int pageNo,
                               @RequestParam(defaultValue = "0") int feeType) {
        return listShopItems(session, model, ShopKind.BAIT, pageNo, feeType);
    }

    /***
     * 陷阱商店界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return view
     */
    @GetMapping(value = "/trap/list.do")
    public String pageShopTrap(HttpSession session, Model model,
                               @RequestParam(defaultValue = "1") int pageNo,
                               @RequestParam(defaultValue = "0") int feeType) {
        return listShopItems(session, model, ShopKind.TRAP, pageNo, feeType);
    }

    /***
     * 鸟笼商店界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return view
     */
    @GetMapping(value = "/cage/list.do")
    public String pageShopCage(HttpSession session, Model model,
                               @RequestParam(defaultValue = "1") int pageNo,
                               @RequestParam(defaultValue = "0") int feeType) {
        return listShopItems(session, model, ShopKind.CAGE, pageNo, feeType);
    }

    /***
     * 鸟巢商店界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return view
     */
    @GetMapping(value = "/nest/list.do")
    public String pageShopNest(HttpSession session, Model model,
                               @RequestParam(defaultValue = "1") int pageNo,
                               @RequestParam(defaultValue = "0") int feeType) {
        return listShopItems(session, model, ShopKind.NEST, pageNo, feeType);
    }

    /***
     * 训练场商店界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return view
     */
    @GetMapping(value = "/train/list.do")
    public String pageShopTrain(HttpSession session, Model model,
                                @RequestParam(defaultValue = "1") int pageNo,
                                @RequestParam(defaultValue = "0") int feeType) {
        return listShopItems(session, model, ShopKind.TRAIN, pageNo, feeType);
    }

    /***
     * 道具商店界面
     * @param session session
     * @param model model
     * @param pageNo 页码
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return view
     */
    @GetMapping(value = "/prop/list.do")
    public String pageShopProp(HttpSession session, Model model,
                               @RequestParam(defaultValue = "1") int pageNo,
                               @RequestParam(defaultValue = "0") int feeType) {
        return listShopItems(session, model, ShopKind.PROP, pageNo, feeType);
    }

    // ---------- 6 个购买方法：统一委托 buyShopItem ----------

    /***
     * 购买诱饵
     * @param session session
     * @param model model
     * @param id 配置id
     * @param payNum 购买数量
     * @return view
     */
    @PostMapping(value = "/bait/buy.do")
    public String buyShopBait(HttpSession session, Model model,
                              @RequestParam(defaultValue = "1") int id,
                              @RequestParam(defaultValue = "1") int payNum) {
        return buyShopItem(session, model, ShopKind.BAIT, id, payNum);
    }

    /***
     * 购买陷阱
     * @param session session
     * @param model model
     * @param id 配置id
     * @param payNum 购买数量
     * @return view
     */
    @PostMapping(value = "/trap/buy.do")
    public String buyShopTrap(HttpSession session, Model model,
                              @RequestParam(defaultValue = "1") int id,
                              @RequestParam(defaultValue = "1") int payNum) {
        return buyShopItem(session, model, ShopKind.TRAP, id, payNum);
    }

    /***
     * 购买鸟笼
     * @param session session
     * @param model model
     * @param id 配置id
     * @param payNum 购买数量
     * @return view
     */
    @PostMapping(value = "/cage/buy.do")
    public String buyShopCage(HttpSession session, Model model,
                              @RequestParam(defaultValue = "1") int id,
                              @RequestParam(defaultValue = "1") int payNum) {
        return buyShopItem(session, model, ShopKind.CAGE, id, payNum);
    }

    /***
     * 购买鸟巢
     * @param session session
     * @param model model
     * @param id 配置id
     * @param payNum 购买数量
     * @return view
     */
    @PostMapping(value = "/nest/buy.do")
    public String buyShopNest(HttpSession session, Model model,
                              @RequestParam(defaultValue = "1") int id,
                              @RequestParam(defaultValue = "1") int payNum) {
        return buyShopItem(session, model, ShopKind.NEST, id, payNum);
    }

    /***
     * 购买训练场
     * @param session session
     * @param model model
     * @param id 配置id
     * @param payNum 购买数量
     * @return view
     */
    @PostMapping(value = "/train/buy.do")
    public String buyShopTrain(HttpSession session, Model model,
                               @RequestParam(defaultValue = "1") int id,
                               @RequestParam(defaultValue = "1") int payNum) {
        return buyShopItem(session, model, ShopKind.TRAIN, id, payNum);
    }

    /***
     * 购买道具
     * @param session session
     * @param model model
     * @param id 配置id
     * @param payNum 购买数量
     * @return view
     */
    @PostMapping(value = "/prop/buy.do")
    public String buyShopProp(HttpSession session, Model model,
                              @RequestParam(defaultValue = "1") int id,
                              @RequestParam(defaultValue = "1") int payNum) {
        return buyShopItem(session, model, ShopKind.PROP, id, payNum);
    }

    // ---------- 6 个详情页：方法体原本就是单行返回视图名，保留原样 ----------

    /***
     * 诱饵详情
     * @param session session
     * @param model model
     * @param id 配置id
     * @return view
     */
    @GetMapping(value = "/bait/detail.do")
    public String pageShopBaitDetail(HttpSession session, Model model,
                                     @RequestParam(defaultValue = "1") int id) {
        return "shop/baitDetail";
    }

    /***
     * 陷阱详情
     * @param session session
     * @param model model
     * @param id 配置id
     * @return view
     */
    @GetMapping(value = "/trap/detail.do")
    public String pageShopTrapDetail(HttpSession session, Model model,
                                     @RequestParam(defaultValue = "1") int id) {
        return "shop/trapDetail";
    }

    /***
     * 鸟笼详情
     * @param session session
     * @param model model
     * @param id 配置id
     * @return view
     */
    @GetMapping(value = "/cage/detail.do")
    public String pageShopCageDetail(HttpSession session, Model model,
                                     @RequestParam(defaultValue = "1") int id) {
        return "shop/cageDetail";
    }

    /***
     * 鸟巢详情
     * @param session session
     * @param model model
     * @param id 配置id
     * @return view
     */
    @GetMapping(value = "/nest/detail.do")
    public String pageShopNestDetail(HttpSession session, Model model,
                                     @RequestParam(defaultValue = "1") int id) {
        return "shop/nestDetail";
    }

    /***
     * 训练场详情
     * @param session session
     * @param model model
     * @param id 配置id
     * @return view
     */
    @GetMapping(value = "/train/detail.do")
    public String pageShopTrainDetail(HttpSession session, Model model,
                                      @RequestParam(defaultValue = "1") int id) {
        return "shop/trainDetail";
    }

    /***
     * 道具详情
     * @param session session
     * @param model model
     * @param id 配置id
     * @return view
     */
    @GetMapping(value = "/prop/detail.do")
    public String pageShopPropDetail(HttpSession session, Model model,
                                     @RequestParam(defaultValue = "1") int id) {
        return "shop/propDetail";
    }

    // ---------- 公共实现 ----------

    /**
     * 商店列表的公共实现：查一页在售商品并写入模型。
     *
     * <p>过滤条件（{@code lu <= 角色等级}、{@code is_shopping = 1}、
     * {@code order by lu desc}）与每页条数，逐字沿用原实现。
     *
     * <p><b>为什么仍用原始实体而不是 {@code ShopItemView}</b>：模板经 SpEL 访问属性，
     * 而 record 的访问器是 {@code name()} 而非 {@code getName()}，SpEL 无法解析，
     * 一旦放进 Model 会让 6 个商店列表页与 {@code fragment/shop.html} 全部 500
     * （{@code EL1008E}）。record 只作购买路径内部的瞬时快照。
     *
     * @param session 会话
     * @param model   模型
     * @param kind    道具种类
     * @param pageNo  页码，小于 1 时按 1 处理
     * @param feeType 货币类型过滤，小于等于 0 不过滤
     * @return 视图名
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String listShopItems(HttpSession session, Model model, ShopKind kind, int pageNo, int feeType) {
        // 参数兼容
        int current = pageNo < 1 ? 1 : pageNo;
        // 参数回写（模板用它维持筛选状态）
        model.addAttribute("feeType", feeType);
        Role role = getSessionRole(session);
        Page pageInfo = new Page<>();
        pageInfo.setCurrent(current);
        pageInfo.setSize(ConstData.SHOP_PAGE_SIZE);
        shopItemAccessors.pageShopItems(pageInfo, role.getLevel(), feeType, serviceOf(kind));
        model.addAttribute("page", pageInfo);
        return kind.getView();
    }

    /**
     * 购买流程的公共实现。
     *
     * @param session 会话
     * @param model   模型
     * @param kind    道具种类
     * @param id      配置 id
     * @param payNum  购买数量
     * @return 恒为 {@code common/message}，与原实现一致
     */
    private String buyShopItem(HttpSession session, Model model, ShopKind kind, int id, int payNum) {
        Role role = getSessionRole(session);
        PurchaseResult result = shopPurchaseService.purchase(role, kind, id, payNum);
        model.addAttribute("message", result.message());
        return "common/message";
    }

    /**
     * 取该种类对应的配置 Service。
     *
     * <p>只有这一处需要按种类分派，故用 switch。
     *
     * <p><b>为什么这个 switch 不写 default</b>：分派对象是<b>枚举</b>，
     * Java 编译器能在将来新增第 7 个 {@link ShopKind} 而此处漏加 case 时
     * <b>直接编译报错</b>，这正是我们要的保护。写了 default 反而会掩盖它，
     * 让新种类悄悄走进某个既有分支。对比 {@code FeeKind.of} 走的是
     * {@code Byte} 参数、无法穷尽，那里<b>必须</b>统一兜底抛异常 —— 两条规则相反是有意为之。
     *
     * @param kind 道具种类
     * @return 该种类对应的 Service
     */
    @SuppressWarnings("rawtypes")
    private IService serviceOf(ShopKind kind) {
        return switch (kind) {
            case BAIT -> baseBaitService;
            case TRAP -> baseTrapService;
            case CAGE -> baseCageService;
            case NEST -> baseNestService;
            case TRAIN -> baseTrainService;
            case PROP -> basePropService;
        };
    }
}