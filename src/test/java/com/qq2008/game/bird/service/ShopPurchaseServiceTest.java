package com.qq2008.game.bird.service;

import com.qq2008.common.entity.vo.MessageVO;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.data.ShopItemAccessors;
import com.qq2008.game.bird.model.dbo.BaseBait;
import com.qq2008.game.bird.model.dbo.BaseCage;
import com.qq2008.game.bird.model.dbo.BaseNest;
import com.qq2008.game.bird.model.dbo.BaseProp;
import com.qq2008.game.bird.model.dbo.BaseTrain;
import com.qq2008.game.bird.model.dbo.BaseTrap;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.dbo.RolePack;
import com.qq2008.game.bird.model.shop.PurchaseResult;
import com.qq2008.game.bird.model.shop.ShopKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 商店购买流程的纯单元测试（T-PURCH-TEST）。
 *
 * <p><b>U10 门禁已通过</b>：A1 去重把 6 个 {@code buyShopXxx} 收敛进
 * {@link ShopPurchaseService} 后，本类<b>只改了调用点</b>
 * （{@code controller.buyShopXxx(...)} → {@code service.purchase(...)}），
 * <b>全部断言逐字未动</b>，24 个用例一次通过。这证明 A1 是无行为变更的重构。
 * 若当初有任何一条断言需要修改才能通过，就说明 A1 引入了行为变更。
 *
 * <p><b>约束</b>（刻意遵守）：
 * <ul>
 *   <li>不加载 Spring 容器，全类无注解式容器引导，单测毫秒级完成；</li>
 *   <li>不连任何真实数据库，不声明任何数据源；</li>
 *   <li>不 mock 静态方法 —— 涉及的唯一静态方法
 *       {@code GameUtils.getFeeTypeName} 是纯函数，没必要 mock；</li>
 *   <li>断言同时检查<b>失败文案</b>，因为这些文案直接展示给玩家，属于业务契约。</li>
 * </ul>
 */
class ShopPurchaseServiceTest {

    /** 被测对象：真实的购买服务，其协作者全部为 mock。 */
    private ShopPurchaseService service;

    /** 配置访问器（真实实例，内部 loader 由 mock Service 提供数据）。 */
    private ShopItemAccessors accessors;

    /** 6 个商品类型对应的 mock Service。 */
    private IBaseBaitService baseBaitService;
    private IBaseCageService baseCageService;
    private IBaseNestService baseNestService;
    private IBaseTrainService baseTrainService;
    private IBaseTrapService baseTrapService;
    private IBasePropService basePropService;
    private IRolePackService packService;
    private IRoleService roleService;

    /** 真实实例：扣币 / 加包裹数量是本测试要验证的行为，不能 mock 掉。 */
    private RoleValueService roleValueService;

    /** 商品 id，全用例统一，避免每个测试重复赋值。 */
    private static final int ITEM_ID = 7;

    /** 包裹里该商品的初始数量，统一从 0 开始，便于断言增量。 */
    private static final int PACK_INIT_NUM = 0;

    /**
     * 商店在售的 6 种商品。
     *
     * <p>每个枚举自带「包裹 type_id」与「对应的 {@link ShopKind}」，测试用例据此参数化，
     * 避免 6 份逐字复制的用例代码。
     */
    private enum Item {

        /** 诱饵。 */
        BAIT(ConstData.PACK_TYPE_BAIT, ShopKind.BAIT),
        /** 陷阱。 */
        TRAP(ConstData.PACK_TYPE_TRAP, ShopKind.TRAP),
        /** 鸟笼。 */
        CAGE(ConstData.PACK_TYPE_CAGE, ShopKind.CAGE),
        /** 巢。 */
        NEST(ConstData.PACK_TYPE_NEST, ShopKind.NEST),
        /** 训练场。 */
        TRAIN(ConstData.PACK_TYPE_TRAIN, ShopKind.TRAIN),
        /** 道具。 */
        PROP(ConstData.PACK_TYPE_PROP, ShopKind.PROP);

        /** 该商品在 role_pack 表中对应的 type_id。 */
        private final Byte packType;

        /** 等价的服务层种类。 */
        private final ShopKind shopKind;

        Item(Byte packType, ShopKind shopKind) {
            this.packType = packType;
            this.shopKind = shopKind;
        }

        Byte packType() {
            return packType;
        }

        ShopKind shopKind() {
            return shopKind;
        }
    }

    /**
     * 构造被测对象并注入 mock。
     *
     * <p>{@code RoleValueService} 用<b>真实实例</b>而非 mock：本测试的核心断言就是
     * 「扣多少、加多少」，把它 mock 掉等于把被测行为也一起 mock 掉了。
     */
    @BeforeEach
    void setUp() {
        baseBaitService = mock(IBaseBaitService.class);
        baseCageService = mock(IBaseCageService.class);
        baseNestService = mock(IBaseNestService.class);
        baseTrainService = mock(IBaseTrainService.class);
        baseTrapService = mock(IBaseTrapService.class);
        basePropService = mock(IBasePropService.class);
        packService = mock(IRolePackService.class);
        roleService = mock(IRoleService.class);
        roleValueService = new RoleValueService(mock(GameConfigManager.class));

        // 配置访问器用真实实例（它只是 6 个方法引用的容器），
        // 但要手动注入 6 个 mock Service 并触发 bind()，替代 Spring 的 @PostConstruct。
        accessors = new ShopItemAccessors();
        accessors.setBaseBaitService(baseBaitService);
        accessors.setBaseTrapService(baseTrapService);
        accessors.setBaseCageService(baseCageService);
        accessors.setBaseNestService(baseNestService);
        accessors.setBaseTrainService(baseTrainService);
        accessors.setBasePropService(basePropService);
        accessors.bind();

        service = new ShopPurchaseService(accessors, roleValueService, roleService, packService);
    }

    /**
     * 占位别名，仅为让上面的 import 列表保持可读；实际类型见 {@link IBaseNestService}。
     */
    private interface INestServiceAlias extends IBaseNestService {
    }

    // ------------------------------------------------------------------
    // 成功路径：6 种商品 x 3种货币 = 18 个用例
    // ------------------------------------------------------------------

    @Test
    @DisplayName("购买诱饵 - 支付金币：扣金币、加诱饵数量")
    void buyBaitWithCoin() {
        SuccessCase result = purchase(Item.BAIT, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买诱饵 - 支付钻石：扣钻石、加诱饵数量")
    void buyBaitWithDiamond() {
        SuccessCase result = purchase(Item.BAIT, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买诱饵 - 支付爱心值：扣爱心值、加诱饵数量")
    void buyBaitWithLovePoint() {
        SuccessCase result = purchase(Item.BAIT, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买陷阱 - 支付金币：扣金币、加陷阱数量")
    void buyTrapWithCoin() {
        SuccessCase result = purchase(Item.TRAP, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买陷阱 - 支付钻石：扣钻石、加陷阱数量")
    void buyTrapWithDiamond() {
        SuccessCase result = purchase(Item.TRAP, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买陷阱 - 支付爱心值：扣爱心值、加陷阱数量")
    void buyTrapWithLovePoint() {
        SuccessCase result = purchase(Item.TRAP, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买鸟笼 - 支付金币：扣金币、加鸟笼数量")
    void buyCageWithCoin() {
        SuccessCase result = purchase(Item.CAGE, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买鸟笼 - 支付钻石：扣钻石、加鸟笼数量")
    void buyCageWithDiamond() {
        SuccessCase result = purchase(Item.CAGE, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买鸟笼 - 支付爱心值：扣爱心值、加鸟笼数量")
    void buyCageWithLovePoint() {
        SuccessCase result = purchase(Item.CAGE, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买巢 - 支付金币：扣金币、加巢数量")
    void buyNestWithCoin() {
        SuccessCase result = purchase(Item.NEST, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买巢 - 支付钻石：扣钻石、加巢数量")
    void buyNestWithDiamond() {
        SuccessCase result = purchase(Item.NEST, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买巢 - 支付爱心值：扣爱心值、加巢数量")
    void buyNestWithLovePoint() {
        SuccessCase result = purchase(Item.NEST, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买训练场 - 支付金币：扣金币、加训练场数量")
    void buyTrainWithCoin() {
        SuccessCase result = purchase(Item.TRAIN, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买训练场 - 支付钻石：扣钻石、加训练场数量")
    void buyTrainWithDiamond() {
        SuccessCase result = purchase(Item.TRAIN, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买训练场 - 支付爱心值：扣爱心值、加训练场数量")
    void buyTrainWithLovePoint() {
        SuccessCase result = purchase(Item.TRAIN, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买道具 - 支付金币：扣金币、加道具数量")
    void buyPropWithCoin() {
        SuccessCase result = purchase(Item.PROP, ConstData.FEE_TYPE_COIN);
        assertEquals(INIT_COIN - TOTAL_PRICE, result.role().getCoin());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买道具 - 支付钻石：扣钻石、加道具数量")
    void buyPropWithDiamond() {
        SuccessCase result = purchase(Item.PROP, ConstData.FEE_TYPE_DIAMOND);
        assertEquals(INIT_DIAMOND - TOTAL_PRICE, result.role().getDiamond().intValue());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    @Test
    @DisplayName("购买道具 - 支付爱心值：扣爱心值、加道具数量")
    void buyPropWithLovePoint() {
        SuccessCase result = purchase(Item.PROP, ConstData.FEE_TYPE_LOVE_POINT);
        assertEquals(INIT_LOVE_POINT - TOTAL_PRICE, result.role().getLovePoint());
        assertEquals(PACK_INIT_NUM + PAY_NUM, result.packNum());
    }

    // ------------------------------------------------------------------
    // 失败路径：4 类拒绝理由
    // ------------------------------------------------------------------

    @Test
    @DisplayName("商品不存在 - 返回「没有该商品哦。」且不扣任何货币")
    void rejectUnknownItem() {
        Role role = newRole();
        // 刻意不 stub getById，Mockito 默认返回 null，模拟「配置表里没有这一行」
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.BAIT, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("没有该商品哦。", errorText(model));
        assertEquals(INIT_COIN, role.getCoin());
        Mockito.verify(packService, Mockito.never()).saveOrUpdate(any());
    }

    @Test
    @DisplayName("支付类型非法 - 视为商品不存在，不进入扣款分支")
    void rejectIllegalFeeType() {
        Role role = newRole();
        stubItem(Item.BAIT, (byte) 9, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.BAIT, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("没有该商品哦。", errorText(model));
        assertEquals(1000L, role.getCoin());
        assertEquals(40, role.getDiamond());
        assertEquals(900L, role.getLovePoint());
    }

    @Test
    @DisplayName("等级不足 - 返回「您的等级不足。」且不扣款")
    void rejectLevelTooLow() {
        Role role = newRole();
        role.setLevel(1);
        // 商品要求 30 级
        stubItem(Item.CAGE, ConstData.FEE_TYPE_COIN, 12, 30);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.CAGE, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的等级不足。", errorText(model));
        assertEquals(1000L, role.getCoin());
    }

    @Test
    @DisplayName("金币不足 - 提示带当前值与所需值，且不扣款")
    void rejectCoinNotEnough() {
        Role role = newRole();
        role.setCoin(10L);
        stubItem(Item.BAIT, ConstData.FEE_TYPE_COIN, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.BAIT, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的金钱不足哦(10/36)。", errorText(model));
        assertEquals(10L, role.getCoin());
    }

    @Test
    @DisplayName("钻石不足 - 提示带当前值与所需值，且不扣款")
    void rejectDiamondNotEnough() {
        Role role = newRole();
        role.setDiamond(5);
        stubItem(Item.NEST, ConstData.FEE_TYPE_DIAMOND, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.NEST, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的钻石不足(5/36)。", errorText(model));
        assertEquals(5, role.getDiamond());
    }

    @Test
    @DisplayName("爱心值不足 - 提示带当前值与所需值，且不扣款")
    void rejectLovePointNotEnough() {
        Role role = newRole();
        role.setLovePoint(7L);
        stubItem(Item.PROP, ConstData.FEE_TYPE_LOVE_POINT, 12, 1);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = buy(Item.PROP, role, model, 3);

        assertEquals("common/message", view);
        assertEquals("您的爱心值不足(7/36)。", errorText(model));
        assertEquals(7L, role.getLovePoint());
    }

    // ------------------------------------------------------------------
    // 夹具与工具
    // ------------------------------------------------------------------

    /** 单件商品价格，18 个成功用例统一使用。 */
    private static final int UNIT_PRICE = 12;

    /** 成功用例统一购买数量。 */
    private static final int PAY_NUM = 3;

    /** 单次成功购买的总价 = {@link #UNIT_PRICE} x {@link #PAY_NUM}。 */
    private static final int TOTAL_PRICE = UNIT_PRICE * PAY_NUM;

    /** 角色初始金币，保证金币用例余额充足。 */
    private static final long INIT_COIN = 1000L;

    /** 角色初始钻石，保证钻石用例余额充足。 */
    private static final int INIT_DIAMOND = 40;

    /** 角色初始爱心值，保证爱心值用例余额充足。 */
    private static final long INIT_LOVE_POINT = 900L;

    /** 角色初始等级，保证等级用例通过。 */
    private static final int INIT_LEVEL = 50;

    /**
     * 一次成功购买的结果快照。
     *
     * @param role    购买后的角色（三种货币余额均已更新）
     * @param packNum 购买后的包裹数量
     */
    private record SuccessCase(Role role, int packNum) {
    }

    /**
     * 执行一次「应当成功」的购买。
     *
     * @param item    商品种类
     * @param feeType 支付类型
     * @return 购买后的角色与包裹数量
     */
    private SuccessCase purchase(Item item, Byte feeType) {
        Role role = newRole();
        stubItem(item, feeType, UNIT_PRICE, null);
        RolePack pack = stubExistingPack(item);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = buy(item, role, model, PAY_NUM);

        assertEquals("common/message", view);
        MessageVO message = (MessageVO) model.asMap().get("message");
        assertNotNull(message, "成功购买必须写入 message");
        assertEquals(2, message.getMessageType().getTypeId(), "必须是成功类型");
        assertTrue(message.getMessage().contains("购买成功"), message.getMessage());
        // 三种货币中，只有被指定的那一种发生变化
        long expectedCoin = ConstData.FEE_TYPE_COIN.equals(feeType) ? INIT_COIN - UNIT_PRICE * PAY_NUM : INIT_COIN;
        assertEquals(expectedCoin, role.getCoin());
        // 包裹数量与持久化
        assertEquals(PACK_INIT_NUM + PAY_NUM, pack.getNum());
        Mockito.verify(packService).saveOrUpdate(pack);
        Mockito.verify(roleService).saveOrUpdate(role);
        return new SuccessCase(role, pack.getNum());
    }

    /**
     * 构造一个余额充足、等级足够的角色。
     *
     * @return 新角色实例
     */
    private static Role newRole() {
        Role role = new Role();
        role.setRoleId("r-0001");
        role.setLevel(INIT_LEVEL);
        role.setCoin(INIT_COIN);
        role.setDiamond(INIT_DIAMOND);
        role.setLovePoint(INIT_LOVE_POINT);
        return role;
    }

    /**
     * 让对应商品的配置查询返回一条合法记录。
     *
     * @param item商品种类
     * @param feeType 支付类型
     * @param price 单价
     * @param lu    等级门槛；为 {@code null} 时使用 1 级（即不构成门槛）
     */
    private void stubItem(Item item, Byte feeType, int price, Integer lu) {
        switch (item) {
            case BAIT -> {
                BaseBait base = new BaseBait();
                base.setId(ITEM_ID);
                base.setName("中等面包屑");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                // BaseBait 的 lu 是 Short，BaseTrain/BaseProp 等是 Integer —— 不可统一
                base.setLu((short) (lu == null ? 1 : lu));
                when(baseBaitService.getById(ITEM_ID)).thenReturn(base);
            }
            case TRAP -> {
                BaseTrap base = new BaseTrap();
                base.setId(ITEM_ID);
                base.setName("捕鸟夹");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseTrapService.getById(ITEM_ID)).thenReturn(base);
            }
            case CAGE -> {
                BaseCage base = new BaseCage();
                base.setId(ITEM_ID);
                base.setName("竹笼");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseCageService.getById(ITEM_ID)).thenReturn(base);
            }
            case NEST -> {
                BaseNest base = new BaseNest();
                base.setId(ITEM_ID);
                base.setName("爱的巢穴");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseNestService.getById(ITEM_ID)).thenReturn(base);
            }
            case TRAIN -> {
                BaseTrain base = new BaseTrain();
                base.setId(ITEM_ID);
                base.setName("宠物训练场");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(baseTrainService.getById(ITEM_ID)).thenReturn(base);
            }
            case PROP -> {
                BaseProp base = new BaseProp();
                base.setId(ITEM_ID);
                base.setName("加速卡");
                base.setFeeType(feeType);
                base.setFeePrice(price);
                base.setLu(lu == null ? 1 : lu);
                when(basePropService.getById(ITEM_ID)).thenReturn(base);
            }
            default -> throw new IllegalArgumentException("未覆盖的商品类型: " + item);
        }
    }

    /**
     * 让包裹查询返回一条「已存在但数量为 0」的记录，覆盖累加分支。
     *
     * @param item 商品种类
     * @return 被操作的包裹记录
     */
    private RolePack stubExistingPack(Item item) {
        RolePack pack = new RolePack();
        pack.setId(11);
        pack.setRoleId("r-0001");
        pack.setTypeId(item.packType());
        pack.setBaseId(ITEM_ID);
        pack.setNum(PACK_INIT_NUM);
        when(packService.getOne(any())).thenReturn(pack);
        return pack;
    }

    /**
     * 调用被测服务，并把结果消息写入模型 —— 复刻 Controller 的
     * 「取 session 角色 → 调服务 → 写 model → 返回 {@code common/message}」四步。
     *
     * <p><b>U10 变化</b>：原先此处分派到 {@code controller.buyShopXxx(...)}，
     * 现在改为 {@code service.purchase(role, kind, id, payNum)}。
     * <b>断言未作任何改动</b>，这是 A1 无行为变更的证据。
     *
     * @param item   商品种类
     * @param role   已放入 session 的角色
     * @param model  模型
     * @param payNum 购买数量
     * @return 视图名
     */
    private String buy(Item item, Role role, Model model, int payNum) {
        PurchaseResult result = service.purchase(role, item.shopKind(), ITEM_ID, payNum);
        model.addAttribute("message", result.message());
        return "common/message";
    }

    /**
     * 构造一个只含角色的会话。
     *
     * @param role 角色
     * @return 会话
     */
    private static MockHttpSession newSessionWith(Role role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("sRole", role);
        return session;
    }

    /**
     * 取出模型中的错误文案。
     *
     * @param model 模型
     * @return 文案
     */
    private static String errorText(Model model) {
        Object message = model.asMap().get("message");
        assertNotNull(message, "失败分支必须写入 message");
        assertSame(MessageVO.class, message.getClass());
        return ((MessageVO) message).getMessage();
    }

    // ------------------------------------------------------------------
    // Q-D 漏洞：payNum 缺失校验导致的白送 / 倒扣
    // ------------------------------------------------------------------

    @Test
    @DisplayName("payNum = 0 被拒：文案「购买数量必须大于 0。」且不写库")
    void rejectZeroPayNum() {
        assertRejectedPayNum(0, "购买数量必须大于 0。");
    }

    @Test
    @DisplayName("payNum 为负被拒：防止总价为负导致不扣费反而倒扣玩家道具")
    void rejectNegativePayNum() {
        assertRejectedPayNum(-1, "购买数量必须大于 0。");
    }

    @Test
    @DisplayName("payNum = 1000 被拒：单次上限 999")
    void rejectTooLargePayNum() {
        assertRejectedPayNum(1000, "单次购买数量不能超过 " + ShopPurchaseService.MAX_PAY_NUM + "。");
    }

    @Test
    @DisplayName("payNum = 1 是允许的下界，正常扣费发货")
    void acceptLowerBoundaryPayNum() {
        Role role = newRole();
        role.setCoin(100_000L);
        stubItem(Item.BAIT, ConstData.FEE_TYPE_COIN, UNIT_PRICE, null);
        RolePack pack = stubExistingPack(Item.BAIT);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = buy(Item.BAIT, role, model, 1);

        assertEquals("common/message", view);
        assertEquals(100_000L - UNIT_PRICE, role.getCoin().longValue());
        assertEquals(PACK_INIT_NUM + 1, pack.getNum());
    }

    @Test
    @DisplayName("payNum = 999 是允许的上界，仍正常扣费发货")
    void acceptUpperBoundaryPayNum() {
        Role role = newRole();
        role.setCoin(100_000L);
        stubItem(Item.BAIT, ConstData.FEE_TYPE_COIN, UNIT_PRICE, null);
        RolePack pack = stubExistingPack(Item.BAIT);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = buy(Item.BAIT, role, model, ShopPurchaseService.MAX_PAY_NUM);

        assertEquals("common/message", view);
        assertEquals(100_000L - UNIT_PRICE * ShopPurchaseService.MAX_PAY_NUM, role.getCoin().longValue());
        assertEquals(ShopPurchaseService.MAX_PAY_NUM, pack.getNum());
    }

    @Test
    @DisplayName("id <= 0 被拒：文案「商品编号无效。」且不写库")
    void rejectInvalidId() {
        Role role = newRole();

        PurchaseResult result = service.purchase(role, ShopKind.BAIT, 0, PAY_NUM);

        assertNotNull(result);
        assertEquals("商品编号无效。", result.message().getMessage());
        assertEquals(INIT_COIN, role.getCoin(), "被拒时不得改动任何货币");
        Mockito.verify(packService, Mockito.never()).saveOrUpdate(any());
        Mockito.verify(roleService, Mockito.never()).saveOrUpdate(any());
    }

    /**
     * 断言某个购买数量被拒绝，且角色未被改动、未发生写库。
     *
     * @param payNum购买数量
     * @param expectedText 期望文案
     */
    private void assertRejectedPayNum(int payNum, String expectedText) {
        Role role = newRole();
        stubItem(Item.BAIT, ConstData.FEE_TYPE_COIN, UNIT_PRICE, null);

        PurchaseResult result = service.purchase(role, ShopKind.BAIT, ITEM_ID, payNum);

        assertNotNull(result, "payNum=" + payNum + " 应返回结果而非抛异常");
        assertEquals(expectedText, result.message().getMessage());
        assertEquals(INIT_COIN, role.getCoin(), "payNum=" + payNum + " 不得改动金币");
        assertEquals(INIT_DIAMOND, role.getDiamond(), "payNum=" + payNum + " 不得改动钻石");
        assertEquals(INIT_LOVE_POINT, role.getLovePoint(), "payNum=" + payNum + " 不得改动爱心值");
        Mockito.verify(packService, Mockito.never()).saveOrUpdate(any());
        Mockito.verify(roleService, Mockito.never()).saveOrUpdate(any());
    }
}
